package de.haberland.meitowerdefense.save

import de.haberland.meitowerdefense.model.MetaUpgradeType
import kotlinx.serialization.Serializable

/** Everything persisted across app launches, as one flat, serializable blob. */
@Serializable
data class SaveData(
    val stars: Int = 0,
    val metaUpgradeLevels: Map<MetaUpgradeType, Int> = emptyMap(),
    val levelProgress: Map<String, LevelProgress> = emptyMap(),
    val endlessBestWave: Int = 0
)

@Serializable
data class LevelProgress(
    val unlocked: Boolean = false,
    val bestStars: Int = 0
)
