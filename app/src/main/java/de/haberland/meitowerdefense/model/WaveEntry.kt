package de.haberland.meitowerdefense.model

/**
 * One spawn batch within a level's wave list: [count] enemies of [enemyType], spawned
 * [spawnIntervalSeconds] apart. A "wave" in the level-select/HUD sense is just one
 * WaveEntry; a level's waves list is played back in order.
 */
data class WaveEntry(
    val enemyType: EnemyType,
    val count: Int,
    val spawnIntervalSeconds: Float,
    val hpMultiplier: Float = 1f
) {
    init {
        require(count > 0) { "count must be > 0" }
        require(spawnIntervalSeconds > 0f) { "spawnIntervalSeconds must be > 0" }
        require(hpMultiplier > 0f) { "hpMultiplier must be > 0" }
    }
}
