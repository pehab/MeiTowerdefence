package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.sim.Tower

private val panelColor = Color.Black.copy(alpha = 0.78f)

@Composable
fun GameStatusPanel(controller: GameController, onExit: () -> Unit) {
    val hud by controller.hudState
    var showExitConfirm by remember { mutableStateOf(false) }
    var showGlossary by remember { mutableStateOf(false) }
    val width = (LocalConfiguration.current.screenWidthDp * 0.17f).coerceIn(112f, 176f).dp

    Column(
        Modifier.width(width).fillMaxHeight().background(panelColor)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            IconButton(onClick = { showExitConfirm = true }) {
                Icon(Icons.Default.Close, "Level abbrechen", tint = Color.White)
            }
            IconButton(onClick = { showGlossary = true }) {
                Icon(Icons.Default.Info, "Glossar", tint = Color.White)
            }
        }
        HudText("Gold: ${hud.gold}")
        HudText("Leben: ${hud.lives}")
        val waveNumber = hud.waveIndex + 1
        HudText(
            if (hud.totalWaves == null) "Welle $waveNumber · ∞"
            else "Welle ${waveNumber.coerceAtMost(hud.totalWaves!!)}/${hud.totalWaves}"
        )
        SpeedToggle(current = controller.speedMultiplier, onSelect = { controller.speedMultiplier = it })
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Level abbrechen?") },
            text = { Text("Der Fortschritt in diesem Durchlauf geht verloren.") },
            confirmButton = { TextButton(onClick = onExit) { Text("ABBRECHEN") } },
            dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("WEITERSPIELEN") } }
        )
    }
    if (showGlossary) GlossaryDialog(onDismiss = { showGlossary = false })
}

@Composable
fun GameActionsPanel(controller: GameController) {
    val hud by controller.hudState
    val mode = controller.inputMode
    val width = (LocalConfiguration.current.screenWidthDp * 0.22f).coerceIn(148f, 220f).dp

    Column(
        Modifier.width(width).fillMaxHeight().background(panelColor)
            .verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val hasNextWave = hud.totalWaves == null || hud.waveIndex < hud.totalWaves!!
        if (hud.waitingForWaveStart && hasNextWave) {
            Button(onClick = controller::startNextWave, modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)) {
                Text(
                    when {
                        hud.waveIndex == 0 -> if (hud.totalWaves == null) "ENDLOSMODUS STARTEN" else "TRAINING STARTEN"
                        hud.earlyWaveBonusAvailable -> "NÄCHSTE WELLE (+${GameSimulator.EARLY_WAVE_BONUS_GOLD} Gold)"
                        else -> "NÄCHSTE WELLE"
                    },
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp
                )
            }
        }
        when {
            mode is InputMode.Placing -> PlacingBar(mode.type, onCancel = controller::cancelPlacing)
            hud.selectedTower != null -> UpgradePanel(
                tower = hud.selectedTower!!, gold = hud.gold,
                onUpgrade = { spec -> controller.upgradeSelectedTower(spec) },
                onSell = controller::sellSelectedTower, onDeselect = controller::deselectTower
            )
            else -> BuildBar(gold = hud.gold, onSelectType = controller::startPlacing)
        }
    }
}

@Composable
private fun SpeedToggle(current: Float, onSelect: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(1f, 2f, 4f).forEach { speed ->
            OutlinedButton(
                onClick = { onSelect(speed) }, modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                colors = if (current == speed) {
                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                } else ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) { Text("${speed.toInt()}x", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun HudText(text: String) {
    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
}

@Composable
private fun BuildBar(gold: Int, onSelectType: (TowerType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TowerType.entries.forEach { type ->
            OutlinedButton(
                onClick = { onSelectType(type) }, enabled = gold >= type.baseCost,
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(type.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
                    Text("${type.baseCost} Gold", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PlacingBar(type: TowerType, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        HudText("Platziere ${type.displayName} – auf das Feld tippen")
        TextButton(onClick = onCancel) { Text("Abbrechen") }
    }
}

@Composable
private fun UpgradePanel(
    tower: Tower,
    gold: Int,
    onUpgrade: (Specialization?) -> Unit,
    onSell: () -> Unit,
    onDeselect: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HudText("${tower.type.displayName} · Stufe ${tower.level}")
        TextButton(onClick = onDeselect) { Text("Schließen") }
        if (tower.needsSpecializationChoice) {
            val cost = tower.upgradeCost()
            HudText("Spezialisierung wählen (${cost ?: 0} Gold):")
            Specialization.branchesFor(tower.type).forEach { spec ->
                Button(onClick = { onUpgrade(spec) }, enabled = cost != null && gold >= cost,
                    modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp)) {
                    Text(spec.displayName, fontSize = 12.sp, lineHeight = 14.sp)
                }
            }
        } else {
            val cost = tower.upgradeCost()
            if (cost != null) {
                Button(onClick = { onUpgrade(null) }, enabled = gold >= cost,
                    modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp)) {
                    Text("Upgrade ($cost Gold)", fontSize = 12.sp)
                }
            } else HudText("Maximalstufe erreicht")
            OutlinedButton(onClick = onSell, modifier = Modifier.fillMaxWidth()) { Text("Verkaufen") }
        }
    }
}
