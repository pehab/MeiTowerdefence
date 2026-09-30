package de.haberland.meitowerdefense.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SaveErrorDialog(viewModel: MetaViewModel) {
    val error = viewModel.saveError ?: return
    AlertDialog(
        onDismissRequest = viewModel::dismissSaveError,
        title = { Text("Spielstand") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = { viewModel.retrySave() }) { Text("ERNEUT VERSUCHEN") }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissSaveError) { Text("SCHLIESSEN") }
        }
    )
}
