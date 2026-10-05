package de.haberland.meitowerdefense.model

/**
 * Single source of truth for numeric gameplay balancing.
 *
 * Keep gameplay numbers here so simulation, glossary and upgrade previews cannot drift
 * apart. Content such as level geometry and authored campaign wave composition stays in
 * the content package; the reusable rules and scaling knobs live here.
 */
object GameBalance {

    data class TowerStats(
        val baseCost: Int,
        val baseDamage: Float,
        val baseRange: Float,
        val baseFireRate: Float,
        val baseSplashRadius: Float,
        val canHitFlying: Boolean,
        val burnDamagePerSecond: Float = 0f,
        val burnDurationSeconds: Float = 0f,
        val slowFactor: Float = 0f,
        val slowDurationSeconds: Float = 0f
    )

    data class EnemyStats(
        val baseHp: Int,
        val baseSpeed: Float,
        val armor: Int,
        val goldReward: Int,
        val flying: Boolean,
        val livesCost: Int = 1
    )

    data class SpecializationStats(
        val damageMultiplier: Float = 1f,
        val fireRateMultiplier: Float = 1f,
        val rangeMultiplier: Float = 1f,
        val splashRadiusBonus: Float = 0f,
        val armorPierce: Int = 0,
        val extraTargetChance: Float = 0f,
        val freezeChanceBonus: Float = 0f,
        val slowDurationBonus: Float = 0f,
        val slowFactorBonus: Float = 0f,
        val burnDamageBonus: Float = 0f,
        val burnDurationBonus: Float = 0f,
        val splashOnHitBonus: Float = 0f
    )

    val towerStats: Map<TowerType, TowerStats> = mapOf(
        TowerType.ARCHER to TowerStats(
            baseCost = 50, baseDamage = 8f, baseRange = 3.2f,
            baseFireRate = 2.0f, baseSplashRadius = 0f, canHitFlying = true
        ),
        TowerType.CANNON to TowerStats(
            baseCost = 90, baseDamage = 20f, baseRange = 2.6f,
            baseFireRate = 0.7f, baseSplashRadius = 1.0f, canHitFlying = false
        ),
        TowerType.FIRE to TowerStats(
            baseCost = 70, baseDamage = 5f, baseRange = 2.4f,
            baseFireRate = 1.2f, baseSplashRadius = 0f, canHitFlying = false,
            burnDamagePerSecond = 6f, burnDurationSeconds = 3f
        ),
        TowerType.ICE to TowerStats(
            baseCost = 60, baseDamage = 3f, baseRange = 2.6f,
            baseFireRate = 1.5f, baseSplashRadius = 0f, canHitFlying = true,
            slowFactor = 0.4f, slowDurationSeconds = 2f
        )
    )

    val enemyStats: Map<EnemyType, EnemyStats> = mapOf(
        EnemyType.BASIC to EnemyStats(46, 1.4f, 0, 5, false),
        EnemyType.FAST to EnemyStats(29, 2.4f, 0, 6, false),
        EnemyType.ARMORED to EnemyStats(125, 0.9f, 5, 10, false),
        EnemyType.FLYING to EnemyStats(40, 1.6f, 0, 8, true),
        EnemyType.BOSS to EnemyStats(1050, 0.7f, 10, 120, false, livesCost = 5)
    )

    val specializationStats: Map<Specialization, SpecializationStats> = mapOf(
        Specialization.ARCHER_SNIPER to SpecializationStats(
            damageMultiplier = 1.8f, fireRateMultiplier = 0.7f, armorPierce = 6
        ),
        Specialization.ARCHER_RAPID to SpecializationStats(
            fireRateMultiplier = 1.7f, extraTargetChance = 0.35f
        ),
        Specialization.CANNON_SIEGE to SpecializationStats(
            splashRadiusBonus = 0.6f, armorPierce = 8
        ),
        Specialization.CANNON_MORTAR to SpecializationStats(
            splashRadiusBonus = 1.2f, rangeMultiplier = 1.3f, damageMultiplier = 0.75f
        ),
        Specialization.FIRE_INFERNO to SpecializationStats(
            burnDamageBonus = 6f, burnDurationBonus = 2f
        ),
        Specialization.FIRE_SCORCH to SpecializationStats(
            splashOnHitBonus = 1.0f, damageMultiplier = 1.3f
        ),
        Specialization.ICE_DEEP_FREEZE to SpecializationStats(
            freezeChanceBonus = 0.25f, slowDurationBonus = 1.5f
        ),
        Specialization.ICE_FROSTBITE to SpecializationStats(
            slowFactorBonus = 0.2f, burnDamageBonus = 2f, burnDurationBonus = 2f
        )
    )

    fun tower(type: TowerType): TowerStats = towerStats.getValue(type)
    fun enemy(type: EnemyType): EnemyStats = enemyStats.getValue(type)
    fun specialization(type: Specialization): SpecializationStats = specializationStats.getValue(type)

    object TowerLevels {
        const val MAX_LEVEL = 5
        const val SPECIALIZATION_LEVEL = 3

        /** Cumulative stat multiplier, index = tower level. */
        val levelMultiplier = floatArrayOf(0f, 1.0f, 1.35f, 1.75f, 2.15f, 2.6f)

        /** Cost multiplier for upgrading from index/current level to the next level. */
        val upgradeCostMultiplier = floatArrayOf(0f, 0.6f, 1.0f, 1.5f, 2.0f)
    }

    object Meta {
        const val GOLD_INCOME_PERCENT_PER_LEVEL = 8
        const val STARTING_GOLD_PER_LEVEL = 20
        const val STARTING_LIVES_PER_LEVEL = 2
        const val FIRE_SPLASH_RADIUS_PER_LEVEL = 0.3f
        const val ICE_SLOW_DURATION_PER_LEVEL = 0.5f
        const val ARCHER_DAMAGE_PERCENT_PER_LEVEL = 15
    }

    data class MetaUpgradeStats(val maxLevel: Int, val baseStarCost: Int)

    val metaUpgradeStats: Map<MetaUpgradeType, MetaUpgradeStats> = mapOf(
        MetaUpgradeType.GOLD_INCOME to MetaUpgradeStats(5, 2),
        MetaUpgradeType.STARTING_GOLD to MetaUpgradeStats(5, 2),
        MetaUpgradeType.STARTING_LIVES to MetaUpgradeStats(5, 3),
        MetaUpgradeType.FIRE_SPLASH_RADIUS to MetaUpgradeStats(3, 4),
        MetaUpgradeType.ICE_SLOW_DURATION to MetaUpgradeStats(3, 4),
        MetaUpgradeType.ARCHER_DAMAGE to MetaUpgradeStats(3, 4)
    )

    fun metaUpgrade(type: MetaUpgradeType): MetaUpgradeStats = metaUpgradeStats.getValue(type)

    object Gameplay {
        const val MIN_BUILD_DISTANCE_FROM_PATH = 0.6f
        const val PROJECTILE_SPEED = 9f
        const val SELL_REFUND_FRACTION = 0.6f
        const val EARLY_WAVE_BONUS_GOLD = 15
    }

    object Endless {
        const val BASE_COUNT = 6
        const val COUNT_GROWTH_PER_WAVE = 0.7f
        const val HP_LINEAR_GROWTH = 0.115f
        const val HP_QUADRATIC_GROWTH = 0.0015f

        const val BOSS_EVERY_N_WAVES = 10
        const val BOSS_BASE_COUNT = 1
        const val BOSS_EXTRA_EVERY_WAVES = 30
        const val BOSS_SPAWN_INTERVAL = 3f
        const val BOSS_HP_MULTIPLIER = 1.1f

        const val BOSS_FAST_BASE_COUNT = 6
        const val BOSS_FAST_COUNT_DIVISOR = 2
        const val BOSS_FAST_SPAWN_INTERVAL = 0.45f
        const val BOSS_FAST_START_DELAY = 1.5f

        const val BOSS_FLYING_BASE_COUNT = 4
        const val BOSS_FLYING_COUNT_DIVISOR = 3
        const val BOSS_FLYING_SPAWN_INTERVAL = 0.6f
        const val BOSS_FLYING_START_DELAY = 3f

        const val MAIN_SPAWN_INTERVAL_START = 0.85f
        const val MAIN_SPAWN_INTERVAL_DECAY = 0.012f
        const val MAIN_SPAWN_INTERVAL_MIN = 0.28f

        const val FLYING_START_WAVE = 5
        const val FLYING_BASE_COUNT = 3
        const val FLYING_COUNT_DIVISOR = 4
        const val FLYING_SPAWN_INTERVAL = 0.7f
        const val FLYING_HP_MULTIPLIER = 0.9f
        const val FLYING_START_DELAY = 2f

        const val EXTRA_FAST_START_WAVE = 8
        const val EXTRA_FAST_BASE_COUNT = 4
        const val EXTRA_FAST_COUNT_DIVISOR = 5
        const val EXTRA_FAST_SPAWN_INTERVAL = 0.38f
        const val EXTRA_FAST_START_DELAY = 1f

        fun hpMultiplier(index: Int): Float =
            1f + index * HP_LINEAR_GROWTH + index * index * HP_QUADRATIC_GROWTH
    }
}
