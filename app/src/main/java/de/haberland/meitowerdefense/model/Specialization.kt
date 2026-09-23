package de.haberland.meitowerdefense.model

/**
 * Chosen once a tower reaches level 3 (see TowerBalance.SPECIALIZATION_LEVEL), applied on
 * top of the tower's level-scaled base stats for levels 4-5. Each [TowerType] has exactly
 * two branches - [branchesFor] is how the UI discovers which two to offer.
 *
 * All multipliers/bonuses default to "no effect" so each entry only states what it
 * actually changes.
 */
enum class Specialization(
    val displayName: String,
    val towerType: TowerType,
    val damageMultiplier: Float = 1f,
    val fireRateMultiplier: Float = 1f,
    val rangeMultiplier: Float = 1f,
    val splashRadiusBonus: Float = 0f,
    val armorPierce: Int = 0,
    val extraTargetChance: Float = 0f,
    val freezeChanceBonus: Float = 0f,
    val slowDurationBonus: Float = 0f,
    val slowFactorBonus: Float = 0f,
    val burnDamageBonus: Float = 0f,
    val burnDurationBonus: Float = 0f,
    val splashOnHitBonus: Float = 0f
) {
    ARCHER_SNIPER(
        displayName = "Scharfschütze", towerType = TowerType.ARCHER,
        damageMultiplier = 1.8f, fireRateMultiplier = 0.7f, armorPierce = 6
    ),
    ARCHER_RAPID(
        displayName = "Dauerfeuer", towerType = TowerType.ARCHER,
        fireRateMultiplier = 1.7f, extraTargetChance = 0.35f
    ),
    CANNON_SIEGE(
        displayName = "Belagerung", towerType = TowerType.CANNON,
        splashRadiusBonus = 0.6f, armorPierce = 8
    ),
    CANNON_MORTAR(
        displayName = "Mörser", towerType = TowerType.CANNON,
        splashRadiusBonus = 1.2f, rangeMultiplier = 1.3f, damageMultiplier = 0.75f
    ),
    FIRE_INFERNO(
        displayName = "Inferno", towerType = TowerType.FIRE,
        burnDamageBonus = 6f, burnDurationBonus = 2f
    ),
    FIRE_SCORCH(
        displayName = "Sengend", towerType = TowerType.FIRE,
        splashOnHitBonus = 1.0f, damageMultiplier = 1.3f
    ),
    ICE_DEEP_FREEZE(
        displayName = "Tiefkühlung", towerType = TowerType.ICE,
        freezeChanceBonus = 0.25f, slowDurationBonus = 1.5f
    ),
    ICE_FROSTBITE(
        displayName = "Frostbiss", towerType = TowerType.ICE,
        slowFactorBonus = 0.2f, burnDamageBonus = 2f
    );

    companion object {
        fun branchesFor(type: TowerType): List<Specialization> = entries.filter { it.towerType == type }
    }
}
