package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.*
import org.junit.Assert.*
import org.junit.Test

class TargetingTest {
    private fun enemy(id: String, x: Float, type: EnemyType = EnemyType.BASIC) =
        Enemy(id, type, 1_000f, 1_000f, Vec2(x, 4.5f), pathIndex = 1)
    private fun session(type: TowerType, enemies: List<Enemy>, tower: Tower = Tower("tower", type, GridPos(4, 4))) =
        GameSession.start(LevelCatalog.endless.copy(
            groundPath = listOf(Vec2(0f, 4.5f), Vec2(13f, 4.5f)),
            airPath = listOf(Vec2(0f, 4.5f), Vec2(13f, 4.5f))), MetaProgress())
            .copy(towers = listOf(tower), enemies = enemies)
    private fun target(session: GameSession) = GameSimulator.step(session, 0.001f).projectiles.single().targetEnemyId

    @Test
    fun archersPreferReachableFlyersButFallBackToTheMostUrgentGroundEnemy() {
        val start = session(TowerType.ARCHER, listOf(enemy("ground", 7f), enemy("flyer", 5f, EnemyType.FLYING)))
        assertEquals("flyer", target(start))
        assertEquals("ground", target(start.copy(enemies = listOf(enemy("ground", 7f), enemy("far-flyer", 12f, EnemyType.FLYING)))))
        assertEquals("fast", target(start.copy(enemies = listOf(enemy("basic", 6f), enemy("fast", 5f, EnemyType.FAST)))))
    }

    @Test
    fun icePrefersUnfrozenEnemiesAndSpreadsSlowBeforeRefreshingIt() {
        val frozen = enemy("frozen", 6.5f).copy(frozenRemaining = 10f)
        val slowed = enemy("slowed", 6f).copy(slowRemaining = 10f, slowFactor = 0.4f)
        val fresh = enemy("fresh", 5f)
        assertEquals("fresh", target(session(TowerType.ICE, listOf(frozen, slowed, fresh))))
        assertEquals("slowed", target(session(TowerType.ICE, listOf(frozen, slowed))))
        assertEquals("frozen", target(session(TowerType.ICE, listOf(frozen))))
    }

    @Test
    fun firePrefersUnburnedEnemiesButKeepsFiringWhenEverythingBurns() {
        val burning = enemy("burning", 6.5f).copy(burnRemaining = 10f, burnDps = 1f)
        assertEquals("fresh", target(session(TowerType.FIRE, listOf(burning, enemy("fresh", 5f)))))
        assertEquals("burning", target(session(TowerType.FIRE, listOf(burning))))
    }

    @Test
    fun controlTowersAccountForShotsAlreadyInFlightIncludingOtherTowersInTheSameTick() {
        for (type in listOf(TowerType.ICE, TowerType.FIRE)) {
            val start = session(type, listOf(enemy("front", 6.5f), enemy("back", 5.5f)))
                .copy(towers = listOf(Tower("first", type, GridPos(4, 4)), Tower("second", type, GridPos(5, 5))))
            val result = GameSimulator.step(start, 0.001f)
            assertEquals(setOf("front", "back"), result.projectiles.map { it.targetEnemyId }.toSet())
            val later = GameSimulator.step(result.copy(towers = listOf(Tower("third", type, GridPos(5, 5)))), 0.001f)
            assertEquals(3, later.projectiles.size) // Still fires when all candidates have an incoming effect.
        }
    }

    @Test
    fun cannonPrefersTheGroundClusterOverAnIsolatedLeaderOrNearbyFlyers() {
        val targets = listOf(enemy("isolated", 7f), enemy("back", 3f), enemy("middle", 3.4f), enemy("front", 3.8f)) +
            List(5) { enemy("flyer-$it", 7f, EnemyType.FLYING) }
        assertEquals("front", target(session(TowerType.CANNON, targets)))
    }

    @Test
    fun cannonUsesItsActualUpgradedSplashRadius() {
        val targets = listOf(enemy("left-back", 3.8f), enemy("left-front", 4.3f),
            enemy("right", 7.5f), enemy("right-front", 8.7f), enemy("outside", 9.1f))
        val base = Tower("cannon", TowerType.CANNON, GridPos(5, 4))
        assertEquals("left-front", target(session(TowerType.CANNON, targets, base)))
        assertEquals("right-front", target(session(TowerType.CANNON, targets,
            base.copy(level = 4, specialization = Specialization.CANNON_MORTAR))))
    }

    @Test
    fun groundOnlyTowersNeverTargetFlyersAndDeadEnemiesAreIgnored() {
        for (type in listOf(TowerType.FIRE, TowerType.CANNON)) {
            val start = session(type, listOf(enemy("flyer", 5f, EnemyType.FLYING), enemy("dead", 6f).copy(hp = 0f)))
            assertTrue(GameSimulator.step(start, 0.001f).projectiles.isEmpty())
        }
    }
}
