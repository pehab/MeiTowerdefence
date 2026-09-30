package de.haberland.meitowerdefense.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.LevelRating
import de.haberland.meitowerdefense.save.FileSaveRepository
import de.haberland.meitowerdefense.sim.GameOutcome
import de.haberland.meitowerdefense.sim.GameSession
import de.haberland.meitowerdefense.view.GameSurfaceView

/**
 * Not Compose-only: the game canvas is a classic [GameSurfaceView] (SurfaceView + its
 * own GameThread, see the project chat for why - real-time rendering with many moving
 * entities is not what Compose is built for), embedded into the Compose tree via
 * [AndroidView] with the HUD layered on top of it.
 *
 * Declared with android:configChanges="orientation|screenSize|keyboardHidden" in the
 * manifest, so this Activity is never destroyed/recreated on rotation - [controller] and
 * [metaViewModel] live for this Activity, no retained ViewModel or
 * SavedStateHandle needed to survive a config change that can't happen here.
 */
class GameActivity : ComponentActivity() {
    private lateinit var metaViewModel: MetaViewModel
    private lateinit var controller: GameController

    private fun recordRunProgress() {
        if (!::controller.isInitialized) return
        val session = controller.session
        metaViewModel.recordRunProgress(session.stats)
        if (session.level.endless) metaViewModel.recordEndlessProgress(session.completedWaves)
    }

    private fun exitGame() {
        recordRunProgress()
        if (metaViewModel.ensureSaved()) finish()
    }

    override fun onStop() {
        recordRunProgress()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A round shouldn't get interrupted by the screen locking, and the landscape
        // game area should actually be full screen rather than sharing space with the
        // status/navigation bars.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val levelId = intent.getStringExtra(EXTRA_LEVEL_ID)
        val level = levelId?.let { LevelCatalog.byId(it) }
        if (level == null) {
            finish()
            return
        }

        metaViewModel = MetaViewModel(FileSaveRepository(applicationContext))
        controller = GameController(GameSession.start(level, metaViewModel.meta))

        setContent {
            MeiTowerDefenseTheme {
                Box(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxSize()) {
                        GameStatusPanel(controller = controller, onExit = ::exitGame)
                        AndroidView(
                            factory = { ctx -> GameSurfaceView(ctx).also { it.controller = controller } },
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        GameActionsPanel(controller = controller)
                    }

                    val hud by controller.hudState
                    BackHandler { exitGame() }
                    LaunchedEffect(hud.completedWaves) { recordRunProgress() }
                    LaunchedEffect(hud.outcome) {
                        if (hud.outcome != GameOutcome.IN_PROGRESS) {
                            recordRunProgress()
                            if (level.endless) {
                                metaViewModel.recordEndlessResult(endlessWaveReached(controller.session))
                            } else {
                                metaViewModel.recordLevelResult(
                                    level = level,
                                    remainingLives = controller.session.lives,
                                    won = hud.outcome == GameOutcome.WON
                                )
                            }
                        }
                    }

                    if (hud.outcome != GameOutcome.IN_PROGRESS) {
                        val endlessWave = if (level.endless) endlessWaveReached(controller.session) else null
                        LevelEndDialog(
                            won = hud.outcome == GameOutcome.WON,
                            stars = if (level.endless) 0 else LevelRating.starsFor(
                                level.startingLives,
                                controller.session.lives,
                                hud.outcome == GameOutcome.WON
                            ),
                            stats = controller.session.stats,
                            elapsedSeconds = controller.session.elapsedSeconds,
                            endlessWave = endlessWave,
                            endlessBestWave = if (level.endless) maxOf(metaViewModel.endlessBestWave, endlessWave ?: 0) else null,
                            endlessCompletedWaves = if (level.endless) hud.completedWaves else null,
                            endlessStarsEarned = metaViewModel.endlessStarsEarned,
                            achievementStarsEarned = metaViewModel.achievementStarsEarned,
                            onDone = ::exitGame
                        )
                    }
                    SaveErrorDialog(metaViewModel)
                }
            }
        }
    }

    private fun endlessWaveReached(session: GameSession): Int =
        maxOf(
            1,
            session.waveIndex + if (session.waitingForWaveStart) 0 else 1
        )

    companion object {
        const val EXTRA_LEVEL_ID = "level_id"
    }
}
