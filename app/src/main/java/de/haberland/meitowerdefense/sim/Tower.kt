package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerBalance
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.Vec2

/**
 * One placed tower. Combat stats are computed properties/functions rather than stored
 * fields, each folding together three layers: [TowerType] base stats, [TowerBalance]'s
 * per-level multiplier, and the chosen [specialization]'s bonuses. Functions that also
 * take a [MetaProgress] additionally fold in the matching permanent star-shop bonus
 * (Fire splash, Ice slow duration, Archer damage) - meta progress is account-wide state
 * the tower doesn't own, so it's passed in rather than stored.
 */
data class Tower(
    val id: String,
    val type: TowerType,
    val gridPos: GridPos,
    val level: Int = 1,
    val specialization: Specialization? = null,
    val cooldownRemaining: Float = 0f
) {
    val position: Vec2 get() = gridPos.toVec2()

    val range: Float get() = type.baseRange * (specialization?.rangeMultiplier ?: 1f)

    fun damage(meta: MetaProgress): Float {
        val base = type.baseDamage * TowerBalance.levelMultiplier(level) * (specialization?.damageMultiplier ?: 1f)
        return if (type == TowerType.ARCHER) base * (1f + meta.archerDamageMultiplierBonus) else base
    }

    val fireRate: Float get() = type.baseFireRate * (specialization?.fireRateMultiplier ?: 1f)

    fun splashRadius(meta: MetaProgress): Float {
        var r = type.baseSplashRadius + (specialization?.splashRadiusBonus ?: 0f) + (specialization?.splashOnHitBonus ?: 0f)
        if (type == TowerType.FIRE) r += meta.fireSplashRadiusBonus
        return r
    }

    val armorPierce: Int get() = specialization?.armorPierce ?: 0

    val slowFactor: Float get() = (type.slowFactor + (specialization?.slowFactorBonus ?: 0f)).coerceIn(0f, 0.95f)

    fun slowDuration(meta: MetaProgress): Float {
        val base = type.slowDurationSeconds + (specialization?.slowDurationBonus ?: 0f)
        return if (type == TowerType.ICE) base + meta.iceSlowDurationBonus else base
    }

    val freezeChance: Float get() = specialization?.freezeChanceBonus ?: 0f
    val burnDps: Float get() = type.burnDamagePerSecond + (specialization?.burnDamageBonus ?: 0f)
    val burnDuration: Float get() = type.burnDurationSeconds + (specialization?.burnDurationBonus ?: 0f)
    val extraTargetChance: Float get() = specialization?.extraTargetChance ?: 0f

    val canUpgrade: Boolean get() = level < TowerBalance.MAX_LEVEL
    val needsSpecializationChoice: Boolean get() = level == TowerBalance.SPECIALIZATION_LEVEL && specialization == null
    fun upgradeCost(): Int? = TowerBalance.upgradeCost(type, level)
}
