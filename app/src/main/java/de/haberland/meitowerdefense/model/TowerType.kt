package de.haberland.meitowerdefense.model

/**
 * Tower identity and presentation name. All numeric gameplay values live in [GameBalance].
 */
enum class TowerType(val displayName: String) {
    ARCHER("Bogenschütze"),
    CANNON("Kanone"),
    FIRE("Feuer"),
    ICE("Eis");

    private val balance get() = GameBalance.tower(this)

    val baseCost: Int get() = balance.baseCost
    val baseDamage: Float get() = balance.baseDamage
    val baseRange: Float get() = balance.baseRange
    val baseFireRate: Float get() = balance.baseFireRate
    val baseSplashRadius: Float get() = balance.baseSplashRadius
    val canHitFlying: Boolean get() = balance.canHitFlying
    val burnDamagePerSecond: Float get() = balance.burnDamagePerSecond
    val burnDurationSeconds: Float get() = balance.burnDurationSeconds
    val slowFactor: Float get() = balance.slowFactor
    val slowDurationSeconds: Float get() = balance.slowDurationSeconds
}
