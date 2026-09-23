package de.haberland.meitowerdefense.model

/**
 * Permanent, account-wide upgrades bought with stars (earned from level star-ratings,
 * see LevelResult). These stack on top of whatever tower level/specialization a player
 * has built in a given level - e.g. FIRE_SPLASH_RADIUS applies to every Fire tower
 * regardless of its chosen Specialization branch.
 */
enum class MetaUpgradeType(
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val baseStarCost: Int
) {
    GOLD_INCOME(
        displayName = "Goldader",
        description = "+8% Gold pro Kill und Welle je Stufe",
        maxLevel = 5,
        baseStarCost = 2
    ),
    STARTING_GOLD(
        displayName = "Startkapital",
        description = "+20 Startgold je Stufe",
        maxLevel = 5,
        baseStarCost = 2
    ),
    STARTING_LIVES(
        displayName = "Grundmauern",
        description = "+2 Leben je Stufe",
        maxLevel = 5,
        baseStarCost = 3
    ),
    FIRE_SPLASH_RADIUS(
        displayName = "Flammenmeister",
        description = "Alle Feuer-Türme erhalten zusätzlichen Splash-Radius je Stufe",
        maxLevel = 3,
        baseStarCost = 4
    ),
    ICE_SLOW_DURATION(
        displayName = "Ewiger Frost",
        description = "Alle Eis-Türme: längere Verlangsamungs-/Freeze-Dauer je Stufe",
        maxLevel = 3,
        baseStarCost = 4
    ),
    ARCHER_DAMAGE(
        displayName = "Geschärfte Pfeile",
        description = "Alle Bogenschützen-Türme erhalten mehr Schaden je Stufe",
        maxLevel = 3,
        baseStarCost = 4
    );

    /** Star cost to go from [currentLevel] to currentLevel+1, or null if already maxed. */
    fun costForNextLevel(currentLevel: Int): Int? {
        if (currentLevel >= maxLevel) return null
        return baseStarCost * (currentLevel + 1)
    }
}
