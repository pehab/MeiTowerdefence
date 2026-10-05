package de.haberland.meitowerdefense.model

data class MetaProgress(
    val stars: Int = 0,
    val upgradeLevels: Map<MetaUpgradeType, Int> = emptyMap()
) {
    fun levelOf(type: MetaUpgradeType): Int = upgradeLevels[type] ?: 0

    fun canAfford(type: MetaUpgradeType): Boolean {
        val cost = type.costForNextLevel(levelOf(type)) ?: return false
        return stars >= cost
    }

    fun purchase(type: MetaUpgradeType): MetaProgress {
        val level = levelOf(type)
        val cost = type.costForNextLevel(level) ?: return this
        if (stars < cost) return this
        return copy(stars = stars - cost, upgradeLevels = upgradeLevels + (type to level + 1))
    }

    fun addStars(amount: Int): MetaProgress = copy(stars = stars + amount)

    fun resetUpgrade(type: MetaUpgradeType): MetaProgress {
        val level = levelOf(type)
        if (level == 0) return this
        val refund = (0 until level).sumOf { type.costForNextLevel(it) ?: 0 }
        return copy(stars = stars + refund, upgradeLevels = upgradeLevels - type)
    }

    val goldIncomeBonusPercent: Int
        get() = GameBalance.Meta.GOLD_INCOME_PERCENT_PER_LEVEL * levelOf(MetaUpgradeType.GOLD_INCOME)
    val goldIncomeMultiplier: Float get() = 1f + goldIncomeBonusPercent / 100f
    val startingGoldBonus: Int
        get() = GameBalance.Meta.STARTING_GOLD_PER_LEVEL * levelOf(MetaUpgradeType.STARTING_GOLD)
    val startingLivesBonus: Int
        get() = GameBalance.Meta.STARTING_LIVES_PER_LEVEL * levelOf(MetaUpgradeType.STARTING_LIVES)
    val fireSplashRadiusBonus: Float
        get() = GameBalance.Meta.FIRE_SPLASH_RADIUS_PER_LEVEL * levelOf(MetaUpgradeType.FIRE_SPLASH_RADIUS)
    val iceSlowDurationBonus: Float
        get() = GameBalance.Meta.ICE_SLOW_DURATION_PER_LEVEL * levelOf(MetaUpgradeType.ICE_SLOW_DURATION)
    val archerDamageMultiplierBonus: Float
        get() = (GameBalance.Meta.ARCHER_DAMAGE_PERCENT_PER_LEVEL / 100f) * levelOf(MetaUpgradeType.ARCHER_DAMAGE)
}
