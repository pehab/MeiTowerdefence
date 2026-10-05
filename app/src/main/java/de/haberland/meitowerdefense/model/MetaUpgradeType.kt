package de.haberland.meitowerdefense.model

/** Permanent account-wide upgrades. Numeric balance values live in [GameBalance]. */
enum class MetaUpgradeType(
    val displayName: String,
    val description: String
) {
    GOLD_INCOME(
        "Goldader",
        "+${GameBalance.Meta.GOLD_INCOME_PERCENT_PER_LEVEL}% Gold pro besiegtem Gegner je Stufe"
    ),
    STARTING_GOLD(
        "Startkapital",
        "+${GameBalance.Meta.STARTING_GOLD_PER_LEVEL} Startgold je Stufe"
    ),
    STARTING_LIVES(
        "Grundmauern",
        "+${GameBalance.Meta.STARTING_LIVES_PER_LEVEL} Leben je Stufe"
    ),
    FIRE_SPLASH_RADIUS(
        "Flammenmeister",
        "Alle Feuer-Türme: +${GameBalance.Meta.FIRE_SPLASH_RADIUS_PER_LEVEL} Splash-Radius je Stufe"
    ),
    ICE_SLOW_DURATION(
        "Ewiger Frost",
        "Alle Eis-Türme: +${GameBalance.Meta.ICE_SLOW_DURATION_PER_LEVEL} s Verlangsamungs-/Freeze-Dauer je Stufe"
    ),
    ARCHER_DAMAGE(
        "Geschärfte Pfeile",
        "Alle Bogenschützen-Türme: +${GameBalance.Meta.ARCHER_DAMAGE_PERCENT_PER_LEVEL}% Schaden je Stufe"
    );

    private val balance get() = GameBalance.metaUpgrade(this)
    val maxLevel: Int get() = balance.maxLevel
    val baseStarCost: Int get() = balance.baseStarCost

    fun costForNextLevel(currentLevel: Int): Int? {
        if (currentLevel >= maxLevel) return null
        return baseStarCost * (currentLevel + 1)
    }
}
