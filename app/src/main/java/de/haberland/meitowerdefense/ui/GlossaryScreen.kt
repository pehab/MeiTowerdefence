package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.model.EnemyType
import de.haberland.meitowerdefense.model.Specialization
import de.haberland.meitowerdefense.model.TowerType

private val GlossaryGold = Color(0xFFFFD885)
private val GlossaryIvory = Color(0xFFFFF1D7)
private val GlossaryStone = Color(0xE923211D)
private val GlossaryEdge = Color(0xFFAA8D62)

/** The menu and in-game dialog share the same descriptions and artwork. */
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
                    .border(1.dp, GlossaryEdge, RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GlossaryContent(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun GlossaryDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth(0.82f).widthIn(max = 800.dp)
                    .fillMaxHeight(0.88f)
                    .background(GlossaryStone, RoundedCornerShape(10.dp))
                    .border(2.dp, GlossaryEdge, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Glossar", color = GlossaryIvory, fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    TextButton(onClick = onDismiss) { Text("Schließen", color = GlossaryGold) }
                }
                GlossaryContent(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun GlossaryContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Türme", color = GlossaryGold, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        item { Text("Ab Stufe 3 wählst du beim nächsten Upgrade einen von zwei Zweigen. Bei gleicher Zielpriorität wird der Gegner beschossen, der die Basis bei normalem Tempo zuerst erreicht.",
            color = GlossaryIvory, fontSize = 13.sp) }
        items(TowerType.entries, key = { "tower-${it.name}" }) { type -> TowerGlossaryCard(type) }
        item { HorizontalDivider(Modifier.padding(vertical = 4.dp), color = GlossaryEdge) }
        item {
            Text("Gegner", color = GlossaryGold, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        items(EnemyType.entries, key = { "enemy-${it.name}" }) { type ->
            EnemyGlossaryRow(type)
        }
    }
}

@Composable
private fun TowerGlossaryCard(type: TowerType) {
    val atlas = ImageBitmap.imageResource(towerAtlas(type))
    Column(Modifier.fillMaxWidth().background(Color(0xD031302B), RoundedCornerShape(8.dp))
        .border(1.dp, GlossaryEdge.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
        .padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TowerArtwork(atlas, 0, Modifier.size(64.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(type.displayName, color = GlossaryIvory, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${type.baseCost} Gold · ${if (type.canHitFlying) "Boden + Luft" else "nur Boden"}",
                    color = GlossaryGold, fontSize = 12.sp)
                Text(towerBaseStats(type), color = GlossaryIvory, fontSize = 12.sp, lineHeight = 15.sp)
                Text(towerDescription(type), color = GlossaryIvory, fontSize = 13.sp, lineHeight = 17.sp)
                Text(targetingDescription(type), color = GlossaryGold, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
        Specialization.branchesFor(type).forEachIndexed { index, spec ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                TowerArtwork(atlas, index + 1, Modifier.size(52.dp))
                Column(Modifier.padding(start = 10.dp)) {
                    Text(spec.displayName, color = GlossaryGold, fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(specializationStats(spec), color = GlossaryIvory,
                        fontSize = 12.sp, lineHeight = 15.sp)
                    Text(specializationDescription(spec), color = GlossaryIvory,
                        fontSize = 12.sp, lineHeight = 15.sp)
                }
            }
        }
    }
}

private fun towerAtlas(type: TowerType): Int = when (type) {
    TowerType.ARCHER -> R.drawable.tower_archer_atlas
    TowerType.CANNON -> R.drawable.tower_cannon_atlas
    TowerType.FIRE -> R.drawable.tower_fire_atlas
    TowerType.ICE -> R.drawable.tower_ice_atlas
}

@Composable
private fun TowerArtwork(atlas: ImageBitmap, index: Int, modifier: Modifier) {
    val cellWidth = atlas.width / 3
    val painter = remember(atlas, index) {
        BitmapPainter(atlas, IntOffset(index * cellWidth, 0), IntSize(cellWidth, atlas.height))
    }
    Image(painter = painter, contentDescription = null, contentScale = ContentScale.Fit, modifier = modifier)
}

private fun targetingDescription(type: TowerType): String = when (type) {
    TowerType.ARCHER -> "Zielwahl: bevorzugt Flieger in Reichweite."
    TowerType.CANNON -> "Zielwahl: bevorzugt die größte Bodengruppe im Explosionsradius."
    TowerType.FIRE -> "Zielwahl: bevorzugt Gegner ohne Brand, berücksichtigt anfliegende Feuerschüsse."
    TowerType.ICE -> "Zielwahl: bevorzugt ungefrorene Gegner und verteilt Verlangsamung auf neue Ziele."
}

private fun towerBaseStats(type: TowerType): String = buildString {
    append("Schaden ${fmt(type.baseDamage)} · Feuerrate ${fmt(type.baseFireRate)}/s · Reichweite ${fmt(type.baseRange)}")
    if (type.baseSplashRadius > 0f) append(" · Splash ${fmt(type.baseSplashRadius)}")
    if (type.burnDamagePerSecond > 0f) append(" · Brand ${fmt(type.burnDamagePerSecond)}/s für ${fmt(type.burnDurationSeconds)} s")
    if (type.slowFactor > 0f) append(" · Slow ${(type.slowFactor * 100).toInt()} % für ${fmt(type.slowDurationSeconds)} s")
}

private fun specializationStats(spec: Specialization): String = buildString {
    val parts = mutableListOf<String>()
    if (spec.damageMultiplier != 1f) parts += "Schaden ×${fmt(spec.damageMultiplier)}"
    if (spec.fireRateMultiplier != 1f) parts += "Feuerrate ×${fmt(spec.fireRateMultiplier)}"
    if (spec.rangeMultiplier != 1f) parts += "Reichweite ×${fmt(spec.rangeMultiplier)}"
    if (spec.splashRadiusBonus != 0f) parts += "Splash +${fmt(spec.splashRadiusBonus)}"
    if (spec.splashOnHitBonus != 0f) parts += "Treffer-Splash +${fmt(spec.splashOnHitBonus)}"
    if (spec.armorPierce != 0) parts += "Rüstungsdurchdringung ${spec.armorPierce}"
    if (spec.extraTargetChance != 0f) parts += "Zweitziel ${(spec.extraTargetChance * 100).toInt()} %"
    if (spec.freezeChanceBonus != 0f) parts += "Freeze ${(spec.freezeChanceBonus * 100).toInt()} %"
    if (spec.slowDurationBonus != 0f) parts += "Slow-Dauer +${fmt(spec.slowDurationBonus)} s"
    if (spec.slowFactorBonus != 0f) parts += "Slow +${(spec.slowFactorBonus * 100).toInt()} %"
    if (spec.burnDamageBonus != 0f) parts += "Brand +${fmt(spec.burnDamageBonus)}/s"
    if (spec.burnDurationBonus != 0f) parts += "Branddauer +${fmt(spec.burnDurationBonus)} s"
    append(parts.joinToString(" · "))
}

private fun fmt(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else "%.2f".format(value).trimEnd('0').trimEnd(',')

private fun towerDescription(type: TowerType): String = when (type) {
    TowerType.ARCHER -> "Schnelle Einzelschüsse auf Boden- und Fluggegner. Hohe Reichweite; nützlich gegen Flieger."
    TowerType.CANNON -> "Langsame, starke Geschosse mit Flächenschaden. Trifft nur Bodengegner."
    TowerType.FIRE -> "Setzt Bodengegner in Brand: zusätzlich 6 Schaden pro Sekunde für 3 Sekunden. Treffer und Brand umgehen Rüstung."
    TowerType.ICE -> "Trifft Boden und Luft und senkt das Bewegungstempo für 2 Sekunden um 40 %. Eisschaden umgeht Rüstung."
}

private fun specializationDescription(spec: Specialization): String = when (spec) {
    Specialization.ARCHER_SNIPER -> "+80 % Schaden, 30 % langsamer; ignoriert 6 Rüstung."
    Specialization.ARCHER_RAPID -> "+70 % Feuerrate; 35 % Chance auf einen Schuss gegen ein zweites Ziel."
    Specialization.CANNON_SIEGE -> "Größerer Explosionsradius (+0,6 Felder); ignoriert 8 Rüstung."
    Specialization.CANNON_MORTAR -> "Sehr großer Explosionsradius (+1,2 Felder), 30 % mehr Reichweite, 25 % weniger Schaden."
    Specialization.FIRE_INFERNO -> "Brandschaden +6 pro Sekunde; das Feuer brennt 2 Sekunden länger."
    Specialization.FIRE_SCORCH -> "+30 % direkter Schaden und Flächenschaden im Radius von 1 Feld."
    Specialization.ICE_DEEP_FREEZE -> "25 % Chance auf vollständiges Einfrieren; Verlangsamung hält 1,5 Sekunden länger."
    Specialization.ICE_FROSTBITE -> "Verlangsamt um 60 % und verursacht 2 Brandschaden pro Sekunde für 2 Sekunden."
}

@Composable
private fun EnemyGlossaryRow(type: EnemyType) {
    val atlas = ImageBitmap.imageResource(R.drawable.enemy_atlas)
    val cellWidth = atlas.width / EnemyType.entries.size
    val painter = remember(atlas, type) {
        BitmapPainter(atlas, IntOffset(type.ordinal * cellWidth, 0), IntSize(cellWidth, atlas.height))
    }
    Row(
        Modifier.fillMaxWidth().background(Color(0xB831302B), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0x665F5140), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter, contentDescription = null, contentScale = ContentScale.Fit,
            modifier = Modifier.size(if (type == EnemyType.BOSS) 62.dp else 50.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(type.displayName, color = GlossaryIvory, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(buildString {
                append("HP ${type.baseHp} · Tempo ${"%.1f".format(type.baseSpeed)}")
                if (type.armor > 0) append(" · Rüstung ${type.armor}")
                append(if (type.flying) " · fliegt" else " · Boden")
                append(" · ${type.goldReward} Gold")
                if (type.livesCost > 1) append(" · kostet ${type.livesCost} Leben")
            }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
