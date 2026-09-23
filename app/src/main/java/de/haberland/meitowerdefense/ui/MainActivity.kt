package de.haberland.meitowerdefense.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import de.haberland.meitowerdefense.save.FileSaveRepository

class MainActivity : ComponentActivity() {
    private val metaViewModel: MetaViewModel by viewModels {
        viewModelFactory {
            initializer { MetaViewModel(FileSaveRepository(applicationContext)) }
        }
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private var updateReadyToInstall by mutableStateOf(false)

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // A cancelled flexible update is harmless; the next app start checks again.
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            updateReadyToInstall = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterImmersiveMode()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
        checkForFlexibleUpdate()

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

                if (updateReadyToInstall) {
                    AlertDialog(
                        onDismissRequest = { updateReadyToInstall = false },
                        title = { Text("Update bereit") },
                        text = { Text("Eine neue Version von MeiTowerdefense wurde heruntergeladen und kann jetzt installiert werden.") },
                        confirmButton = {
                            TextButton(onClick = { appUpdateManager.completeUpdate() }) {
                                Text("Installieren")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateReadyToInstall = false }) {
                                Text("Später")
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        metaViewModel.reload()
        enterImmersiveMode()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    updateReadyToInstall = true
                }
            }
        }
    }

    override fun onDestroy() {
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
        super.onDestroy()
    }

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun checkForFlexibleUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    updateReadyToInstall = true
                }

                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
            }
        }
    }

    private fun startGame(levelId: String) {
        startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_LEVEL_ID, levelId))
    }

    companion object {
        const val ENDLESS_LEVEL_ID = "endless"
    }
}
