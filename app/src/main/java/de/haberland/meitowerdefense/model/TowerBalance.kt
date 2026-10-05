package de.haberland.meitowerdefense.model

/**
 * Compatibility facade for tower level calculations. Numeric values live in [GameBalance].
 */
object TowerBalance {
    const val MAX_LEVEL = GameBalance.TowerLevels.MAX_LEVEL
    const val SPECIALIZATION_LEVEL = GameBalance.TowerLevels.SPECIALIZATION_LEVEL

    fun levelMultiplier(level: Int): Float {
        require(level in 1..MAX_LEVEL) { "level must be 1..$MAX_LEVEL, was $level" }
        return GameBalance.TowerLevels.levelMultiplier[level]
    }

    fun upgradeCost(type: TowerType, currentLevel: Int): Int? {
        if (currentLevel >= MAX_LEVEL) return null
        return (type.baseCost * GameBalance.TowerLevels.upgradeCostMultiplier[currentLevel]).toInt()
    }

    fun isSpecializationChoice(currentLevel: Int): Boolean =
        currentLevel == SPECIALIZATION_LEVEL
}
