package de.haberland.meitowerdefense.ui

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.*
import de.haberland.meitowerdefense.save.*
import de.haberland.meitowerdefense.sim.RunStats
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class ResetProgressTest {
    private fun progressed() = SaveData(stars = 30,
        metaUpgradeLevels = mapOf(MetaUpgradeType.STARTING_GOLD to 3),
        levelProgress = LevelCatalog.all.associate { it.id to LevelProgress(true, 3) },
        endlessBestWave = 51, endlessBestCompletedWaves = 50,
        killsByEnemy = mapOf(EnemyType.BASIC to 500),
        killsByTower = mapOf(TowerType.ARCHER to 500),
        claimedAchievements = setOf("kills:100", "kills:500", "tower_archer:100", "tower_archer:500"))
    private fun fresh() = SaveData(levelProgress = mapOf(LevelCatalog.forestPath.id to LevelProgress(unlocked = true)))

    @Test
    fun resetClearsAllProgressAndKeepsOnlyTheFirstLevelUnlockedAfterReload() {
        val repo = FakeSaveRepository(progressed())
        val vm = MetaViewModel(repo)
        vm.recordEndlessProgress(60)
        vm.recordRunProgress(RunStats(killsByEnemy = mapOf(EnemyType.BASIC to 100), killsByTower = mapOf(TowerType.ARCHER to 100)))
        assertTrue(vm.resetAllProgress())
        assertEquals(fresh(), repo.load())
        assertEquals(0, vm.endlessStarsEarned)
        assertEquals(0, vm.achievementStarsEarned)
        val reopened = MetaViewModel(repo)
        assertEquals(0, reopened.meta.stars)
        assertTrue(reopened.isUnlocked(LevelCatalog.forestPath))
        assertFalse(reopened.isUnlocked(LevelCatalog.mountainPass))
        assertTrue(reopened.claimedAchievements.isEmpty())
        reopened.recordEndlessProgress(10)
        assertEquals(1, reopened.meta.stars) // A deliberate fresh start resets lifetime claims too.
    }

    @Test
    fun failedResetIsRetriedAsAResetAndNeverReloadsTheOldProgress() {
        val repo = object : SaveRepository {
            var data = progressed()
            var failReset = true
            var resetCalls = 0
            override fun load() = data
            override fun save(data: SaveData) { this.data = data }
            override fun reset(data: SaveData) {
                resetCalls++
                if (failReset) throw IOException("reset failed")
                this.data = data
            }
        }
        val vm = MetaViewModel(repo)
        assertFalse(vm.resetAllProgress())
        assertEquals(0, vm.meta.stars)
        assertNotNull(vm.saveError)
        vm.reload()
        assertEquals(0, vm.meta.stars)
        assertEquals(2, repo.resetCalls)
        assertTrue(repo.data.stars > 0)
        repo.failReset = false
        assertTrue(vm.ensureSaved())
        assertEquals(3, repo.resetCalls)
        assertEquals(fresh(), repo.data)
    }

    @Test
    fun anExplicitResetCanRecoverFromAnUnreadableSave() {
        val repo = object : SaveRepository {
            var saved: SaveData? = null
            override fun load(): SaveData = throw IOException("unreadable")
            override fun save(data: SaveData) { saved = data }
        }
        val vm = MetaViewModel(repo)
        assertNotNull(vm.saveError)
        assertTrue(vm.resetAllProgress())
        assertEquals(fresh(), repo.saved)
        assertNull(vm.saveError)
    }
}
