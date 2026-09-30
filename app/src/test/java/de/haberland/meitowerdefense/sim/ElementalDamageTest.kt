package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.*
import org.junit.Assert.*
import org.junit.Test

class ElementalDamageTest {
    private fun impact(source: TowerType, type: EnemyType, armorPierce: Int = 0): GameSession {
        val enemy = Enemy("target", type, 100f, 100f, Vec2(1f, 4f), pathIndex = 1)
        val shot = Projectile("shot", Vec2(1f, 4f), enemy.id, 100f, 20f, armorPierce,
            0f, 0f, 0f, 0f, 0f, 0f, source)
        return GameSimulator.step(GameSession.start(LevelCatalog.endless, MetaProgress())
            .copy(enemies = listOf(enemy), projectiles = listOf(shot)), 0.01f)
    }

    @Test
    fun fireAndIceIgnoreBothTankAndBossArmorWhilePhysicalShotsStillRespectIt() {
        for (type in listOf(EnemyType.ARMORED, EnemyType.BOSS)) {
            for (source in listOf(TowerType.FIRE, TowerType.ICE)) {
                assertEquals(80f, impact(source, type).enemies.single().hp, 0.001f)
            }
            assertEquals(100f - (20f - type.armor), impact(TowerType.ARCHER, type).enemies.single().hp, 0.001f)
            assertEquals(100f - (20f - type.armor), impact(TowerType.CANNON, type).enemies.single().hp, 0.001f)
        }
        assertEquals(80f, impact(TowerType.ARCHER, EnemyType.ARMORED, armorPierce = 6).enemies.single().hp, 0.001f)
    }

    @Test
    fun burnDamageAlsoBypassesArmor() {
        val enemy = Enemy("burning", EnemyType.BOSS, 100f, 100f, Vec2(1f, 4f), pathIndex = 1,
            burnDps = 6f, burnRemaining = 1f, burnSourceTowerType = TowerType.FIRE)
        val result = GameSimulator.step(GameSession.start(LevelCatalog.endless, MetaProgress())
            .copy(enemies = listOf(enemy)), 0.1f)
        assertEquals(99.4f, result.enemies.single().hp, 0.001f)
    }

    @Test
    fun groundExplosionsCannotDamageFlyersEvenWhereRoutesCross() {
        val ground = Enemy("ground", EnemyType.BASIC, 100f, 100f, Vec2(1f, 4f), pathIndex = 1)
        val air = ground.copy(id = "air", type = EnemyType.FLYING)
        val shot = Projectile("shot", Vec2(1f, 4f), ground.id, 100f, 20f, 0,
            2f, 0f, 0f, 0f, 0f, 0f, TowerType.CANNON)
        val result = GameSimulator.step(GameSession.start(LevelCatalog.endless, MetaProgress())
            .copy(enemies = listOf(ground, air), projectiles = listOf(shot)), 0.01f)
        assertEquals(100f, result.enemies.single { it.id == "air" }.hp, 0.001f)
    }
}
