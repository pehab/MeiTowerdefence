package de.haberland.meitowerdefense.model

/**
 * One gameplay wave can contain multiple spawn groups. Groups use absolute start delays,
 * so they can overlap: e.g. armored enemies can start at t=0 while fast enemies begin
 * two seconds later. This keeps level authoring compact while allowing genuinely mixed
 * waves instead of forcing one enemy type per wave.
 */
data class WaveGroup(
    val enemyType: EnemyType,
    val count: Int,
    val spawnIntervalSeconds: Float,
    val hpMultiplier: Float = 1f,
    val startDelaySeconds: Float = 0f
) {
    init {
        require(count > 0) { "count must be > 0" }
        require(spawnIntervalSeconds > 0f) { "spawnIntervalSeconds must be > 0" }
        require(hpMultiplier > 0f) { "hpMultiplier must be > 0" }
        require(startDelaySeconds >= 0f) { "startDelaySeconds must be >= 0" }
    }
}

data class ScheduledSpawn(
    val atSeconds: Float,
    val enemyType: EnemyType,
    val hpMultiplier: Float
)

data class WaveEntry(val groups: List<WaveGroup>) {
    constructor(
        enemyType: EnemyType,
        count: Int,
        spawnIntervalSeconds: Float,
        hpMultiplier: Float = 1f
    ) : this(listOf(WaveGroup(enemyType, count, spawnIntervalSeconds, hpMultiplier)))

    init {
        require(groups.isNotEmpty()) { "wave needs at least one spawn group" }
    }

    /** Stable schedule used by the deterministic simulator. */
    fun schedule(): List<ScheduledSpawn> =
        groups.flatMapIndexed { groupIndex, group ->
            List(group.count) { enemyIndex ->
                ScheduledSpawn(
                    atSeconds = group.startDelaySeconds + enemyIndex * group.spawnIntervalSeconds,
                    enemyType = group.enemyType,
                    hpMultiplier = group.hpMultiplier
                ) to groupIndex
            }
        }
            .sortedWith(compareBy<Pair<ScheduledSpawn, Int>> { it.first.atSeconds }.thenBy { it.second })
            .map { it.first }

    val totalEnemyCount: Int get() = groups.sumOf { it.count }

    // Backward-compatible conveniences for old tests/callers that use single-group waves.
    val enemyType: EnemyType get() = groups.first().enemyType
    val count: Int get() = groups.sumOf { it.count }
    val spawnIntervalSeconds: Float get() = groups.first().spawnIntervalSeconds
    val hpMultiplier: Float get() = groups.first().hpMultiplier
}
