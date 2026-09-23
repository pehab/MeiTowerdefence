package de.haberland.meitowerdefense.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

        setContent {
            MeiTowerDefenseTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        MainMenuScreen(
                            onPlay = { navController.navigate("levels") },
                            onStarShop = { navController.navigate("shop") }
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
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        metaViewModel.reload()
    }

    private fun startGame(levelId: String) {
        startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_LEVEL_ID, levelId))
    }

    companion object {
        // Not wired up to a real endless-mode LevelDefinition yet - LevelCatalog.byId
        // returns null for this id and GameActivity currently just finishes rather than
        // crashing. Endless mode is explicitly out of scope for this first playable
        // slice (see project chat); LevelSelectScreen's endless card is unreachable in
        // practice anyway until all 3 real levels are beaten.
        const val ENDLESS_LEVEL_ID = "endless"
    }
}
