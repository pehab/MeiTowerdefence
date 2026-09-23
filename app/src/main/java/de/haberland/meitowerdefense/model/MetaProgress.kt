package de.haberland.meitowerdefense.model

/**
 * Account-wide progress: total stars and how many levels of each [MetaUpgradeType] have
 * been bought. Immutable - [purchase] returns an updated copy, the natural shape for
 * something that gets serialized to a save file after every change (see save/SaveData.kt).
 */
data class MetaProgress(
    val stars: Int = 0,
    val upgradeLevels: Map<MetaUpgradeType, Int> = emptyMap()
) {
    fun levelOf(type: MetaUpgradeType): Int = upgradeLevels[type] ?: 0

    fun canAfford(type: MetaUpgradeType): Boolean {
        val cost = type.costForNextLevel(levelOf(type)) ?: return false
        return stars >= cost
    }

    /** Returns this unchanged if [type] is already maxed or not affordable. */
    fun purchase(type: MetaUpgradeType): MetaProgress {
        val level = levelOf(type)
        val cost = type.costForNextLevel(level) ?: return this
        if (stars < cost) return this
        return copy(stars = stars - cost, upgradeLevels = upgradeLevels + (type to level + 1))
    }

    fun addStars(amount: Int): MetaProgress = copy(stars = stars + amount)

    // Derived, effective bonuses - the single place every one of these numbers is defined,
    // so e.g. GameSession and the star-shop UI can't drift out of sync on what a level of
    // "Goldader" is actually worth.
    val goldIncomeMultiplier: Float get() = 1f + 0.08f * levelOf(MetaUpgradeType.GOLD_INCOME)
    val startingGoldBonus: Int get() = 20 * levelOf(MetaUpgradeType.STARTING_GOLD)
    val startingLivesBonus: Int get() = 2 * levelOf(MetaUpgradeType.STARTING_LIVES)
    val fireSplashRadiusBonus: Float get() = 0.3f * levelOf(MetaUpgradeType.FIRE_SPLASH_RADIUS)
    val iceSlowDurationBonus: Float get() = 0.5f * levelOf(MetaUpgradeType.ICE_SLOW_DURATION)
    val archerDamageMultiplierBonus: Float get() = 0.15f * levelOf(MetaUpgradeType.ARCHER_DAMAGE)
}
