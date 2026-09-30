package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.GameSimulator
import de.haberland.meitowerdefense.sim.Tower

private val HudGold = Color(0xFFFFD885)
private val HudIvory = Color(0xFFFFF1D7)
private val HudStone = Color(0xFF28231F)
private val HudEdge = Color(0xFFAA8D62)
private val HudMuted = Color(0xFFE2D7C4)
private val HudPanel = Brush.verticalGradient(listOf(Color(0xFF413A32), HudStone, Color(0xFF1C1A17)))
private val HudCard = RoundedCornerShape(6.dp)

@Composable
private fun PanelTitle(title: String) {
    Text(title, color = HudGold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 18.sp)
}

@Composable
private fun HudButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                      enabled: Boolean = true, prominent: Boolean = false) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = modifier.border(1.dp, if (enabled) HudEdge else HudEdge.copy(alpha = 0.35f), HudCard),
        shape = HudCard,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (prominent) Color(0xFFB9812E) else Color(0xFF413A32),
            contentColor = if (prominent) Color(0xFF21170A) else HudIvory,
            disabledContainerColor = Color(0xFF302C27),
            disabledContentColor = HudMuted.copy(alpha = 0.5f)
        )
    ) { Text(label, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 14.sp) }
}

@Composable
fun GameStatusPanel(controller: GameController, onExit: () -> Unit) {
    val hud by controller.hudState
    var showExitConfirm by remember { mutableStateOf(false) }
    var showGlossary by remember { mutableStateOf(false) }
    val width = (LocalConfiguration.current.screenWidthDp * 0.17f).coerceIn(112f, 176f).dp

    Column(
        Modifier.width(width).fillMaxHeight().background(HudPanel)
            .border(1.dp, HudEdge.copy(alpha = 0.75f))
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelTitle("VERTEIDIGUNG")
        Row {
            IconButton(onClick = { showExitConfirm = true }) {
                Icon(Icons.Default.Close, "Level abbrechen", tint = HudGold)
            }
            IconButton(onClick = { showGlossary = true }) {
                Icon(Icons.Default.Info, "Glossar", tint = HudGold)
            }
        }
        HudStat("GOLD", "${hud.gold}")
        HudStat("LEBEN", "${hud.lives}")
        val waveNumber = hud.waveIndex + 1
        HudStat("WELLE",
            if (hud.totalWaves == null) "Welle $waveNumber · ∞"
            else "Welle ${waveNumber.coerceAtMost(hud.totalWaves!!)}/${hud.totalWaves}"
        )
        PanelTitle("TEMPO")
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
        Modifier.width(width).fillMaxHeight().background(HudPanel)
            .border(1.dp, HudEdge.copy(alpha = 0.75f))
            .verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PanelTitle("BEFEHLE")
        val hasNextWave = hud.totalWaves == null || hud.waveIndex < hud.totalWaves!!
        if (hud.waitingForWaveStart && hasNextWave) {
            HudButton(
                label = when {
                    hud.waveIndex == 0 -> if (hud.totalWaves == null) "ENDLOSMODUS STARTEN" else "ANGRIFF STARTEN"
                    hud.earlyWaveBonusAvailable -> "NÄCHSTE WELLE (+${GameSimulator.EARLY_WAVE_BONUS_GOLD} Gold)"
                    else -> "NÄCHSTE WELLE"
                }, onClick = controller::startNextWave,
                modifier = Modifier.fillMaxWidth(), prominent = true
            )
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
            HudButton("${speed.toInt()}x", onClick = { onSelect(speed) },
                modifier = Modifier.fillMaxWidth(), prominent = current == speed)
        }
    }
}

@Composable
private fun HudText(text: String) {
    Text(text, color = HudIvory, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
}

@Composable
private fun HudStat(label: String, value: String) {
    Column(Modifier.fillMaxWidth().background(Color(0xAA1C1A17), HudCard)
        .border(1.dp, HudEdge.copy(alpha = 0.6f), HudCard)
        .padding(horizontal = 7.dp, vertical = 5.dp)) {
        Text(label, color = HudGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        HudText(value)
    }
}

@Composable
private fun BuildBar(gold: Int, onSelectType: (TowerType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PanelTitle("TÜRME")
        TowerType.entries.forEach { type ->
            HudButton("${type.displayName} · ${type.baseCost} Gold", onClick = { onSelectType(type) },
                enabled = gold >= type.baseCost, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PlacingBar(type: TowerType, onCancel: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        PanelTitle("BAUEN")
        HudText("Platziere ${type.displayName} – auf das Feld tippen")
        HudButton("Abbrechen", onClick = onCancel, modifier = Modifier.fillMaxWidth())
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
        PanelTitle("TURM")
        HudText("${tower.type.displayName} · Stufe ${tower.level}")
        tower.specialization?.let { Text(it.displayName, color = HudGold, fontSize = 12.sp) }
        HudButton("Schließen", onClick = onDeselect, modifier = Modifier.fillMaxWidth())
        if (tower.needsSpecializationChoice) {
            val cost = tower.upgradeCost()
            HudText("Spezialisierung wählen (${cost ?: 0} Gold):")
            Specialization.branchesFor(tower.type).forEach { spec ->
                HudButton(spec.displayName, onClick = { onUpgrade(spec) },
                    enabled = cost != null && gold >= cost, modifier = Modifier.fillMaxWidth(), prominent = true)
            }
        } else {
            val cost = tower.upgradeCost()
            if (cost != null) {
                HudButton("Upgrade ($cost Gold)", onClick = { onUpgrade(null) },
                    enabled = gold >= cost, modifier = Modifier.fillMaxWidth(), prominent = true)
            } else HudText("Maximalstufe erreicht")
            HudButton("Verkaufen", onClick = onSell, modifier = Modifier.fillMaxWidth())
        }
    }
}
