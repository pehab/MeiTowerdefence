package de.haberland.meitowerdefense.ui

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.MetaUpgradeType
import de.haberland.meitowerdefense.save.FakeSaveRepository
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
}
