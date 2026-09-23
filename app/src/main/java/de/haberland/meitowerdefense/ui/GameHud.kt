package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.sim.Tower

@Composable
fun GameHud(controller: GameController, onExit: () -> Unit) {
    val hud by controller.hudState
    val mode = controller.inputMode

    // One Column filling the whole overlay: top bar, a weighted Spacer that pushes
    // everything below it down, then the bottom panel - the game canvas shows through
    // in the middle, since this whole overlay has a transparent background apart from
    // the two bars themselves. Callers place this inside a Box(Modifier.fillMaxSize())
    // alongside the game canvas (see GameActivity).
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) { Icon(Icons.Default.Close, "Verlassen", tint = Color.White) }
            HudText("Gold: ${hud.gold}")
            HudText("Leben: ${hud.lives}")
            HudText("Welle ${hud.waveIndex.coerceAtMost(hud.totalWaves)}/${hud.totalWaves}")
        }

        Spacer(Modifier.weight(1f))

        Row(
            Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
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

@Composable
private fun HudText(text: String) {
    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
            HudText("Spezialisierung wählen:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Specialization.branchesFor(tower.type).forEach { spec ->
                    val cost = tower.upgradeCost()
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
