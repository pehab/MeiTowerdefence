package de.haberland.meitowerdefense.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun LevelEndDialog(won: Boolean, stars: Int, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(if (won) "Level geschafft!" else "Niederlage") },
        text = {
            Text(
                if (won) "★".repeat(stars) + "☆".repeat((3 - stars).coerceAtLeast(0)) else "Die Basis wurde überrannt.",
                fontSize = 22.sp
            )
        },
        confirmButton = { Button(onClick = onDone) { Text("Weiter") } }
    )
}
