package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2

/**
 * A fired shot in flight. Carries a snapshot of the firing tower's stats at launch time
 * (damage, splash, status effects) rather than a reference back to the tower, so a tower
 * being sold/upgraded mid-flight can't retroactively change a shot that's already on its
 * way - and so this stays a plain, storable value with no back-reference to mutate.
 */
data class Projectile(
    val id: String,
    val position: Vec2,
    val targetEnemyId: String,
    val speed: Float,
    val damage: Float,
    val armorPierce: Int,
    val splashRadius: Float,
    val slowFactor: Float,
    val slowDuration: Float,
    val freezeChance: Float,
    val burnDps: Float,
    val burnDuration: Float,
    val sourceTowerType: TowerType
)
