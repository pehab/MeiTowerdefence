package de.haberland.meitowerdefense.model

/**
 * Enemy identity and presentation name. All numeric gameplay values live in [GameBalance].
 */
enum class EnemyType(val displayName: String) {
    BASIC("Einfach"),
    FAST("Schnell"),
    ARMORED("Gepanzert"),
    FLYING("Fliegend"),
    BOSS("Boss");

    private val balance get() = GameBalance.enemy(this)

    val baseHp: Int get() = balance.baseHp
    val baseSpeed: Float get() = balance.baseSpeed
    val armor: Int get() = balance.armor
    val goldReward: Int get() = balance.goldReward
    val flying: Boolean get() = balance.flying
    val livesCost: Int get() = balance.livesCost
}
