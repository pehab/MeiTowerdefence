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

    var saveError by mutableStateOf<String?>(null)
        private set

    private var storageReady = false
    private var hasUnsavedChanges = false

    init {
        reload()
    }

    fun reload() {
        // Never replace in-memory progress with an older disk copy after a failed save.
        if (hasUnsavedChanges) {
            persist()
            return
        }
        val data = try {
            repo.load()
        } catch (_: Exception) {
            storageReady = false
            saveError = "Der Spielstand konnte nicht geladen werden. Bitte erneut versuchen. Die gespeicherten Dateien werden nicht überschrieben."
            return
        }
        storageReady = true
        meta = MetaProgress(stars = data.stars, upgradeLevels = data.metaUpgradeLevels)
        levelProgress = ensureCampaignUnlocks(data.levelProgress)
        endlessBestWave = data.endlessBestWave
        persist()
    }

    /** Reconcile unlock order on load without removing earned stars or existing access. */
    private fun ensureCampaignUnlocks(progress: Map<String, LevelProgress>): Map<String, LevelProgress> {
        var updated = progress
        LevelCatalog.all.forEachIndexed { index, level ->
            val previous = LevelCatalog.all.getOrNull(index - 1)
            val previousCleared = previous != null && (progress[previous.id]?.bestStars ?: 0) > 0
            val shouldUnlock = index == 0 || previousCleared
            val current = updated[level.id] ?: LevelProgress()
            if (shouldUnlock && !current.unlocked) {
                updated = updated + (level.id to current.copy(unlocked = true))
            }
        }
        return updated
    }

    fun isUnlocked(level: LevelDefinition): Boolean = levelProgress[level.id]?.unlocked == true

    fun bestStars(level: LevelDefinition): Int = levelProgress[level.id]?.bestStars ?: 0

    fun purchaseUpgrade(type: MetaUpgradeType) {
        if (!storageReady) return
        meta = meta.purchase(type)
        persist()
    }

    fun resetUpgrade(type: MetaUpgradeType) {
        if (!storageReady) return
        meta = meta.resetUpgrade(type)
        persist()
    }

    /** Records the outcome of a finished level: updates best stars, unlocks the next level, awards stars. */
    fun recordLevelResult(level: LevelDefinition, remainingLives: Int, won: Boolean) {
        if (!storageReady) return
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
        if (!storageReady) return
        if (waveReached > endlessBestWave) {
            endlessBestWave = waveReached
            persist()
        }
    }

    fun dismissSaveError() {
        saveError = null
    }

    fun retrySave(): Boolean {
        if (storageReady) persist() else reload()
        return storageReady && !hasUnsavedChanges
    }

    /** Called before leaving a result screen or opening another gameplay activity. */
    fun ensureSaved(): Boolean = if (storageReady && !hasUnsavedChanges) true else retrySave()

    private fun persist() {
        if (!storageReady) return
        hasUnsavedChanges = true
        try {
            repo.save(
                SaveData(
                    stars = meta.stars,
                    metaUpgradeLevels = meta.upgradeLevels,
                    levelProgress = levelProgress,
                    endlessBestWave = endlessBestWave
                )
            )
            hasUnsavedChanges = false
            saveError = null
        } catch (_: Exception) {
            saveError = "Dein Fortschritt ist noch nicht gespeichert. Bitte erneut versuchen, bevor du das Spiel verlässt."
        }
    }
}
