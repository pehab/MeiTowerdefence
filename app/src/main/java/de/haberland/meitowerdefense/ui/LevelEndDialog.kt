package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.sim.RunStats
import kotlin.math.roundToInt

@Composable
fun LevelEndDialog(
    won: Boolean,
    stars: Int,
    stats: RunStats,
    elapsedSeconds: Float,
    endlessWave: Int? = null,
    endlessBestWave: Int? = null,
    onDone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                when {
                    endlessWave != null -> "Endloslauf beendet"
                    won -> "Level geschafft!"
                    else -> "Niederlage"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (endlessWave != null) {
                    Text("Erreichte Welle: $endlessWave", fontSize = 22.sp)
                    endlessBestWave?.let { Text("Bestwert: Welle $it") }
                } else if (won) {
                    Text("★".repeat(stars) + "☆".repeat((3 - stars).coerceAtLeast(0)), fontSize = 22.sp)
                } else {
                    Text("Die Basis wurde überrannt.")
                }

                val totalSeconds = elapsedSeconds.roundToInt().coerceAtLeast(0)
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                Text("Zeit: %d:%02d".format(minutes, seconds))
                Text("Gegner besiegt: ${stats.enemiesKilled}")
                Text("Gold verdient: ${stats.goldEarned}")
                Text("Türme: ${stats.towersBuilt} gebaut · ${stats.towersUpgraded} Upgrades · ${stats.towersSold} verkauft")
                if (stats.livesLost > 0) Text("Verlorene Leben: ${stats.livesLost}")
            }
        },
        confirmButton = { Button(onClick = onDone) { Text("Weiter") } }
    )
}
