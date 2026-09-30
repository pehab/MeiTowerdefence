package de.haberland.meitowerdefense.save

import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class JsonFileSaveRepositoryTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private fun primary(dir: File) = File(dir, "savegame.json")
    private fun backup(dir: File) = File(dir, "savegame.json.bak")

    @Test
    fun resetReplacesBothCopiesAndRecoveryCannotResurrectTheOldProgress() {
        val dir = temporary.newFolder()
        val repo = JsonFileSaveRepository(dir)
        repo.save(SaveData(stars = 30, endlessBestWave = 50,
            claimedAchievements = setOf("kills:100")))
        val fresh = SaveData(levelProgress = mapOf("forest_path" to LevelProgress(unlocked = true)))
        repo.reset(fresh)
        assertEquals(primary(dir).readText(), backup(dir).readText())
        primary(dir).writeText("broken after reset")
        assertEquals(fresh, JsonFileSaveRepository(dir).load())
    }

    @Test
    fun failedResetBackupReplacementDoesNotOverwriteThePrimaryAndCanBeRetried() {
        val dir = temporary.newFolder()
        val repo = JsonFileSaveRepository(dir)
        val old = SaveData(stars = 30)
        repo.save(old)
        assertTrue(backup(dir).delete())
        assertTrue(backup(dir).mkdir())
        val blocker = File(backup(dir), "blocked").apply { writeText("block replacement") }
        assertThrows(IOException::class.java) { repo.reset(SaveData()) }
        assertEquals(old, repo.load())
        assertTrue(blocker.delete())
        assertTrue(backup(dir).delete())
        repo.reset(SaveData())
        assertEquals(SaveData(), repo.load())
        assertEquals(primary(dir).readText(), backup(dir).readText())
    }

    @Test
    fun oldJsonLoadsWithEmptyAchievementCountersAndNoInferredCompletedWaves() {
        val dir = temporary.newFolder()
        primary(dir).writeText("""{"stars":7,"endlessBestWave":42}""")
        val data = JsonFileSaveRepository(dir).load()
        assertEquals(7, data.stars)
        assertEquals(42, data.endlessBestWave)
        assertEquals(0, data.endlessBestCompletedWaves)
        assertTrue(data.killsByEnemy.isEmpty())
        assertTrue(data.claimedAchievements.isEmpty())
    }

    @Test
    fun firstSaveSeedsBothCopiesAndCanBeReadByANewRepository() {
        val dir = temporary.newFolder()
        val data = SaveData(stars = 12, endlessBestWave = 9,
            levelProgress = mapOf("forest_path" to LevelProgress(true, 3)),
            endlessBestCompletedWaves = 20,
            killsByEnemy = mapOf(EnemyType.ARMORED to 55),
            killsByTower = mapOf(TowerType.CANNON to 55),
            claimedAchievements = setOf("armored:50"),
            leaderboardName = "Peter")
        JsonFileSaveRepository(dir).save(data)
        assertEquals(data, JsonFileSaveRepository(dir).load())
        assertEquals(primary(dir).readText(), backup(dir).readText())
    }

    @Test
    fun corruptPrimaryRecoversPreviousGenerationAndPreservesDamagedBytes() {
        val dir = temporary.newFolder()
        val repo = JsonFileSaveRepository(dir)
        val previous = SaveData(stars = 5, endlessBestWave = 4)
        repo.save(previous)
        repo.save(SaveData(stars = 8, endlessBestWave = 6))
        primary(dir).writeText("{unfinished")

        val recovered = repo.load()
        assertEquals(previous, recovered)
        val archived = dir.listFiles()!!.single { it.name.startsWith("savegame-corrupt-") }
        assertEquals("{unfinished", archived.readText())
        repo.save(recovered)
        assertEquals(previous, JsonFileSaveRepository(dir).load())
        primary(dir).writeText("broken again")
        assertEquals(previous, repo.load())
    }

    @Test
    fun bothDamagedCopiesAreArchivedBeforeStartingFresh() {
        val dir = temporary.newFolder()
        primary(dir).writeText("bad primary")
        backup(dir).writeText("bad backup")
        val repo = JsonFileSaveRepository(dir)
        assertEquals(SaveData(), repo.load())
        val archived = dir.listFiles()!!.filter { it.name.startsWith("savegame-corrupt-") }
        assertEquals(setOf("bad primary", "bad backup"), archived.map { it.readText() }.toSet())
        repo.save(SaveData(stars = 2))
        assertEquals(SaveData(stars = 2), repo.load())
    }

    @Test
    fun failedBackupReplacementLeavesThePrimaryUnchangedAndCleansTemporaryFiles() {
        val dir = temporary.newFolder()
        val repo = JsonFileSaveRepository(dir)
        val data = SaveData(stars = 7)
        repo.save(data)
        assertTrue(backup(dir).delete())
        assertTrue(backup(dir).mkdir())
        File(backup(dir), "blocked").writeText("block replacement")
        val original = primary(dir).readBytes()

        assertThrows(IOException::class.java) { repo.save(SaveData(stars = 10)) }
        assertTrue(original.contentEquals(primary(dir).readBytes()))
        assertEquals(data, repo.load())
        assertFalse(dir.listFiles()!!.any { it.name.startsWith("savegame-write-") })
    }

    @Test
    fun interruptedTemporaryWriteCannotReplaceTheLastCommittedSave() {
        val dir = temporary.newFolder()
        val data = SaveData(stars = 4)
        JsonFileSaveRepository(dir).save(data)
        File(dir, "savegame-write-interrupted.tmp").writeText("{incomplete")
        assertEquals(data, JsonFileSaveRepository(dir).load())
    }

    @Test
    fun missingPrimaryUsesBackupAndAnUnreadablePrimaryDoesNotOverwriteIt() {
        val dir = temporary.newFolder()
        val data = SaveData(stars = 6)
        val repo = JsonFileSaveRepository(dir)
        repo.save(data)
        assertTrue(primary(dir).delete())
        assertEquals(data, repo.load())
        assertTrue(primary(dir).mkdir())
        assertThrows(IOException::class.java) { repo.load() }
        assertThrows(IOException::class.java) { repo.save(SaveData()) }
        assertTrue(backup(dir).isFile)
    }
}
