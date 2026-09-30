package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.haberland.meitowerdefense.R

private val Gold = Color(0xFFFFD885)
private val Ivory = Color(0xFFFFF1D7)
private val Stone = Color(0xFF28231F)

@Composable
fun MainMenuScreen(onPlay: () -> Unit, onStarShop: () -> Unit, onGlossary: () -> Unit, onAchievements: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.meissen_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Keeps the text readable even where the landscape is bright on narrow screens.
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to Color(0xE90E1412),
                    0.33f to Color(0xB9151914),
                    0.61f to Color(0x240D100B),
                    1f to Color.Transparent
                )
            )
        )
        val compact = maxHeight < 440.dp
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
                .padding(start = if (compact) 56.dp else 72.dp, end = 16.dp)
                .widthIn(max = 370.dp)
                .heightIn(max = maxHeight - 24.dp)
                .displayCutoutPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "♜",
                color = Gold,
                fontSize = if (compact) 26.sp else 36.sp,
                lineHeight = if (compact) 28.sp else 38.sp
            )
            Text(
                text = "MeiTowerDefense",
                color = Ivory,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 27.sp else 33.sp,
                maxLines = 1,
                style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 8f))
            )
            Spacer(Modifier.height(if (compact) 12.dp else 28.dp))
            MenuButton("SPIELEN", prominent = true, compact = compact, onClick = onPlay)
            Spacer(Modifier.height(if (compact) 7.dp else 12.dp))
            MenuButton("STERNEN-SHOP", prominent = false, compact = compact, onClick = onStarShop)
            Spacer(Modifier.height(if (compact) 7.dp else 12.dp))
            MenuButton("ERFOLGE", prominent = false, compact = compact, onClick = onAchievements)
            Spacer(Modifier.height(if (compact) 7.dp else 12.dp))
            MenuButton("GLOSSAR", prominent = false, compact = compact, onClick = onGlossary)
        }
    }
}

@Composable
private fun MenuButton(label: String, prominent: Boolean, compact: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(if (compact) 48.dp else 56.dp)
            .shadow(5.dp, shape)
            .background(
                if (prominent) Brush.verticalGradient(listOf(Color(0xFFFFE5A4), Color(0xFFB9812E)))
                else Brush.verticalGradient(listOf(Color(0xFF413A32), Stone)), shape
            )
            .border(2.dp, if (prominent) Gold else Color(0xFFAA8D62), shape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (prominent) Color(0xFF2A1E0E) else Ivory,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 18.sp else 21.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
