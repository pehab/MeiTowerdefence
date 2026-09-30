package de.haberland.meitowerdefense.save

import de.haberland.meitowerdefense.model.MetaUpgradeType
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType
import kotlinx.serialization.Serializable

/** Everything persisted across app launches, as one flat, serializable blob. */
@Serializable
data class SaveData(
    val stars: Int = 0,
    val metaUpgradeLevels: Map<MetaUpgradeType, Int> = emptyMap(),
    val levelProgress: Map<String, LevelProgress> = emptyMap(),
    val endlessBestWave: Int = 0,
    val endlessBestCompletedWaves: Int = 0,
    val killsByEnemy: Map<EnemyType, Int> = emptyMap(),
    val killsByTower: Map<TowerType, Int> = emptyMap(),
    val claimedAchievements: Set<String> = emptySet(),
    val leaderboardName: String = ""
)

@Serializable
data class LevelProgress(
    val unlocked: Boolean = false,
    val bestStars: Int = 0
)
