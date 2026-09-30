package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.content.AchievementCatalog
import de.haberland.meitowerdefense.content.AchievementTrack
import java.text.NumberFormat
import java.util.Locale

private val AchievementGold = Color(0xFFFFD885)
private val AchievementIvory = Color(0xFFFFF1D7)
private val AchievementMuted = Color(0xFFE2D7C4)
private val AchievementEdge = Color(0xFFAA8D62)
private fun countText(value: Int): String = NumberFormat.getIntegerInstance(Locale.GERMANY).format(value)

@Composable
fun AchievementsScreen(metaViewModel: MetaViewModel, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.meissen_menu), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
            listOf(Color(0xEB101411), Color(0xD3171915), Color(0x8D151812)))))
        Column(Modifier.fillMaxSize().displayCutoutPadding()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Erfolge", color = AchievementIvory, fontFamily = FontFamily.Serif,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("‹ Zurück", color = AchievementGold, fontSize = 17.sp) }
            }
            Text("${metaViewModel.claimedAchievements.size}/${AchievementCatalog.milestoneCount} erreicht · " +
                "★ ${metaViewModel.totalAchievementStars} verdient", color = AchievementGold, fontSize = 15.sp)
            Text("Sterne werden automatisch gutgeschrieben. Fortschritt zählt über alle Partien hinweg.",
                color = AchievementMuted, fontSize = 13.sp)
            LazyVerticalGrid(columns = GridCells.Adaptive(280.dp),
                modifier = Modifier.weight(1f).padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(AchievementCatalog.tracks, key = { it.id }) { track ->
                    AchievementCard(track, metaViewModel.achievementValue(track), metaViewModel.claimedAchievements)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(track: AchievementTrack, value: Int, claimed: Set<String>) {
    val shape = RoundedCornerShape(8.dp)
    val next = track.thresholds.indices.firstOrNull { track.milestoneId(it) !in claimed }
    Column(Modifier.fillMaxWidth().background(Color(0xF026241F), shape)
        .border(1.dp, AchievementEdge, shape).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(track.title, color = AchievementIvory, fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(track.description, color = AchievementMuted, fontSize = 13.sp, lineHeight = 17.sp)
        if (next == null) {
            Text("✓ Alle Ziele erreicht · ${countText(value)} insgesamt", color = AchievementGold, fontSize = 13.sp)
        } else {
            val target = track.thresholds[next]
            Text("Nächstes Ziel: ${countText(value)} / ${countText(target)}", color = AchievementGold, fontSize = 13.sp)
            LinearProgressIndicator(progress = { (value.toFloat() / target).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(), color = AchievementGold, trackColor = Color(0xFF4A4236))
        }
        track.thresholds.forEachIndexed { index, threshold ->
            val achieved = track.milestoneId(index) in claimed
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (achieved) "✓" else "○"} ${countText(threshold)}",
                    color = if (achieved) AchievementGold else AchievementMuted, fontSize = 13.sp)
                Text("★ ${track.rewards[index]} ${if (achieved) "erhalten" else "Belohnung"}",
                    color = if (achieved) AchievementGold else AchievementMuted, fontSize = 13.sp)
            }
        }
    }
}
