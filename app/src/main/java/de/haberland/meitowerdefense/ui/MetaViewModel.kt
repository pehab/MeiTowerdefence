package de.haberland.meitowerdefense.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.LevelDefinition
import de.haberland.meitowerdefense.model.LevelRating
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.model.MetaUpgradeType
import de.haberland.meitowerdefense.save.LevelProgress
import de.haberland.meitowerdefense.save.SaveData
import de.haberland.meitowerdefense.save.SaveRepository

/**
 * Holds account-wide state (stars/meta-upgrades, per-level unlock/best-stars, endless
 * best wave) and persists it via [repo] after every change. A plain ViewModel (not
 * AndroidViewModel) with [repo] injected - same reasoning as MeiOCRWorkout's
 * AppViewModel: keeps this 100% testable with a FakeSaveRepository, no Context needed
 * in the class itself. The real [de.haberland.meitowerdefense.save.FileSaveRepository]
 * is constructed once, where a real Context is available, and handed in.
 *
 * The first level is always unlocked; level N+1 unlocks the moment level N is beaten
 * (LevelRating.starsFor > 0), independent of how many stars were earned.
 */
class MetaViewModel(private val repo: SaveRepository) : ViewModel() {

    var meta by mutableStateOf(MetaProgress())
        private set

    var levelProgress by mutableStateOf<Map<String, LevelProgress>>(emptyMap())
        private set

    var endlessBestWave by mutableStateOf(0)
        private set

    init {
        reload()
    }

    fun reload() {
        val data = repo.load()
        meta = MetaProgress(stars = data.stars, upgradeLevels = data.metaUpgradeLevels)
        levelProgress = ensureFirstLevelUnlocked(data.levelProgress)
        endlessBestWave = data.endlessBestWave
        persist()
    }

    private fun ensureFirstLevelUnlocked(progress: Map<String, LevelProgress>): Map<String, LevelProgress> {
        val firstId = LevelCatalog.all.firstOrNull()?.id ?: return progress
        val current = progress[firstId] ?: LevelProgress()
        if (current.unlocked) return progress
        return progress + (firstId to current.copy(unlocked = true))
    }

    fun isUnlocked(level: LevelDefinition): Boolean = levelProgress[level.id]?.unlocked == true

    fun bestStars(level: LevelDefinition): Int = levelProgress[level.id]?.bestStars ?: 0

    fun purchaseUpgrade(type: MetaUpgradeType) {
        meta = meta.purchase(type)
        persist()
    }

    fun resetUpgrade(type: MetaUpgradeType) {
        meta = meta.resetUpgrade(type)
        persist()
    }

    /** Records the outcome of a finished level: updates best stars, unlocks the next level, awards stars. */
    fun recordLevelResult(level: LevelDefinition, remainingLives: Int, won: Boolean) {
        val stars = LevelRating.starsFor(level.startingLives, remainingLives, won)
        val current = levelProgress[level.id] ?: LevelProgress()
        var updatedProgress = levelProgress + (level.id to current.copy(
            unlocked = true,
            bestStars = maxOf(current.bestStars, stars)
        ))

        if (stars > 0) {
            val index = LevelCatalog.all.indexOfFirst { it.id == level.id }
            val next = LevelCatalog.all.getOrNull(index + 1)
            if (next != null) {
                val nextCurrent = updatedProgress[next.id] ?: LevelProgress()
                if (!nextCurrent.unlocked) {
                    updatedProgress = updatedProgress + (next.id to nextCurrent.copy(unlocked = true))
                }
            }
        }

        levelProgress = updatedProgress
        // Award only the improvement over the previous best - replaying a level you
        // already 3-starred at 1 star again shouldn't pay out, and improving from 1 to
        // 3 stars should pay the 2-star difference, not another full 3.
        val starsToAward = (stars - current.bestStars).coerceAtLeast(0)
        if (starsToAward > 0) {
            meta = meta.addStars(starsToAward)
        }
        persist()
    }

    fun recordEndlessResult(waveReached: Int) {
        if (waveReached > endlessBestWave) {
            endlessBestWave = waveReached
            persist()
        }
    }

    private fun persist() {
        repo.save(
            SaveData(
                stars = meta.stars,
                metaUpgradeLevels = meta.upgradeLevels,
                levelProgress = levelProgress,
                endlessBestWave = endlessBestWave
            )
        )
    }
}
