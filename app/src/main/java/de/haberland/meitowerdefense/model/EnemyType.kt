package de.haberland.meitowerdefense.model

/**
 * Base stats per enemy type. Speed is in grid cells/second along the level path.
 * [flying] enemies take a level's separate, more direct air path (see LevelDefinition)
 * and can only be targeted by tower types with [TowerType.canHitFlying] set - the
 * intended source of real tactical pressure from air waves, not just a different icon.
 */
enum class EnemyType(
    val displayName: String,
    val baseHp: Int,
    val baseSpeed: Float,
    val armor: Int,
    val goldReward: Int,
    val flying: Boolean
) {
    BASIC(displayName = "Einfach", baseHp = 40, baseSpeed = 1.4f, armor = 0, goldReward = 5, flying = false),
    FAST(displayName = "Schnell", baseHp = 25, baseSpeed = 2.4f, armor = 0, goldReward = 6, flying = false),
    ARMORED(displayName = "Gepanzert", baseHp = 110, baseSpeed = 0.9f, armor = 5, goldReward = 10, flying = false),
    FLYING(displayName = "Fliegend", baseHp = 35, baseSpeed = 1.6f, armor = 0, goldReward = 8, flying = true),
    BOSS(displayName = "Boss", baseHp = 900, baseSpeed = 0.7f, armor = 10, goldReward = 120, flying = false)
}
