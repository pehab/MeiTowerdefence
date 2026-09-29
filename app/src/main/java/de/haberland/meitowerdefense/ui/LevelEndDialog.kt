package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.haberland.meitowerdefense.sim.RunStats
import kotlin.math.roundToInt

private val Gold = Color(0xFFFFD885)
private val Ivory = Color(0xFFFFF1D7)
private val Muted = Color(0xFFE2D7C4)
private val Edge = Color(0xFFAA8D62)

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
    val totalSeconds = elapsedSeconds.roundToInt().coerceAtLeast(0)
    val time = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    val shape = RoundedCornerShape(10.dp)
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth(0.78f).widthIn(max = 600.dp)
                    .heightIn(max = maxHeight - 24.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF413A32), Color(0xFF28231F), Color(0xFF1C1A17))), shape)
                    .border(2.dp, Edge, shape)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    when {
                        endlessWave != null -> "Endloslauf beendet"
                        won -> "Level geschafft!"
                        else -> "Niederlage"
                    },
                    color = Ivory, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, textAlign = TextAlign.Center
                )
                Text(
                    when {
                        endlessWave != null -> "Welle $endlessWave erreicht"
                        won -> "★".repeat(stars) + "☆".repeat((3 - stars).coerceAtLeast(0))
                        else -> "Die Basis wurde überrannt."
                    },
                    color = Gold, fontSize = if (won && endlessWave == null) 26.sp else 17.sp,
                    fontFamily = FontFamily.Serif, textAlign = TextAlign.Center
                )
                Column(
                    Modifier.fillMaxWidth().weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xB31C1A17), RoundedCornerShape(6.dp))
                        .border(1.dp, Edge.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (endlessWave != null && endlessBestWave != null) StatRow("Bestwert", "Welle $endlessBestWave")
                    StatRow("Zeit", time)
                    StatRow("Gegner besiegt", "${stats.enemiesKilled}")
                    StatRow("Gold verdient", "${stats.goldEarned}")
                    HorizontalDivider(color = Edge.copy(alpha = 0.6f))
                    StatRow("Türme gebaut", "${stats.towersBuilt}")
                    StatRow("Upgrades", "${stats.towersUpgraded}")
                    StatRow("Türme verkauft", "${stats.towersSold}")
                    if (stats.livesLost > 0) StatRow("Verlorene Leben", "${stats.livesLost}")
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().border(1.dp, Gold, RoundedCornerShape(6.dp)),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9812E), contentColor = Color(0xFF21170A))
                ) {
                    Text("WEITER", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp)
        Text(value, color = Ivory, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
