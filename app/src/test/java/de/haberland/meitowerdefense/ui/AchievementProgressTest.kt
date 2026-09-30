package de.haberland.meitowerdefense.ui

import de.haberland.meitowerdefense.content.AchievementCatalog
import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.MetaUpgradeType
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.save.FakeSaveRepository
import de.haberland.meitowerdefense.save.LevelProgress
import de.haberland.meitowerdefense.save.SaveData
import de.haberland.meitowerdefense.save.SaveRepository
import de.haberland.meitowerdefense.sim.RunStats
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class AchievementProgressTest {
    private fun kills(count: Int) = RunStats(enemiesKilled = count,
        killsByEnemy = mapOf(EnemyType.BASIC to count), killsByTower = mapOf(TowerType.ARCHER to count))

    @Test
    fun repeatedSnapshotsAndReloadDoNotDoubleCountButANewRunAddsItsKills() {
        val repo = FakeSaveRepository()
        val first = MetaViewModel(repo)
        first.recordRunProgress(kills(60))
        first.recordRunProgress(kills(60))
        first.reload()
        first.recordRunProgress(kills(100))
        assertEquals(100, first.killsByEnemy[EnemyType.BASIC])
        assertEquals(2, first.meta.stars) // First total-kill and archer milestones.
        first.recordRunProgress(kills(60)) // Older observation must not move the baseline back.
        first.recordRunProgress(kills(100))
        assertEquals(100, first.killsByEnemy[EnemyType.BASIC])

        val second = MetaViewModel(repo)
        second.recordRunProgress(kills(400))
        assertEquals(500, second.killsByEnemy[EnemyType.BASIC])
        assertEquals(6, second.meta.stars) // Only the next two milestones pay 2 each.
        assertEquals(4, second.claimedAchievements.size)
    }

    @Test
    fun enemyAndTowerTracksCountIndependentlyAndCatchUpAllCrossedMilestones() {
        val vm = MetaViewModel(FakeSaveRepository())
        vm.recordRunProgress(RunStats(enemiesKilled = 205,
            killsByEnemy = mapOf(EnemyType.ARMORED to 100, EnemyType.FLYING to 100, EnemyType.BOSS to 5),
            killsByTower = mapOf(TowerType.CANNON to 100, TowerType.ICE to 100, TowerType.FIRE to 5)))
        assertEquals(6, vm.meta.stars) // total, armored, flying, boss, cannon, ice
        assertTrue("armored:50" in vm.claimedAchievements)
        assertTrue("flying:50" in vm.claimedAchievements)
        assertTrue("boss:5" in vm.claimedAchievements)
        assertFalse("tower_fire:100" in vm.claimedAchievements)
        vm.recordRunProgress(RunStats(enemiesKilled = 2_000,
            killsByEnemy = mapOf(EnemyType.ARMORED to 2_000)))
        assertTrue("kills:2000" in vm.claimedAchievements)
        assertTrue("armored:1000" in vm.claimedAchievements)
        val stars = vm.meta.stars
        vm.recordRunProgress(RunStats(enemiesKilled = 2_000, killsByEnemy = mapOf(EnemyType.ARMORED to 2_000)))
        assertEquals(stars, vm.meta.stars)
    }

    @Test
    fun existingCampaignRatingsGrantAchievementsOnceAndReplaysDoNotCountAsDistinctLevels() {
        val progress = LevelCatalog.all.take(4).associate { it.id to LevelProgress(true, 3) }
        val repo = FakeSaveRepository(SaveData(stars = 7, levelProgress = progress))
        val vm = MetaViewModel(repo)
        assertEquals(14, vm.meta.stars) // campaign 1+2, perfect 1+3
        assertEquals(4, vm.claimedAchievements.size)
        vm.recordLevelResult(LevelCatalog.all.first(), 18, true)
        vm.reload()
        assertEquals(14, vm.meta.stars)
        assertEquals(14, MetaViewModel(repo).meta.stars)
        assertEquals(4, vm.achievementValue(AchievementCatalog.tracks.first { it.id == "campaign" }))
    }

    @Test
    fun failedSaveKeepsCountersAndClaimsForAnIdempotentRetry() {
        val repo = object : SaveRepository {
            var data = SaveData()
            var fail = false
            override fun load() = data
            override fun save(data: SaveData) {
                if (fail) throw IOException("disk full")
                this.data = data
            }
        }
        val vm = MetaViewModel(repo)
        repo.fail = true
        vm.recordRunProgress(kills(100))
        vm.recordRunProgress(kills(100))
        vm.reload()
        assertNotNull(vm.saveError)
        assertEquals(2, vm.meta.stars)
        assertEquals(100, vm.killsByEnemy[EnemyType.BASIC])
        repo.fail = false
        assertTrue(vm.ensureSaved())
        val reopened = MetaViewModel(repo)
        assertEquals(2, reopened.meta.stars)
        assertEquals(vm.claimedAchievements, reopened.claimedAchievements)
        assertEquals(vm.killsByEnemy, reopened.killsByEnemy)
    }

    @Test
    fun catalogHasUniquePersistentIdsAndCoversTheFullUpgradeBudget() {
        val ids = AchievementCatalog.tracks.flatMap { track -> track.thresholds.indices.map(track::milestoneId) }
        assertEquals(ids.size, ids.toSet().size)
        val upgradeBudget = MetaUpgradeType.entries.sumOf { type ->
            (0 until type.maxLevel).sumOf { type.costForNextLevel(it) ?: 0 }
        }
        val available = AchievementCatalog.tracks.sumOf { it.rewards.sum() } + LevelCatalog.all.size * 3
        assertTrue("Campaign and achievement rewards should cover all permanent upgrades", available >= upgradeBudget)
    }
}
