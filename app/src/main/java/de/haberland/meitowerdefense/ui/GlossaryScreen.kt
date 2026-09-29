package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.TowerType
import de.haberland.meitowerdefense.model.TypeColors

private val GlossaryGold = Color(0xFFFFD885)
private val GlossaryIvory = Color(0xFFFFF1D7)
private val GlossaryStone = Color(0xE923211D)

/** The menu and in-game dialog share their legend entries. */
@Composable
fun GlossaryScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.meissen_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xEC101411), Color(0xC3171915), Color(0x80151812)))
        ))
        Column(Modifier.fillMaxSize().padding(start = 64.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Glossar", color = GlossaryIvory, fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("‹ Zurück", color = GlossaryGold, fontSize = 17.sp) }
            }
            Box(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxSize()
                    .background(GlossaryStone, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFAA8D62), RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GlossaryContent(Modifier.fillMaxSize())
            }
        }
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
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Gegner", color = GlossaryGold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp) }
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
        item { HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color(0xFFAA8D62)) }
        item { Text("Türme", color = GlossaryGold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp) }
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
    Row(
        Modifier.fillMaxWidth().background(Color(0xB831302B), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x665F5140), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(22.dp).background(color, shape).border(1.dp, GlossaryIvory.copy(alpha = 0.6f), shape))
        Column(Modifier.padding(start = 12.dp)) {
            Text(name, color = GlossaryIvory, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
