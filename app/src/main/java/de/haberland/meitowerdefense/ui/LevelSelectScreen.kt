package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.content.LevelCatalog
import de.haberland.meitowerdefense.model.LevelDefinition

private val LevelGold = Color(0xFFFFD885)
private val LevelIvory = Color(0xFFFFF1D7)
private val LevelStone = Color(0xF026241F)

@Composable
fun LevelSelectScreen(
    metaViewModel: MetaViewModel,
    onBack: () -> Unit,
    onPlayLevel: (String) -> Unit,
    onPlayEndless: () -> Unit
) {
    val allBeaten = LevelCatalog.all.all { metaViewModel.bestStars(it) > 0 }
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.meissen_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xEB101411), Color(0xD3171915), Color(0x8D151812)))
        ))
        Column(Modifier.fillMaxSize().padding(start = 64.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Level wählen", color = LevelIvory, fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("‹ Zurück", color = LevelGold, fontSize = 17.sp) }
            }
            val atlas = ImageBitmap.imageResource(R.drawable.level_atlas)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(195.dp),
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(LevelCatalog.all, key = { it.id }) { level ->
                    val index = LevelCatalog.all.indexOf(level)
                    val unlocked = metaViewModel.isUnlocked(level)
                    LevelCard(
                        level = level,
                        index = index,
                        atlas = atlas,
                        unlocked = unlocked,
                        stars = metaViewModel.bestStars(level),
                        onClick = { onPlayLevel(level.id) }
                    )
                }
                item(key = "endless") {
                    val shape = RoundedCornerShape(8.dp)
                    Column(
                        Modifier.fillMaxWidth().background(LevelStone, shape)
                            .border(1.dp, Color(0xFFAA8D62), shape)
                            .clickable(enabled = allBeaten, role = Role.Button, onClickLabel = "Unendlich", onClick = onPlayEndless)
                    ) {
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(0.85f)
                                .background(Brush.verticalGradient(listOf(Color(0xFF593B2C), Color(0xFF151D1B)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("∞", color = LevelGold, fontFamily = FontFamily.Serif, fontSize = 82.sp)
                        }
                        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text("Unendlich", color = LevelIvory, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (allBeaten) "Beste Welle: ${metaViewModel.endlessBestWave}" else "Schließe alle Level ab",
                                color = if (allBeaten) LevelGold else Color(0xFFC7B9A2),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: LevelDefinition, index: Int, atlas: ImageBitmap, unlocked: Boolean, stars: Int, onClick: () -> Unit) {
    val cellWidth = atlas.width / 4
    val cellHeight = atlas.height / 2
    val artwork = remember(atlas, index) {
        BitmapPainter(atlas, IntOffset((index % 4) * cellWidth, (index / 4) * cellHeight), IntSize(cellWidth, cellHeight))
    }
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.fillMaxWidth().background(LevelStone, shape).border(1.dp, Color(0xFFAA8D62), shape)
            .clickable(enabled = unlocked, role = Role.Button, onClickLabel = level.displayName, onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.85f).background(Color(0xFF171611))) {
            Image(
                painter = artwork,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            if (!unlocked) Box(Modifier.fillMaxSize().background(Color(0x880D0E0C)))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(level.displayName, color = LevelIvory, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                if (unlocked) starString(stars) else "🔒 Gesperrt",
                color = if (unlocked) LevelGold else Color(0xFFC7B9A2),
                fontSize = 15.sp,
                maxLines = 1
            )
        }
    }
}

private fun starString(stars: Int): String = "★".repeat(stars) + "☆".repeat((3 - stars).coerceAtLeast(0))
