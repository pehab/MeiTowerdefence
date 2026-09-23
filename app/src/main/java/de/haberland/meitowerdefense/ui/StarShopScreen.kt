package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.model.MetaUpgradeType

@Composable
fun StarShopScreen(metaViewModel: MetaViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sternen-Shop", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Zurück") }
        }
        Text("★ ${metaViewModel.meta.stars}", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(MetaUpgradeType.entries) { type ->
                val level = metaViewModel.meta.levelOf(type)
                val cost = type.costForNextLevel(level)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(type.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(type.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Stufe $level/${type.maxLevel}")
                            if (cost != null) {
                                Button(
                                    onClick = { metaViewModel.purchaseUpgrade(type) },
                                    enabled = metaViewModel.meta.stars >= cost
                                ) { Text("$cost ★") }
                            } else {
                                Text("MAXIMAL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
