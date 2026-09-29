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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import de.haberland.meitowerdefense.R
import de.haberland.meitowerdefense.model.MetaUpgradeType

private val ShopGold = Color(0xFFFFD885)
private val ShopIvory = Color(0xFFFFF1D7)
private val ShopCard = Color(0xF026241F)

@Composable
fun StarShopScreen(metaViewModel: MetaViewModel, onBack: () -> Unit) {
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
                Text("Sternen-Shop", color = ShopIvory, fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("‹ Zurück", color = ShopGold, fontSize = 17.sp) }
            }
            Text("★ ${metaViewModel.meta.stars} verfügbar", color = ShopGold, fontSize = 16.sp)
            val atlas = ImageBitmap.imageResource(R.drawable.upgrade_atlas)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(245.dp),
                modifier = Modifier.fillMaxSize().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MetaUpgradeType.entries, key = { it.name }) { type ->
                    UpgradeCard(
                        type = type,
                        atlas = atlas,
                        level = metaViewModel.meta.levelOf(type),
                        availableStars = metaViewModel.meta.stars,
                        onPurchase = { metaViewModel.purchaseUpgrade(type) },
                        onReset = { metaViewModel.resetUpgrade(type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    type: MetaUpgradeType,
    atlas: ImageBitmap,
    level: Int,
    availableStars: Int,
    onPurchase: () -> Unit,
    onReset: () -> Unit
) {
    val index = type.ordinal
    val cellWidth = atlas.width / 3
    val cellHeight = atlas.height / 2
    val artwork = remember(atlas, index) {
        BitmapPainter(atlas, IntOffset((index % 3) * cellWidth, (index / 3) * cellHeight), IntSize(cellWidth, cellHeight))
    }
    val cost = type.costForNextLevel(level)
    val shape = RoundedCornerShape(8.dp)
    Column(Modifier.fillMaxWidth().background(ShopCard, shape).border(1.dp, Color(0xFFAA8D62), shape)) {
        Image(
            painter = artwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(96.dp)
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(type.displayName, color = ShopIvory, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text(type.description, color = Color(0xFFE2D7C4), fontSize = 13.sp, minLines = 2, lineHeight = 16.sp)
            Text("Stufe $level/${type.maxLevel}", color = ShopGold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (level > 0) {
                    TextButton(onClick = onReset) { Text("Zurücksetzen", color = ShopIvory, fontSize = 12.sp) }
                } else {
                    Box(Modifier.weight(1f))
                }
                if (cost != null) {
                    Button(
                        onClick = onPurchase,
                        enabled = availableStars >= cost,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9812E), contentColor = Color(0xFF21170A)),
                        modifier = Modifier.widthIn(min = 76.dp)
                    ) { Text("$cost ★", maxLines = 1) }
                } else {
                    Text("MAXIMAL", color = ShopGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
