package de.haberland.meitowerdefense.content

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.GameBalance
import de.haberland.meitowerdefense.model.WaveEntry
import de.haberland.meitowerdefense.model.WaveGroup

/** Deterministic endless-wave generator. Numeric tuning lives in [GameBalance.Endless]. */
object EndlessWaves {

    fun wave(index: Int): WaveEntry {
        require(index >= 0) { "index must be >= 0" }
        val b = GameBalance.Endless
        val number = index + 1
        val hp = hpMultiplier(index)
        val count = b.BASE_COUNT + (index * b.COUNT_GROWTH_PER_WAVE).toInt()

        if (number % b.BOSS_EVERY_N_WAVES == 0) {
            return WaveEntry(
                listOf(
                    WaveGroup(
                        enemyType = EnemyType.BOSS,
                        count = b.BOSS_BASE_COUNT + index / b.BOSS_EXTRA_EVERY_WAVES,
                        spawnIntervalSeconds = b.BOSS_SPAWN_INTERVAL,
                        hpMultiplier = hp * b.BOSS_HP_MULTIPLIER
                    ),
                    WaveGroup(
                        enemyType = EnemyType.FAST,
                        count = b.BOSS_FAST_BASE_COUNT + index / b.BOSS_FAST_COUNT_DIVISOR,
                        spawnIntervalSeconds = b.BOSS_FAST_SPAWN_INTERVAL,
                        hpMultiplier = hp,
                        startDelaySeconds = b.BOSS_FAST_START_DELAY
                    ),
                    WaveGroup(
                        enemyType = EnemyType.FLYING,
                        count = b.BOSS_FLYING_BASE_COUNT + index / b.BOSS_FLYING_COUNT_DIVISOR,
                        spawnIntervalSeconds = b.BOSS_FLYING_SPAWN_INTERVAL,
                        hpMultiplier = hp,
                        startDelaySeconds = b.BOSS_FLYING_START_DELAY
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
                spawnIntervalSeconds = (b.MAIN_SPAWN_INTERVAL_START - index * b.MAIN_SPAWN_INTERVAL_DECAY)
                    .coerceAtLeast(b.MAIN_SPAWN_INTERVAL_MIN),
                hpMultiplier = hp
            )
        )

        if (number >= b.FLYING_START_WAVE) {
            groups += WaveGroup(
                enemyType = EnemyType.FLYING,
                count = b.FLYING_BASE_COUNT + index / b.FLYING_COUNT_DIVISOR,
                spawnIntervalSeconds = b.FLYING_SPAWN_INTERVAL,
                hpMultiplier = hp * b.FLYING_HP_MULTIPLIER,
                startDelaySeconds = b.FLYING_START_DELAY
            )
        }

        if (number >= b.EXTRA_FAST_START_WAVE && number % 2 == 0) {
            groups += WaveGroup(
                enemyType = EnemyType.FAST,
                count = b.EXTRA_FAST_BASE_COUNT + index / b.EXTRA_FAST_COUNT_DIVISOR,
                spawnIntervalSeconds = b.EXTRA_FAST_SPAWN_INTERVAL,
                hpMultiplier = hp,
                startDelaySeconds = b.EXTRA_FAST_START_DELAY
            )
        }

        return WaveEntry(groups)
    }

    internal fun hpMultiplier(index: Int): Float {
        require(index >= 0) { "index must be >= 0" }
        return GameBalance.Endless.hpMultiplier(index)
    }
}
