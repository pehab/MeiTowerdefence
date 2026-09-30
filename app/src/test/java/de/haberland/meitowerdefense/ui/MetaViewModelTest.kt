package de.haberland.meitowerdefense.ui

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.MetaUpgradeType
import de.haberland.meitowerdefense.save.LevelProgress
import de.haberland.meitowerdefense.save.FakeSaveRepository
import de.haberland.meitowerdefense.save.SaveRepository
import java.io.IOException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import de.haberland.meitowerdefense.save.SaveData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetaViewModelTest {

    private val firstLevel = LevelCatalog.all[0]
    private val secondLevel = LevelCatalog.all[1]

    @Test
    fun theFirstLevelStartsUnlockedOnAFreshSave() {
        val vm = MetaViewModel(FakeSaveRepository())
        assertTrue(vm.isUnlocked(firstLevel))
        assertFalse(vm.isUnlocked(secondLevel))
    }

    @Test
    fun clearingALevelAwardsStarsEqualToTheRatingAndUnlocksTheNextLevel() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordLevelResult(firstLevel, remainingLives = firstLevel.startingLives, won = true) // perfect clear -> 3 stars
        assertEquals(3, vm.meta.stars)
        assertEquals(3, vm.bestStars(firstLevel))
        assertTrue(vm.isUnlocked(secondLevel))
    }

    @Test
    fun losingALevelAwardsNoStarsAndDoesNotUnlockTheNextOne() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordLevelResult(firstLevel, remainingLives = 0, won = false)
        assertEquals(0, vm.meta.stars)
        assertEquals(0, vm.bestStars(firstLevel))
        assertFalse(vm.isUnlocked(secondLevel))
    }

    @Test
    fun replayingWithTheSameOrWorseRatingAwardsNoAdditionalStars() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordLevelResult(firstLevel, remainingLives = firstLevel.startingLives, won = true) // 3 stars
        val starsAfterFirstClear = vm.meta.stars
        vm.recordLevelResult(firstLevel, remainingLives = 1, won = true) // worse clear, 1 star
        assertEquals(starsAfterFirstClear, vm.meta.stars)
        assertEquals(3, vm.bestStars(firstLevel)) // best stays at the previous best
    }

    @Test
    fun improvingAPreviousClearAwardsOnlyTheDifference() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordLevelResult(firstLevel, remainingLives = 1, won = true) // 1 star
        assertEquals(1, vm.meta.stars)
        vm.recordLevelResult(firstLevel, remainingLives = firstLevel.startingLives, won = true) // improves to 3 stars
        assertEquals(3, vm.meta.stars) // 1 (already had) + 2 (the improvement), not 1 + 3
        assertEquals(3, vm.bestStars(firstLevel))
    }

    @Test
    fun purchasingAnUpgradeSpendsStarsAndPersists() {
        val repo = FakeSaveRepository(SaveData(stars = 10))
        val vm = MetaViewModel(repo)
        val cost = MetaUpgradeType.GOLD_INCOME.costForNextLevel(0)!!
        vm.purchaseUpgrade(MetaUpgradeType.GOLD_INCOME)
        assertEquals(10 - cost, vm.meta.stars)
        assertEquals(1, vm.meta.levelOf(MetaUpgradeType.GOLD_INCOME))
        assertEquals(vm.meta.stars, repo.load().stars)
    }

    @Test
    fun resettingAnUpgradeRefundsStarsAndPersists() {
        val repo = FakeSaveRepository(SaveData(stars = 100))
        val vm = MetaViewModel(repo)
        vm.purchaseUpgrade(MetaUpgradeType.STARTING_LIVES)
        vm.purchaseUpgrade(MetaUpgradeType.STARTING_LIVES)
        val starsBeforeReset = vm.meta.stars

        vm.resetUpgrade(MetaUpgradeType.STARTING_LIVES)

        assertEquals(0, vm.meta.levelOf(MetaUpgradeType.STARTING_LIVES))
        assertEquals(100, vm.meta.stars) // fully refunded back to the starting amount
        assertTrue(vm.meta.stars > starsBeforeReset)
        assertEquals(vm.meta.stars, repo.load().stars)
    }

    @Test
    fun recordEndlessResultOnlyKeepsTheBestWaveReached() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordEndlessResult(5)
        assertEquals(5, vm.endlessBestWave)
        vm.recordEndlessResult(3) // worse - ignored
        assertEquals(5, vm.endlessBestWave)
        vm.recordEndlessResult(9) // better - kept
        assertEquals(9, vm.endlessBestWave)
    }

    @Test
    fun stateSurvivesAReloadFromTheSameUnderlyingSave() {
        val repo = FakeSaveRepository()
        val first = MetaViewModel(repo)
        first.recordLevelResult(firstLevel, remainingLives = firstLevel.startingLives, won = true)
        first.purchaseUpgrade(MetaUpgradeType.STARTING_GOLD)

        val second = MetaViewModel(repo) // simulates relaunching the app against the same save
        assertEquals(first.meta.stars, second.meta.stars)
        assertEquals(first.meta.upgradeLevels, second.meta.upgradeLevels)
        assertTrue(second.isUnlocked(secondLevel))
    }
    private class UnreliableRepository(var data: SaveData = SaveData()) : SaveRepository {
        var failLoad = false
        var failSave = false
        var saveCalls = 0
        override fun load(): SaveData {
            if (failLoad) throw IOException("unreadable")
            return data
        }
        override fun save(data: SaveData) {
            saveCalls++
            if (failSave) throw IOException("disk full")
            this.data = data
        }
    }

    @Test
    fun failedSaveKeepsEarnedProgressAcrossReloadUntilRetrySucceeds() {
        val repo = UnreliableRepository()
        val vm = MetaViewModel(repo)
        repo.failSave = true
        vm.recordLevelResult(firstLevel, firstLevel.startingLives, won = true)
        assertEquals(3, vm.meta.stars)
        assertNotNull(vm.saveError)
        vm.reload()
        assertEquals(3, vm.meta.stars)
        assertEquals(3, vm.bestStars(firstLevel))
        assertFalse(vm.ensureSaved())

        repo.failSave = false
        assertTrue(vm.retrySave())
        assertNull(vm.saveError)
        assertEquals(3, MetaViewModel(repo).meta.stars)
    }

    @Test
    fun failedLoadNeverWritesAnEmptyReplacementAndCanBeRetried() {
        val repo = UnreliableRepository(SaveData(stars = 12))
        repo.failLoad = true
        val vm = MetaViewModel(repo)
        vm.recordLevelResult(firstLevel, firstLevel.startingLives, won = true)
        assertEquals(0, repo.saveCalls)
        assertFalse(vm.ensureSaved())
        assertEquals(12, repo.data.stars)

        repo.failLoad = false
        assertTrue(vm.retrySave())
        assertEquals(12, vm.meta.stars)
        assertNull(vm.saveError)
    }
    @Test
    fun clearingRiverbankUnlocksCrossroadsThenSerpentinesThenFortress() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordLevelResult(LevelCatalog.riverbank, 18, won = true)
        assertTrue(vm.isUnlocked(LevelCatalog.crossroads))
        assertFalse(vm.isUnlocked(LevelCatalog.serpentines))
        vm.recordLevelResult(LevelCatalog.crossroads, 18, won = true)
        assertTrue(vm.isUnlocked(LevelCatalog.serpentines))
        assertFalse(vm.isUnlocked(LevelCatalog.fortress))
        vm.recordLevelResult(LevelCatalog.serpentines, 18, won = true)
        assertTrue(vm.isUnlocked(LevelCatalog.fortress))
    }

    @Test
    fun reorderingPreservesOldUnlocksAndRatingsAndOpensTheNewSuccessor() {
        val repo = FakeSaveRepository(SaveData(stars = 7, levelProgress = mapOf(
            LevelCatalog.riverbank.id to LevelProgress(true, 3),
            LevelCatalog.serpentines.id to LevelProgress(true, 2),
            LevelCatalog.crossroads.id to LevelProgress(true, 0)
        )))
        val vm = MetaViewModel(repo)
        assertTrue(vm.isUnlocked(LevelCatalog.crossroads))
        assertTrue(vm.isUnlocked(LevelCatalog.serpentines))
        assertTrue(vm.isUnlocked(LevelCatalog.fortress))
        assertEquals(2, vm.bestStars(LevelCatalog.serpentines))
        assertEquals(0, vm.bestStars(LevelCatalog.crossroads))
        assertEquals(7, vm.meta.stars)
    }
    @Test
    fun anOldSaveWithOnlySerpentinesAvailableAlsoOpensCrossroadsAfterTheSwap() {
        val vm = MetaViewModel(FakeSaveRepository(SaveData(levelProgress = mapOf(
            LevelCatalog.riverbank.id to LevelProgress(true, 1),
            LevelCatalog.serpentines.id to LevelProgress(true, 0)
        ))))
        assertTrue(vm.isUnlocked(LevelCatalog.crossroads))
        assertTrue(vm.isUnlocked(LevelCatalog.serpentines))
        assertFalse(vm.isUnlocked(LevelCatalog.fortress))
        assertEquals(0, vm.meta.stars)
    }
}
