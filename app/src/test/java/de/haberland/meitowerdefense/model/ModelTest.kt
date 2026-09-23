package de.haberland.meitowerdefense.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TowerBalanceTest {

    @Test
    fun levelOneHasNoMultiplierBonus() {
        assertEquals(1.0f, TowerBalance.levelMultiplier(1), 0.001f)
    }

    @Test
    fun laterLevelsAreStrictlyStrongerThanEarlierOnes() {
        var previous = TowerBalance.levelMultiplier(1)
        for (level in 2..TowerBalance.MAX_LEVEL) {
            val current = TowerBalance.levelMultiplier(level)
            assertTrue("level $level ($current) should be stronger than level ${level - 1} ($previous)", current > previous)
            previous = current
        }
    }

    @Test
    fun levelOutsideOneToMaxIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { TowerBalance.levelMultiplier(0) }
        assertThrows(IllegalArgumentException::class.java) { TowerBalance.levelMultiplier(TowerBalance.MAX_LEVEL + 1) }
    }

    @Test
    fun upgradeCostExistsBelowMaxLevelAndIsNullAtMaxLevel() {
        for (level in 1 until TowerBalance.MAX_LEVEL) {
            assertTrue(TowerBalance.upgradeCost(TowerType.ARCHER, level) != null)
        }
        assertNull(TowerBalance.upgradeCost(TowerType.ARCHER, TowerBalance.MAX_LEVEL))
    }

    @Test
    fun specializationChoiceHappensExactlyAtTheConfiguredLevel() {
        assertFalse(TowerBalance.isSpecializationChoice(TowerBalance.SPECIALIZATION_LEVEL - 1))
        assertTrue(TowerBalance.isSpecializationChoice(TowerBalance.SPECIALIZATION_LEVEL))
        assertFalse(TowerBalance.isSpecializationChoice(TowerBalance.SPECIALIZATION_LEVEL + 1))
    }
}

class SpecializationTest {

    @Test
    fun everyTowerTypeHasExactlyTwoSpecializationBranches() {
        for (type in TowerType.entries) {
            assertEquals("$type should have exactly 2 branches", 2, Specialization.branchesFor(type).size)
        }
    }

    @Test
    fun everyBranchPointsBackToItsOwnTowerType() {
        for (spec in Specialization.entries) {
            assertEquals(spec.towerType, Specialization.branchesFor(spec.towerType).find { it == spec }?.towerType)
        }
    }
}

class MetaProgressTest {

    @Test
    fun startsAtLevelZeroForEveryUpgrade() {
        val progress = MetaProgress()
        for (type in MetaUpgradeType.entries) {
            assertEquals(0, progress.levelOf(type))
        }
    }

    @Test
    fun purchaseSpendsStarsAndIncreasesLevel() {
        var progress = MetaProgress(stars = 10)
        val cost = MetaUpgradeType.GOLD_INCOME.costForNextLevel(0)!!
        progress = progress.purchase(MetaUpgradeType.GOLD_INCOME)
        assertEquals(1, progress.levelOf(MetaUpgradeType.GOLD_INCOME))
        assertEquals(10 - cost, progress.stars)
    }

    @Test
    fun purchaseIsANoOpWhenNotEnoughStars() {
        val progress = MetaProgress(stars = 0)
        val after = progress.purchase(MetaUpgradeType.GOLD_INCOME)
        assertEquals(progress, after)
    }

    @Test
    fun purchaseIsANoOpOnceMaxLevelIsReached() {
        var progress = MetaProgress(stars = 1000)
        repeat(MetaUpgradeType.ARCHER_DAMAGE.maxLevel) {
            progress = progress.purchase(MetaUpgradeType.ARCHER_DAMAGE)
        }
        assertEquals(MetaUpgradeType.ARCHER_DAMAGE.maxLevel, progress.levelOf(MetaUpgradeType.ARCHER_DAMAGE))
        val starsAtMax = progress.stars
        progress = progress.purchase(MetaUpgradeType.ARCHER_DAMAGE)
        assertEquals(MetaUpgradeType.ARCHER_DAMAGE.maxLevel, progress.levelOf(MetaUpgradeType.ARCHER_DAMAGE))
        assertEquals(starsAtMax, progress.stars)
    }

    @Test
    fun derivedBonusesAreZeroAtLevelZeroAndPositiveAfterPurchase() {
        var progress = MetaProgress(stars = 1000)
        assertEquals(1.0f, progress.goldIncomeMultiplier, 0.001f)
        progress = progress.purchase(MetaUpgradeType.GOLD_INCOME)
        assertTrue(progress.goldIncomeMultiplier > 1.0f)
    }
}

class LevelRatingTest {

    @Test
    fun losingAlwaysGivesZeroStars() {
        assertEquals(0, LevelRating.starsFor(startingLives = 10, remainingLives = 5, won = false))
    }

    @Test
    fun perfectClearGivesThreeStars() {
        assertEquals(3, LevelRating.starsFor(startingLives = 10, remainingLives = 10, won = true))
    }

    @Test
    fun aboveHalfLivesRemainingGivesTwoStars() {
        assertEquals(2, LevelRating.starsFor(startingLives = 10, remainingLives = 6, won = true))
    }

    @Test
    fun belowHalfLivesRemainingGivesOneStar() {
        assertEquals(1, LevelRating.starsFor(startingLives = 10, remainingLives = 2, won = true))
    }
}
