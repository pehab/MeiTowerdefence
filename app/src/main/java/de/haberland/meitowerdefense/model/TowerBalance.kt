package de.haberland.meitowerdefense.model

/**
 * The shared level-scaling curve and upgrade-cost table every [TowerType] uses. Kept in
 * one place (rather than per-type formulas) so the whole game's power curve can be tuned
 * by editing one table, and so it's trivial to unit test in isolation.
 */
object TowerBalance {
    const val MAX_LEVEL = 5
    const val SPECIALIZATION_LEVEL = 3

    /** Cumulative stat multiplier at each level, 1-indexed (index 0 unused). */
    private val LEVEL_MULTIPLIER = floatArrayOf(0f, 1.0f, 1.35f, 1.75f, 2.15f, 2.6f)

    /** Gold cost multiplier (applied to TowerType.baseCost) to go from level N to N+1. */
    private val UPGRADE_COST_MULTIPLIER = floatArrayOf(0f, 0.6f, 1.0f, 1.5f, 2.0f)

    fun levelMultiplier(level: Int): Float {
        require(level in 1..MAX_LEVEL) { "level must be 1..$MAX_LEVEL, was $level" }
        return LEVEL_MULTIPLIER[level]
    }

    /** Gold cost to upgrade from [currentLevel] to currentLevel+1, or null if already maxed. */
    fun upgradeCost(type: TowerType, currentLevel: Int): Int? {
        if (currentLevel >= MAX_LEVEL) return null
        return (type.baseCost * UPGRADE_COST_MULTIPLIER[currentLevel]).toInt()
    }

    fun isSpecializationChoice(currentLevel: Int): Boolean = currentLevel == SPECIALIZATION_LEVEL
}
