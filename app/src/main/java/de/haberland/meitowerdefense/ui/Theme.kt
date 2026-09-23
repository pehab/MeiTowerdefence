package de.haberland.meitowerdefense.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TdColors = darkColorScheme(
    primary = Color(0xFF7BC96F),
    secondary = Color(0xFF6FA8C9),
    background = Color(0xFF10120E),
    surface = Color(0xFF181B14),
    surfaceVariant = Color(0xFF23271C)
)

@Composable
fun MeiTowerDefenseTheme(content: @Composable () -> Unit) {
    // Always dark, regardless of system theme: a bright system theme would clash badly
    // with the game canvas, which is drawn with its own fixed dark palette
    // (GameRenderer) no matter what the device is set to.
    MaterialTheme(colorScheme = TdColors, content = content)
}
