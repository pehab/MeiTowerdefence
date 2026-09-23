package de.haberland.meitowerdefense.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.haberland.meitowerdefense.model.GridPos
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.GameOutcome
import de.haberland.meitowerdefense.sim.GameSession
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.sim.Tower
import java.util.concurrent.ConcurrentLinkedQueue

sealed interface GameAction {
    data class Build(val type: TowerType, val pos: GridPos) : GameAction
    data class Upgrade(val towerId: String, val specialization: Specialization? = null) : GameAction
    data class Sell(val towerId: String) : GameAction
    data object StartNextWave : GameAction
}

sealed interface InputMode {
    data object Normal : InputMode
    data class Placing(val type: TowerType) : InputMode
}

/** The small slice of [GameSession] the Compose HUD actually needs to observe. */
data class HudSnapshot(
    val gold: Int,
    val lives: Int,
    val waveIndex: Int,
    val totalWaves: Int,
    val waitingForWaveStart: Boolean,
    /** Whether tapping startNextWave() right now would pay GameSimulator.EARLY_WAVE_BONUS_GOLD. */
    val earlyWaveBonusAvailable: Boolean,
    val outcome: GameOutcome,
    val selectedTower: Tower?
)

/**
 * Owns the authoritative [GameSession] and is the only thing allowed to mutate it -
 * GameThread calls [tick] once per frame (background thread); touch input and the
 * Compose HUD only ever *submit* [GameAction]s via [onTapGrid]/[upgradeSelectedTower]/
 * [sellSelectedTower]/[startNextWave] (main thread), queued and applied at the start of
 * the next tick.
 *
 * That queue is what makes this safe without locking the whole session on every touch
 * event: two threads never write [session] at once, only GameThread does, and the
 * @Volatile read gives touch handling and rendering a consistent (if momentarily
 * slightly stale) view of it.
 *
 * Deliberately a plain class, not a ViewModel: GameActivity is declared with
 * android:configChanges="orientation|screenSize|keyboardHidden" specifically so it is
 * never recreated on rotation, so there's no configuration-change survival gap for a
 * ViewModel to solve here (contrast with AppViewModel in MeiOCRWorkout, which exists
 * because MainActivity there is *not* configChanges-protected).
 */
class GameController(initialSession: GameSession) {

    @Volatile
    var session: GameSession = initialSession
        private set

    private val pendingActions = ConcurrentLinkedQueue<GameAction>()

    var inputMode by mutableStateOf<InputMode>(InputMode.Normal)
        private set

    var selectedTowerId by mutableStateOf<String?>(null)
        private set

    /**
     * 1x/2x/4x simulation speed. Read from GameThread's background thread in [tick] (as
     * a scale factor on that frame's dt), written from the HUD's speed buttons on the
     * main thread - safe for the same reason [session] is: Compose State's snapshot
     * system supports cross-thread reads/writes, and this is the only thing that ever
     * writes it.
     */
    var speedMultiplier by mutableStateOf(1f)

    val hudState = mutableStateOf(snapshot(initialSession))

    fun startPlacing(type: TowerType) {
        inputMode = InputMode.Placing(type)
        selectedTowerId = null
    }

    fun cancelPlacing() {
        if (inputMode is InputMode.Placing) inputMode = InputMode.Normal
    }

    /** Called from GameSurfaceView's touch handling (main thread) on a completed tap. */
    fun onTapGrid(pos: GridPos) {
        when (val mode = inputMode) {
            is InputMode.Placing -> {
                pendingActions.add(GameAction.Build(mode.type, pos))
                inputMode = InputMode.Normal
            }
            InputMode.Normal -> {
                selectedTowerId = session.towers.find { it.gridPos == pos }?.id
            }
        }
    }

    fun deselectTower() {
        selectedTowerId = null
    }

    fun upgradeSelectedTower(specialization: Specialization? = null) {
        val id = selectedTowerId ?: return
        pendingActions.add(GameAction.Upgrade(id, specialization))
    }

    fun sellSelectedTower() {
        val id = selectedTowerId ?: return
        pendingActions.add(GameAction.Sell(id))
        selectedTowerId = null
    }

    fun startNextWave() {
        pendingActions.add(GameAction.StartNextWave)
    }

    /** Called once per frame by [de.haberland.meitowerdefense.engine.GameThread] - background thread. */
    fun tick(dt: Float) {
        var current = session
        while (true) {
            val action = pendingActions.poll() ?: break
            current = applyAction(current, action) ?: current
        }
        current = GameSimulator.step(current, dt * speedMultiplier)
        session = current
        hudState.value = snapshot(current)
    }

    private fun applyAction(session: GameSession, action: GameAction): GameSession? = when (action) {
        is GameAction.Build -> GameSimulator.buildTower(session, action.type, action.pos)
        is GameAction.Upgrade -> GameSimulator.upgradeTower(session, action.towerId, action.specialization)
        is GameAction.Sell -> GameSimulator.sellTower(session, action.towerId)
        GameAction.StartNextWave -> GameSimulator.startNextWave(session)
    }

    private fun snapshot(s: GameSession) = HudSnapshot(
        gold = s.gold,
        lives = s.lives,
        waveIndex = s.waveIndex.coerceAtMost(s.level.waves.size),
        totalWaves = s.level.waves.size,
        waitingForWaveStart = s.waitingForWaveStart,
        earlyWaveBonusAvailable = s.waveIndex > 0 && s.timeUntilAutoStart > 0f,
        outcome = s.outcome,
        selectedTower = s.towers.find { it.id == selectedTowerId }
    )
}
