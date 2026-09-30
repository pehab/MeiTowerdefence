package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2
import org.junit.Assert.*
import org.junit.Test

class AchievementTelemetryTest {
    private fun enemy(id: String, type: EnemyType, x: Float = 1f) = Enemy(
        id, type, 10f, 10f, Vec2(x, 4f), pathIndex = 1)
    private fun session(enemies: List<Enemy>) = GameSession.start(LevelCatalog.endless, MetaProgress()).copy(enemies = enemies)
    private fun shot(type: TowerType, target: String, splash: Float = 0f) = Projectile(
        id = "shot", position = Vec2(1f, 4f), targetEnemyId = target, speed = 100f,
        damage = 100f, armorPierce = 0, splashRadius = splash,
        slowFactor = 0f, slowDuration = 0f, freezeChance = 0f, burnDps = 0f,
        burnDuration = 0f, sourceTowerType = type)

    @Test
    fun directAndSplashKillsCreditEnemyTypesAndTheFiringTower() {
        val start = session(listOf(enemy("tank", EnemyType.ARMORED), enemy("basic", EnemyType.BASIC, 1.5f)))
            .copy(projectiles = listOf(shot(TowerType.CANNON, "tank", splash = 1f)))
        val result = GameSimulator.step(start, 0.1f)
        assertEquals(2, result.stats.enemiesKilled)
        assertEquals(1, result.stats.killsByEnemy[EnemyType.ARMORED])
        assertEquals(1, result.stats.killsByEnemy[EnemyType.BASIC])
        assertEquals(2, result.stats.killsByTower[TowerType.CANNON])
        val again = GameSimulator.step(result, 0.1f)
        assertEquals(result.stats, again.stats)
    }

    @Test
    fun lethalBurnCreditsItsSourceRatherThanTheLastDirectHit() {
        val target = enemy("burning", EnemyType.BOSS).copy(hp = 0.5f, burnDps = 10f, burnRemaining = 1f,
            burnSourceTowerType = TowerType.FIRE, lastHitTowerType = TowerType.ARCHER)
        val result = GameSimulator.step(session(listOf(target)), 0.1f)
        assertEquals(1, result.stats.killsByEnemy[EnemyType.BOSS])
        assertEquals(1, result.stats.killsByTower[TowerType.FIRE])
        assertEquals(0, result.stats.killsByTower[TowerType.ARCHER])
    }

    @Test
    fun leakedEnemiesDoNotCountAsKills() {
        val start = session(emptyList())
        val leaked = enemy("leak", EnemyType.FLYING).copy(pathIndex = start.level.airPath.size)
        val result = GameSimulator.step(start.copy(enemies = listOf(leaked)), 0.1f)
        assertEquals(0, result.stats.enemiesKilled)
        assertTrue(result.stats.killsByEnemy.isEmpty())
    }
}
