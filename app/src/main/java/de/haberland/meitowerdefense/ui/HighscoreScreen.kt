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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import de.haberland.meitowerdefense.leaderboard.FirebaseLeaderboardRepository
import de.haberland.meitowerdefense.leaderboard.LeaderboardEntry
import de.haberland.meitowerdefense.leaderboard.LeaderboardRepository

private val HighscoreGold = Color(0xFFFFD885)
private val HighscoreIvory = Color(0xFFFFF1D7)
private val HighscoreMuted = Color(0xFFE2D7C4)
private val HighscoreEdge = Color(0xFFAA8D62)

@Composable
fun HighscoreScreen(
    onBack: () -> Unit,
    repository: LeaderboardRepository? = null
) {
    val source = remember(repository) { repository ?: FirebaseLeaderboardRepository() }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(reload) {
        entries = null
        error = null
        source.loadTop(
            onSuccess = { entries = it },
            onError = { error = "Die Highscores konnten nicht geladen werden. Prüfe deine Verbindung und versuche es erneut." }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.meissen_menu), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
            listOf(Color(0xEB101411), Color(0xD3171915), Color(0x8D151812)))))
        Column(Modifier.fillMaxSize().displayCutoutPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Highscores", color = HighscoreIvory, fontFamily = FontFamily.Serif,
                        fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Endlosmodus · vollständig überstandene Wellen", color = HighscoreMuted, fontSize = 13.sp)
                }
                TextButton(onClick = onBack) { Text("‹ Zurück", color = HighscoreGold, fontSize = 17.sp) }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(error!!, color = HighscoreIvory, fontSize = 15.sp)
                        Button(onClick = { reload++ }, colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB9812E), contentColor = Color(0xFF21170A))) { Text("ERNEUT VERSUCHEN") }
                    }
                    entries == null -> CircularProgressIndicator(color = HighscoreGold)
                    entries!!.isEmpty() -> Text("Noch keine veröffentlichten Rekorde.", color = HighscoreIvory, fontSize = 16.sp)
                    else -> {
                        val shape = RoundedCornerShape(8.dp)
                        LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxWidth()
                            .background(Color(0xF026241F), shape).border(1.dp, HighscoreEdge, shape)
                            .padding(horizontal = 14.dp, vertical = 8.dp)) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                                    Text("Rang", color = HighscoreGold, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
                                    Text("Name", color = HighscoreGold, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.55f))
                                    Text("Punkte", color = HighscoreGold, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))
                                }
                            }
                            itemsIndexed(entries!!, key = { index, entry -> "$index-${entry.name}-${entry.score}" }) { index, entry ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text("${index + 1}.", color = HighscoreMuted, modifier = Modifier.weight(0.2f))
                                    Text(entry.name, color = HighscoreIvory, maxLines = 1, modifier = Modifier.weight(0.55f))
                                    Text("${entry.score}", color = HighscoreIvory, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
