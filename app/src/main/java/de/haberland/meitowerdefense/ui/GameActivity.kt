package de.haberland.meitowerdefense.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
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
 * [metaViewModel] just live as plain local vals in [onCreate], no ViewModel or
 * SavedStateHandle needed to survive a config change that can't happen here.
 */
class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val levelId = intent.getStringExtra(EXTRA_LEVEL_ID)
        val level = levelId?.let { LevelCatalog.byId(it) }
        if (level == null) {
            // Unknown or not-yet-implemented level id (e.g. endless mode - see
            // MainActivity.ENDLESS_LEVEL_ID). Nothing sensible to show; bail out rather
            // than crash on a null level.
            finish()
            return
        }

        val metaViewModel = MetaViewModel(FileSaveRepository(applicationContext))
        val controller = GameController(GameSession.start(level, metaViewModel.meta))

        setContent {
            MeiTowerDefenseTheme {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx -> GameSurfaceView(ctx).also { it.controller = controller } },
                        modifier = Modifier.fillMaxSize()
                    )
                    GameHud(controller = controller, onExit = { finish() })

                    val hud by controller.hudState
                    LaunchedEffect(hud.outcome) {
                        if (hud.outcome != GameOutcome.IN_PROGRESS) {
                            metaViewModel.recordLevelResult(
                                level = level,
                                remainingLives = controller.session.lives,
                                won = hud.outcome == GameOutcome.WON
                            )
                        }
                    }

                    if (hud.outcome != GameOutcome.IN_PROGRESS) {
                        LevelEndDialog(
                            won = hud.outcome == GameOutcome.WON,
                            stars = LevelRating.starsFor(level.startingLives, controller.session.lives, hud.outcome == GameOutcome.WON),
                            onDone = { finish() }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_LEVEL_ID = "level_id"
    }
}
