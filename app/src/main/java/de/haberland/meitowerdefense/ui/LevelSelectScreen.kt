package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.content.LevelCatalog

@Composable
fun LevelSelectScreen(
    metaViewModel: MetaViewModel,
    onBack: () -> Unit,
    onPlayLevel: (String) -> Unit,
    onPlayEndless: () -> Unit
) {
    val allBeaten = LevelCatalog.all.all { metaViewModel.bestStars(it) > 0 }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Level wählen", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Zurück") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(LevelCatalog.all) { level ->
                val unlocked = metaViewModel.isUnlocked(level)
                val stars = metaViewModel.bestStars(level)
                Card(
                    modifier = Modifier.fillMaxWidth().let { if (unlocked) it.clickable { onPlayLevel(level.id) } else it },
                    colors = CardDefaults.cardColors(containerColor = if (unlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(level.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (!unlocked) {
                                Text("Gesperrt", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                        if (unlocked) {
                            Text(starString(stars), fontSize = 18.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().let { if (allBeaten) it.clickable { onPlayEndless() } else it },
                    colors = CardDefaults.cardColors(containerColor = if (allBeaten) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unendlich", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            if (allBeaten) "Beste Welle: ${metaViewModel.endlessBestWave}" else "Schließe alle Level ab",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun starString(stars: Int): String = "★".repeat(stars) + "☆".repeat((3 - stars).coerceAtLeast(0))
