package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.WaveEntry
import de.haberland.meitowerdefense.model.WaveGroup

/**
 * Deterministic endless-wave generator. The same wave number always produces the same
 * composition, which keeps runs comparable and makes the high score meaningful.
 *
 * Enemy health grows slightly faster than linear in the opening waves and increasingly
 * faster later on. This keeps the mode approachable at first while preventing fully
 * upgraded defenses from coasting through the high double-digit waves.
 */
object EndlessWaves {

    fun wave(index: Int): WaveEntry {
        require(index >= 0) { "index must be >= 0" }
        val number = index + 1
        val hp = hpMultiplier(index)
        val count = 6 + (index * 0.7f).toInt()

        if (number % 10 == 0) {
            return WaveEntry(
                listOf(
                    WaveGroup(
                        enemyType = EnemyType.BOSS,
                        count = 1 + index / 30,
                        spawnIntervalSeconds = 3f,
                        hpMultiplier = hp * 1.1f
                    ),
                    WaveGroup(
                        enemyType = EnemyType.FAST,
                        count = 6 + index / 2,
                        spawnIntervalSeconds = 0.45f,
                        hpMultiplier = hp,
                        startDelaySeconds = 1.5f
                    ),
                    WaveGroup(
                        enemyType = EnemyType.FLYING,
                        count = 4 + index / 3,
                        spawnIntervalSeconds = 0.6f,
                        hpMultiplier = hp,
                        startDelaySeconds = 3f
                    )
                )
            )
        }

        val groups = mutableListOf(
            WaveGroup(
                enemyType = when {
                    number < 4 -> EnemyType.BASIC
                    number % 3 == 0 -> EnemyType.ARMORED
                    number % 2 == 0 -> EnemyType.FAST
                    else -> EnemyType.BASIC
                },
                count = count,
                spawnIntervalSeconds = (0.85f - index * 0.012f).coerceAtLeast(0.28f),
                hpMultiplier = hp
            )
        )

        if (number >= 5) {
            groups += WaveGroup(
                enemyType = EnemyType.FLYING,
                count = 3 + index / 4,
                spawnIntervalSeconds = 0.7f.coerceAtLeast(0.35f),
                hpMultiplier = hp * 0.9f,
                startDelaySeconds = 2f
            )
        }

        if (number >= 8 && number % 2 == 0) {
            groups += WaveGroup(
                enemyType = EnemyType.FAST,
                count = 4 + index / 5,
                spawnIntervalSeconds = 0.38f,
                hpMultiplier = hp,
                startDelaySeconds = 1f
            )
        }

        return WaveEntry(groups)
    }

    internal fun hpMultiplier(index: Int): Float {
        require(index >= 0) { "index must be >= 0" }
        return 1f + index * 0.115f + index * index * 0.0015f
    }
}
