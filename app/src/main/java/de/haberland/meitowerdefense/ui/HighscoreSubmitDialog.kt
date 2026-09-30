package de.haberland.meitowerdefense.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.haberland.meitowerdefense.leaderboard.LeaderboardInput
import de.haberland.meitowerdefense.leaderboard.LeaderboardRepository

@Composable
fun HighscoreSubmitDialog(
    score: Int,
    initialName: String,
    repository: LeaderboardRepository,
    onPosted: (String) -> Unit,
    onSkip: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val normalized = LeaderboardInput.normalizeName(name)
    val valid = LeaderboardInput.validName(normalized) && LeaderboardInput.validScore(score)
    val focus = LocalFocusManager.current

    fun submit() {
        if (!valid || submitting) return
        focus.clearFocus()
        submitting = true
        error = null
        repository.submitPersonalBest(normalized, score,
            onSuccess = { onPosted(normalized) },
            onError = {
                submitting = false
                error = "Der Rekord konnte nicht veröffentlicht werden. Prüfe deine Verbindung und versuche es erneut."
            })
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Neuer persönlicher Rekord!") },
        text = {
            Column {
                Text("Du hast $score Punkte erreicht. Möchtest du den Rekord in der öffentlichen Highscoreliste veröffentlichen?")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(LeaderboardInput.MAX_NAME_LENGTH); error = null },
                    label = { Text("Name") },
                    supportingText = { Text("2–${LeaderboardInput.MAX_NAME_LENGTH} Zeichen · öffentlich sichtbar") },
                    singleLine = true,
                    enabled = !submitting,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
                error?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }, enabled = valid && !submitting) {
                Text(if (submitting) "WIRD GESENDET …" else "VERÖFFENTLICHEN")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip, enabled = !submitting) { Text("NICHT VERÖFFENTLICHEN") }
        }
    )
}
