package de.haberland.meitowerdefense.sim

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType

/**
 * Lightweight per-run telemetry shown on the result screen. Keeping it inside the
 * immutable GameSession makes it deterministic and naturally testable with the rest of
 * the simulation.
 */
data class RunStats(
    val enemiesKilled: Int = 0,
    val goldEarned: Int = 0,
    val towersBuilt: Int = 0,
    val towersUpgraded: Int = 0,
    val towersSold: Int = 0,
    val livesLost: Int = 0,
    val killsByEnemy: Map<EnemyType, Int> = emptyMap(),
    val killsByTower: Map<TowerType, Int> = emptyMap()
)
