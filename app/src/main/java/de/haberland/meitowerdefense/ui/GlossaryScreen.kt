package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.TypeColors

/**
 * A standalone full screen (menu route) and an in-game dialog (see [GlossaryDialog], used
 * from GameHud) both render the same [GlossaryContent], so the legend can never drift
 * between the two places it's shown.
 */
@Composable
fun GlossaryScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Glossar", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Zurück") }
        }
        GlossaryContent(Modifier.fillMaxSize().padding(top = 12.dp))
    }
}

@Composable
fun GlossaryDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Glossar") },
        text = { GlossaryContent(Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
private fun GlossaryContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Gegner", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(EnemyType.entries) { type ->
            GlossaryRow(
                color = Color(TypeColors.enemyColor(type)),
                shape = CircleShape,
                name = type.displayName,
                detail = buildString {
                    append("HP ${type.baseHp} · Tempo ${"%.1f".format(type.baseSpeed)}")
                    append(if (type.flying) " · fliegt" else " · Boden")
                    if (type.livesCost > 1) append(" · kostet ${type.livesCost} Leben")
                }
            )
        }
        item { HorizontalDivider(Modifier.padding(vertical = 4.dp)) }
        item { Text("Türme", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(TowerType.entries) { type ->
            GlossaryRow(
                color = Color(TypeColors.towerColor(type)),
                shape = RoundedCornerShape(4.dp),
                name = type.displayName,
                detail = "${type.baseCost} Gold · ${if (type.canHitFlying) "trifft Boden + Luft" else "nur Boden"}"
            )
        }
    }
}

@Composable
private fun GlossaryRow(color: Color, shape: Shape, name: String, detail: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).background(color, shape))
        Column(Modifier.padding(start = 12.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
