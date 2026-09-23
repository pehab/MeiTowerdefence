package de.haberland.meitowerdefense.model

/**
 * Base stats per tower type, before per-level scaling ([TowerBalance]) or a chosen
 * [Specialization] are applied. Range/splashRadius are in grid cells, fireRate in
 * shots/second.
 *
 * canHitFlying is deliberately false for CANNON and FIRE (lobbed cannonballs and ground
 * fire can't reach the sky) - the point of that split, together with EnemyType.FLYING's
 * separate air path, is to force a real choice: an all-cannon/fire build has no answer
 * to a flying wave.
 */
enum class TowerType(
    val displayName: String,
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
) {
    ARCHER(
        displayName = "Bogenschütze", baseCost = 50, baseDamage = 8f, baseRange = 3.2f,
        baseFireRate = 2.0f, baseSplashRadius = 0f, canHitFlying = true
    ),
    CANNON(
        displayName = "Kanone", baseCost = 90, baseDamage = 20f, baseRange = 2.6f,
        baseFireRate = 0.7f, baseSplashRadius = 1.0f, canHitFlying = false
    ),
    FIRE(
        displayName = "Feuer", baseCost = 70, baseDamage = 5f, baseRange = 2.4f,
        baseFireRate = 1.2f, baseSplashRadius = 0f, canHitFlying = false,
        burnDamagePerSecond = 6f, burnDurationSeconds = 3f
    ),
    ICE(
        displayName = "Eis", baseCost = 60, baseDamage = 3f, baseRange = 2.6f,
        baseFireRate = 1.5f, baseSplashRadius = 0f, canHitFlying = true,
        slowFactor = 0.4f, slowDurationSeconds = 2f
    )
}
