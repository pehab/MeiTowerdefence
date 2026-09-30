package de.haberland.meitowerdefense.ui

import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.Tower
import de.haberland.meitowerdefense.model.MetaProgress
import de.haberland.meitowerdefense.sim.GameSession
import de.haberland.meitowerdefense.sim.GameSimulator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameControllerTest {
    @Test
    fun endlessHudKeepsTheWaveIndexBeyondTheEmptyCampaignWaveList() {
        val session = GameSession.start(LevelCatalog.endless, MetaProgress()).copy(waveIndex = 7)
        val controller = GameController(session)

        assertEquals(7, controller.hudState.value.waveIndex)
        assertNull(controller.hudState.value.totalWaves)
        controller.tick(0f)
        assertEquals(7, controller.hudState.value.waveIndex)
    }

    @Test
    fun startingThePendingWavePaysTheBonusAndClearsItsIndicator() {
        val session = GameSession.start(LevelCatalog.forestPath, MetaProgress()).copy(
            waveIndex = 1,
            waitingForWaveStart = true,
            timeUntilAutoStart = 3f
        )
        val controller = GameController(session)
        assertTrue(controller.hudState.value.earlyWaveBonusAvailable)

        controller.startNextWave()
        controller.tick(0f)

        assertEquals(session.gold + GameSimulator.EARLY_WAVE_BONUS_GOLD, controller.session.gold)
        assertFalse(controller.hudState.value.waitingForWaveStart)
        assertFalse(controller.hudState.value.earlyWaveBonusAvailable)
    }

    @Test
    fun theLastCampaignWaveDoesNotOfferAnotherEarlyWaveBonus() {
        val level = LevelCatalog.forestPath
        val controller = GameController(GameSession.start(level, MetaProgress()).copy(
            waveIndex = level.waves.size,
            waitingForWaveStart = true,
            timeUntilAutoStart = 3f
        ))
        val initialGold = controller.session.gold
        assertFalse(controller.hudState.value.earlyWaveBonusAvailable)

        controller.startNextWave()
        controller.tick(0f)
        assertEquals(initialGold, controller.session.gold)
    }
    @Test
    fun confirmedSaleUsesItsTowerIdEvenIfAnotherTowerIsSelected() {
        val confirmedTower = Tower("confirmed", TowerType.ARCHER, GridPos(1, 1))
        val otherTower = Tower("other", TowerType.ICE, GridPos(2, 1))
        val session = GameSession.start(LevelCatalog.forestPath, MetaProgress()).copy(
            towers = listOf(confirmedTower, otherTower)
        )
        val controller = GameController(session)
        controller.onTapGrid(otherTower.gridPos)
        controller.sellTower(confirmedTower.id)
        // Selling is still queued, and no tower disappears until the game tick applies it.
        assertEquals(2, controller.session.towers.size)
        controller.tick(0f)

        assertEquals(listOf(otherTower), controller.session.towers)
        assertEquals(otherTower.id, controller.selectedTowerId)
        assertEquals(session.gold + GameSimulator.sellValue(confirmedTower), controller.session.gold)
    }
}
