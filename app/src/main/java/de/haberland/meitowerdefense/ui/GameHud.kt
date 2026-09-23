package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.sim.Tower

@Composable
fun GameHud(controller: GameController, onExit: () -> Unit) {
    val hud by controller.hudState
    val mode = controller.inputMode
    var showExitConfirm by remember { mutableStateOf(false) }
    var showGlossary by remember { mutableStateOf(false) }

    // One Column filling the whole overlay: top bar, a weighted Spacer that pushes
    // everything below it down, then the bottom panel - the game canvas shows through
    // in the middle, since this whole overlay has a transparent background apart from
    // the two bars themselves. Callers place this inside a Box(Modifier.fillMaxSize())
    // alongside the game canvas (see GameActivity).
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showExitConfirm = true }) { Icon(Icons.Default.Close, "Level abbrechen", tint = Color.White) }
                IconButton(onClick = { showGlossary = true }) { Icon(Icons.Default.Info, "Glossar", tint = Color.White) }
            }
            HudText("Gold: ${hud.gold}")
            HudText("Leben: ${hud.lives}")
            val waveNumber = hud.waveIndex + 1
            val totalWaves = hud.totalWaves
            HudText(
                if (totalWaves == null) {
                    "Welle $waveNumber · ∞"
                } else {
                    "Welle ${waveNumber.coerceAtMost(totalWaves)}/$totalWaves"
                }
            )
            SpeedToggle(current = controller.speedMultiplier, onSelect = { controller.speedMultiplier = it })
        }

        Spacer(Modifier.weight(1f))

        Column(
            Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hud.waitingForWaveStart) {
                Button(onClick = controller::startNextWave, modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        when {
                            hud.waveIndex == 0 -> if (hud.totalWaves == null) "ENDLOSMODUS STARTEN" else "TRAINING STARTEN"
                            hud.earlyWaveBonusAvailable -> "NÄCHSTE WELLE (+${GameSimulator.EARLY_WAVE_BONUS_GOLD} Gold)"
                            else -> "NÄCHSTE WELLE"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.Center) {
                when {
                    mode is InputMode.Placing -> PlacingBar(mode.type, onCancel = controller::cancelPlacing)
                    hud.selectedTower != null -> UpgradePanel(
                        tower = hud.selectedTower!!,
                        gold = hud.gold,
                        onUpgrade = { spec -> controller.upgradeSelectedTower(spec) },
                        onSell = controller::sellSelectedTower,
                        onDeselect = controller::deselectTower
                    )
                    else -> BuildBar(gold = hud.gold, onSelectType = controller::startPlacing)
                }
            }
        }
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

    if (showGlossary) {
        GlossaryDialog(onDismiss = { showGlossary = false })
    }
}

@Composable
private fun SpeedToggle(current: Float, onSelect: (Float) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(1f, 2f, 4f).forEach { speed ->
            val active = current == speed
            OutlinedButton(
                onClick = { onSelect(speed) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = if (active) {
                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                } else {
                    ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                }
            ) {
                Text("${speed.toInt()}x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HudText(text: String) {
    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
}

@Composable
private fun BuildBar(gold: Int, onSelectType: (TowerType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TowerType.entries.forEach { type ->
            val affordable = gold >= type.baseCost
            OutlinedButton(onClick = { onSelectType(type) }, enabled = affordable) {
                Column {
                    Text(type.displayName, fontWeight = FontWeight.Bold)
                    Text("${type.baseCost} Gold", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PlacingBar(type: TowerType, onCancel: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            HudText("${tower.type.displayName} · Stufe ${tower.level}")
            TextButton(onClick = onDeselect) { Text("Schließen") }
        }

        if (tower.needsSpecializationChoice) {
            val cost = tower.upgradeCost()
            HudText("Spezialisierung wählen (${cost ?: 0} Gold):")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Specialization.branchesFor(tower.type).forEach { spec ->
                    Button(onClick = { onUpgrade(spec) }, enabled = cost != null && gold >= cost) {
                        Text(spec.displayName)
                    }
                }
            }
        } else {
            val cost = tower.upgradeCost()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (cost != null) {
                    Button(onClick = { onUpgrade(null) }, enabled = gold >= cost) { Text("Upgrade ($cost Gold)") }
                } else {
                    HudText("Maximalstufe erreicht")
                }
                OutlinedButton(onClick = onSell) { Text("Verkaufen") }
            }
        }
    }
}
