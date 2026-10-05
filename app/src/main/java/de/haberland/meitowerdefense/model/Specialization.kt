package de.haberland.meitowerdefense.model

/**
 * Tower specialization identity. Numeric effects are defined exclusively in [GameBalance].
 */
enum class Specialization(
    val displayName: String,
    val towerType: TowerType
) {
    ARCHER_SNIPER("Scharfschütze", TowerType.ARCHER),
    ARCHER_RAPID("Dauerfeuer", TowerType.ARCHER),
    CANNON_SIEGE("Belagerung", TowerType.CANNON),
    CANNON_MORTAR("Mörser", TowerType.CANNON),
    FIRE_INFERNO("Inferno", TowerType.FIRE),
    FIRE_SCORCH("Sengend", TowerType.FIRE),
    ICE_DEEP_FREEZE("Tiefkühlung", TowerType.ICE),
    ICE_FROSTBITE("Frostbiss", TowerType.ICE);

    private val balance get() = GameBalance.specialization(this)

    val damageMultiplier: Float get() = balance.damageMultiplier
    val fireRateMultiplier: Float get() = balance.fireRateMultiplier
    val rangeMultiplier: Float get() = balance.rangeMultiplier
    val splashRadiusBonus: Float get() = balance.splashRadiusBonus
    val armorPierce: Int get() = balance.armorPierce
    val extraTargetChance: Float get() = balance.extraTargetChance
    val freezeChanceBonus: Float get() = balance.freezeChanceBonus
    val slowDurationBonus: Float get() = balance.slowDurationBonus
    val slowFactorBonus: Float get() = balance.slowFactorBonus
    val burnDamageBonus: Float get() = balance.burnDamageBonus
    val burnDurationBonus: Float get() = balance.burnDurationBonus
    val splashOnHitBonus: Float get() = balance.splashOnHitBonus

    companion object {
        fun branchesFor(type: TowerType): List<Specialization> = entries.filter { it.towerType == type }
    }
}
