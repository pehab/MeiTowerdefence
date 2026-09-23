package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerBalance
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2
import de.haberland.meitowerdefense.model.WaveEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameSimulatorTest {

    private fun straightLevel(
        waves: List<WaveEntry> = listOf(WaveEntry(EnemyType.BASIC, count = 1, spawnIntervalSeconds = 1f)),
        startingGold: Int = 500,
        startingLives: Int = 10,
        timeBetweenWaves: Float = 1f
    ) = LevelDefinition(
        id = "test",
        displayName = "Test",
        gridWidth = 12,
        gridHeight = 10,
        // Ground path near the bottom, air path near the top - far enough apart (6
        // rows) that a tower can be placed comfortably close to one without being
        // rejected as "too close" to the other.
        groundPath = listOf(Vec2(0f, 7f), Vec2(11f, 7f)),
        airPath = listOf(Vec2(0f, 1f), Vec2(11f, 1f)),
        waves = waves,
        startingGold = startingGold,
        startingLives = startingLives,
        timeBetweenWaves = timeBetweenWaves
    )

    private fun freshSession(level: LevelDefinition = straightLevel(), meta: MetaProgress = MetaProgress()) =
        GameSession.start(level, meta)

    // --- build ---

    @Test
    fun buildingATowerSpendsGoldAndAddsIt() {
        val session = freshSession()
        val result = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))
        assertNotNull(result)
        assertEquals(session.gold - TowerType.ARCHER.baseCost, result!!.gold)
        assertEquals(1, result.towers.size)
        assertEquals(TowerType.ARCHER, result.towers.first().type)
    }

    @Test
    fun buildingTooCloseToThePathIsRejected() {
        val session = freshSession()
        // The ground path runs along row 3; row 3 itself is essentially on the path.
        val result = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(5, 7))
        assertNull(result)
    }

    @Test
    fun buildingWithoutEnoughGoldIsRejected() {
        val session = freshSession(level = straightLevel(startingGold = 10))
        val result = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))
        assertNull(result)
    }

    @Test
    fun buildingOnAnAlreadyOccupiedCellIsRejected() {
        val session = freshSession()
        val afterFirst = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))!!
        val afterSecond = GameSimulator.buildTower(afterFirst, TowerType.ARCHER, GridPos(2, 8))
        assertNull(afterSecond)
    }

    // --- upgrade ---

    @Test
    fun upgradingIncreasesLevelAndSpendsGold() {
        val session = freshSession()
        val withTower = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))!!
        val cost = withTower.towers.first().upgradeCost()!!
        val upgraded = GameSimulator.upgradeTower(withTower, withTower.towers.first().id)
        assertNotNull(upgraded)
        assertEquals(2, upgraded!!.towers.first().level)
        assertEquals(withTower.gold - cost, upgraded.gold)
    }

    @Test
    fun reachingSpecializationLevelRequiresAMatchingBranchChoice() {
        val session = freshSession(level = straightLevel(startingGold = 10_000))
        var s = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))!!
        val towerId = s.towers.first().id

        // Level up to just below the specialization threshold.
        while (s.towers.first().level < TowerBalance.SPECIALIZATION_LEVEL) {
            s = GameSimulator.upgradeTower(s, towerId)!!
        }
        assertTrue(s.towers.first().needsSpecializationChoice)

        // Missing choice: rejected.
        assertNull(GameSimulator.upgradeTower(s, towerId))
        // Wrong tower type's branch: rejected.
        assertNull(GameSimulator.upgradeTower(s, towerId, Specialization.ICE_DEEP_FREEZE))
        // Correct branch: accepted, and recorded on the tower.
        val specialized = GameSimulator.upgradeTower(s, towerId, Specialization.ARCHER_SNIPER)
        assertNotNull(specialized)
        assertEquals(Specialization.ARCHER_SNIPER, specialized!!.towers.first().specialization)
        assertEquals(TowerBalance.SPECIALIZATION_LEVEL + 1, specialized.towers.first().level)
    }

    @Test
    fun cannotUpgradePastMaxLevel() {
        val session = freshSession(level = straightLevel(startingGold = 10_000))
        var s = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))!!
        val towerId = s.towers.first().id
        while (s.towers.first().level < TowerBalance.SPECIALIZATION_LEVEL) {
            s = GameSimulator.upgradeTower(s, towerId)!!
        }
        s = GameSimulator.upgradeTower(s, towerId, Specialization.ARCHER_SNIPER)!!
        while (s.towers.first().level < TowerBalance.MAX_LEVEL) {
            s = GameSimulator.upgradeTower(s, towerId)!!
        }
        assertEquals(TowerBalance.MAX_LEVEL, s.towers.first().level)
        assertNull(GameSimulator.upgradeTower(s, towerId))
    }

    // --- sell ---

    @Test
    fun sellingRemovesTheTowerAndRefundsPartOfItsCost() {
        val session = freshSession()
        val withTower = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(2, 8))!!
        val goldAfterBuild = withTower.gold
        val sold = GameSimulator.sellTower(withTower, withTower.towers.first().id)
        assertNotNull(sold)
        assertTrue(sold!!.towers.isEmpty())
        assertTrue("expected a partial refund", sold.gold > goldAfterBuild)
        assertTrue("refund should be less than the full price paid", sold.gold < goldAfterBuild + TowerType.ARCHER.baseCost)
    }

    // --- tick simulation ---

    @Test
    fun anEnemySpawnsOnlyAfterTheWaveStartDelay() {
        val session = freshSession(level = straightLevel(timeBetweenWaves = 2f))
        val justBefore = GameSimulator.step(session, dt = 1.9f)
        assertTrue(justBefore.enemies.isEmpty())
        val justAfter = GameSimulator.step(justBefore, dt = 0.2f)
        assertTrue(justAfter.enemies.isNotEmpty())
    }

    @Test
    fun anEnemyReachingTheEndCostsALifeAndIsRemoved() {
        var session = freshSession(level = straightLevel(timeBetweenWaves = 0f))
        val startingLives = session.lives
        repeat(2000) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }
        assertTrue(session.lives < startingLives)
        assertTrue(session.enemies.none { !it.isDead })
    }

    @Test
    fun aStrongWellPlacedTowerKillsAnEnemyBeforeItReachesTheEnd() {
        var session = freshSession(level = straightLevel(startingGold = 10_000, timeBetweenWaves = 0f))
        session = GameSimulator.buildTower(session, TowerType.CANNON, GridPos(2, 8))!!
        // Max the tower out so a single BASIC enemy dies almost immediately.
        val towerId = session.towers.first().id
        while (session.towers.first().canUpgrade) {
            session = if (session.towers.first().needsSpecializationChoice) {
                GameSimulator.upgradeTower(session, towerId, Specialization.CANNON_SIEGE)!!
            } else {
                GameSimulator.upgradeTower(session, towerId)!!
            }
        }

        val startingLives = session.lives
        val goldBeforeKill = session.gold
        repeat(200) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }

        assertEquals("no life should have been lost", startingLives, session.lives)
        assertTrue("expected gold from a kill", session.gold > goldBeforeKill)
    }

    @Test
    fun splashDamageAlsoHitsNearbyEnemies() {
        // Two BASIC enemies close together (short spawn interval on a slow-ish enemy),
        // one well-upgraded CANNON with splash should be able to kill both from one shot
        // or in short order, well before either could reach the end of a long path.
        val level = straightLevel(
            waves = listOf(WaveEntry(EnemyType.BASIC, count = 2, spawnIntervalSeconds = 0.1f)),
            startingGold = 10_000,
            timeBetweenWaves = 0f
        )
        var session = freshSession(level)
        session = GameSimulator.buildTower(session, TowerType.CANNON, GridPos(2, 8))!!
        val towerId = session.towers.first().id
        while (session.towers.first().canUpgrade) {
            session = if (session.towers.first().needsSpecializationChoice) {
                GameSimulator.upgradeTower(session, towerId, Specialization.CANNON_SIEGE)!!
            } else {
                GameSimulator.upgradeTower(session, towerId)!!
            }
        }

        repeat(200) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }

        assertEquals(GameOutcome.WON, session.outcome)
    }

    @Test
    fun aGroundOnlyTowerCannotTargetAFlyingEnemy() {
        val level = straightLevel(waves = listOf(WaveEntry(EnemyType.FLYING, count = 1, spawnIntervalSeconds = 1f)))
        var session = freshSession(level.copy(startingGold = 10_000, timeBetweenWaves = 0f))
        // Cannon cannot hit flying enemies; place it right next to the air path.
        session = GameSimulator.buildTower(session, TowerType.CANNON, GridPos(2, 2))!!
        val towerId = session.towers.first().id
        while (session.towers.first().canUpgrade) {
            session = if (session.towers.first().needsSpecializationChoice) {
                GameSimulator.upgradeTower(session, towerId, Specialization.CANNON_SIEGE)!!
            } else {
                GameSimulator.upgradeTower(session, towerId)!!
            }
        }

        val startingLives = session.lives
        repeat(400) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }

        // The flying enemy should have reached the end untouched.
        assertTrue("a ground-only tower should not be able to stop a flying enemy", session.lives < startingLives)
    }

    @Test
    fun iceTowerSlowMakesAnEnemyTakeLongerToCrossThanItWouldUnslowed() {
        val level = straightLevel(waves = listOf(WaveEntry(EnemyType.BASIC, count = 1, spawnIntervalSeconds = 1f)), timeBetweenWaves = 0f)
        val baselineTicksToReachEnd = run {
            var session = freshSession(level)
            var ticks = 0
            while (session.enemies.isNotEmpty() || session.lives == session.level.startingLives) {
                session = GameSimulator.step(session, dt = 0.05f)
                ticks++
                if (session.lives < session.level.startingLives) break
                if (ticks > 2000) error("baseline never finished")
            }
            ticks
        }

        val slowedTicksToReachEnd = run {
            var session = freshSession(level.copy(startingGold = 10_000))
            session = GameSimulator.buildTower(session, TowerType.ICE, GridPos(5, 8))!!
            var ticks = 0
            while (session.lives == session.level.startingLives) {
                session = GameSimulator.step(session, dt = 0.05f)
                ticks++
                if (ticks > 5000) error("slowed run never finished")
            }
            ticks
        }

        assertTrue(
            "a slowed enemy should take measurably longer to cross ($slowedTicksToReachEnd) than an unslowed one ($baselineTicksToReachEnd)",
            slowedTicksToReachEnd > baselineTicksToReachEnd
        )
    }

    @Test
    fun winTriggersOnceAllWavesAreClearedAndNoEnemiesRemain() {
        val level = straightLevel(startingGold = 10_000, timeBetweenWaves = 0f)
        var session = freshSession(level)
        session = GameSimulator.buildTower(session, TowerType.CANNON, GridPos(2, 8))!!
        val towerId = session.towers.first().id
        while (session.towers.first().canUpgrade) {
            session = if (session.towers.first().needsSpecializationChoice) {
                GameSimulator.upgradeTower(session, towerId, Specialization.CANNON_SIEGE)!!
            } else {
                GameSimulator.upgradeTower(session, towerId)!!
            }
        }
        repeat(200) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }
        assertEquals(GameOutcome.WON, session.outcome)
    }

    @Test
    fun loseTriggersOnceLivesReachZero() {
        val level = straightLevel(
            waves = listOf(WaveEntry(EnemyType.BASIC, count = 20, spawnIntervalSeconds = 0.05f)),
            startingLives = 3,
            timeBetweenWaves = 0f
        )
        var session = freshSession(level) // no towers at all - every enemy gets through
        repeat(3000) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }
        assertEquals(GameOutcome.LOST, session.outcome)
        assertEquals(0, session.lives)
    }

    @Test
    fun stepIsANoOpOnceTheGameHasConcluded() {
        val level = straightLevel(startingLives = 1, waves = listOf(WaveEntry(EnemyType.BASIC, count = 1, spawnIntervalSeconds = 1f)), timeBetweenWaves = 0f)
        var session = freshSession(level)
        repeat(500) {
            if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f)
        }
        assertEquals(GameOutcome.LOST, session.outcome)
        val afterConcluded = GameSimulator.step(session, dt = 1f)
        assertEquals(session, afterConcluded)
    }

    @Test
    fun simulationIsDeterministicForAFixedSeed() {
        val level = straightLevel(
            waves = listOf(WaveEntry(EnemyType.FAST, count = 5, spawnIntervalSeconds = 0.2f)),
            startingGold = 300,
            timeBetweenWaves = 0f
        )

        fun run(): GameSession {
            var session = freshSession(level)
            session = GameSimulator.buildTower(session, TowerType.ARCHER, GridPos(3, 8))!!
            val random = Random(123)
            repeat(500) {
                if (session.outcome == GameOutcome.IN_PROGRESS) session = GameSimulator.step(session, dt = 0.05f, random = random)
            }
            return session
        }

        val first = run()
        val second = run()
        assertEquals(first.outcome, second.outcome)
        assertEquals(first.gold, second.gold)
        assertEquals(first.lives, second.lives)
        assertEquals(first.enemies.size, second.enemies.size)
    }
}
