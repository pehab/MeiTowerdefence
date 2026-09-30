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

    @Test
    fun cannonShotsFromBothSpecializationsDamageGroundGroupsButNeverCrossingFlyers() {
        val path = listOf(Vec2(0f, 4.5f), Vec2(13f, 4.5f))
        val level = LevelCatalog.endless.copy(groundPath = path, airPath = path)
        for (branch in listOf(null, Specialization.CANNON_SIEGE, Specialization.CANNON_MORTAR)) {
            val ground = Enemy("ground", EnemyType.BASIC, 1_000f, 1_000f, Vec2(5.5f, 4.5f), pathIndex = 1)
            var session = GameSession.start(level, MetaProgress()).copy(
                towers = listOf(Tower("cannon", TowerType.CANNON, GridPos(4, 4),
                    level = if (branch == null) 1 else 4, specialization = branch)),
                enemies = listOf(ground, ground.copy(id = "neighbor", position = Vec2(5.6f, 4.5f)),
                    ground.copy(id = "flyer", type = EnemyType.FLYING, position = Vec2(5.6f, 4.5f)))
            )
            repeat(40) { session = GameSimulator.step(session, 0.01f) }
            assertTrue("Ground target must still be hit for $branch", session.enemies.single { it.id == "ground" }.hp < 1_000f)
            assertTrue("Ground splash must remain effective for $branch", session.enemies.single { it.id == "neighbor" }.hp < 1_000f)
            assertEquals("Flyer must not receive cannon splash for $branch", 1_000f,
                session.enemies.single { it.id == "flyer" }.hp, 0.001f)
        }
    }

    @Test
    fun groundOnlyProjectilesCannotApplyDamageOrStatusEvenWithAnInvalidFlyingTarget() {
        for (source in listOf(TowerType.CANNON, TowerType.FIRE)) {
            val flyer = Enemy("flyer", EnemyType.FLYING, 100f, 100f, Vec2(1f, 4f), pathIndex = 1)
            val shot = Projectile("invalid", flyer.position, flyer.id, 100f, 20f, 0,
                2f, 0.4f, 2f, 1f, 6f, 3f, source)
            val result = GameSimulator.step(GameSession.start(LevelCatalog.endless, MetaProgress())
                .copy(enemies = listOf(flyer), projectiles = listOf(shot)), 0.01f).enemies.single()
            assertEquals(100f, result.hp, 0.001f)
            assertEquals(0f, result.slowRemaining, 0.001f)
            assertEquals(0f, result.frozenRemaining, 0.001f)
            assertEquals(0f, result.burnRemaining, 0.001f)
        }
    }

}
