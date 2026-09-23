package de.haberland.meitowerdefense.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.haberland.meitowerdefense.save.FileSaveRepository

class MainActivity : ComponentActivity() {
    private val metaViewModel: MetaViewModel by viewModels {
        viewModelFactory {
            initializer { MetaViewModel(FileSaveRepository(applicationContext)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterImmersiveMode()

        setContent {
            MeiTowerDefenseTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        MainMenuScreen(
                            onPlay = { navController.navigate("levels") },
                            onStarShop = { navController.navigate("shop") },
                            onGlossary = { navController.navigate("glossary") }
                        )
                    }
                    composable("levels") {
                        LevelSelectScreen(
                            metaViewModel = metaViewModel,
                            onBack = { navController.popBackStack() },
                            onPlayLevel = ::startGame,
                            onPlayEndless = { startGame(ENDLESS_LEVEL_ID) }
                        )
                    }
                    composable("shop") {
                        StarShopScreen(metaViewModel = metaViewModel, onBack = { navController.popBackStack() })
                    }
                    composable("glossary") {
                        GlossaryScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        metaViewModel.reload()
        enterImmersiveMode()
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun startGame(levelId: String) {
        startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_LEVEL_ID, levelId))
    }

    companion object {
        const val ENDLESS_LEVEL_ID = "endless"
    }
}
