package de.haberland.meitowerdefense.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import de.haberland.meitowerdefense.R

private val InfoGold = Color(0xFFFFD885)
private val InfoIvory = Color(0xFFFFF1D7)
private val InfoMuted = Color(0xFFE2D7C4)
private val InfoEdge = Color(0xFFAA8D62)

@Composable
fun InfoScreen(metaViewModel: MetaViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val developer = stringResource(R.string.developer_name)
    val email = stringResource(R.string.contact_email)
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    val version = remember(context) {
        @Suppress("DEPRECATION")
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName ?: "Unbekannt"} (${PackageInfoCompat.getLongVersionCode(info)})"
    }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.meissen_menu), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
            listOf(Color(0xEB101411), Color(0xD3171915), Color(0x8D151812)))))
        Column(Modifier.fillMaxSize().displayCutoutPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Info", color = InfoIvory, fontFamily = FontFamily.Serif,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("‹ Zurück", color = InfoGold, fontSize = 17.sp) }
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {
                val shape = RoundedCornerShape(8.dp)
                Column(Modifier.widthIn(max = 620.dp).fillMaxWidth()
                    .background(Color(0xF026241F), shape).border(1.dp, InfoEdge, shape).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MeiTowerDefense", color = InfoIvory, fontFamily = FontFamily.Serif,
                        fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Version $version", color = InfoGold, fontSize = 16.sp)
                    Text("Entwickelt von $developer", color = InfoIvory, fontSize = 17.sp)
                    SelectionContainer { Text(email, color = InfoGold, fontSize = 16.sp) }
                    Text("Fragen, Fehler oder Ideen? Schreib mir gern. Bei Fehlern hilft die Angabe der App-Version.",
                        color = InfoMuted, fontSize = 14.sp, lineHeight = 19.sp)
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
                            .putExtra(Intent.EXTRA_SUBJECT, "MeiTowerDefense $version – Kontakt")
                        feedback = if (openInfoIntent(context, intent)) null
                        else "Keine E-Mail-App verfügbar. Du kannst die Adresse kopieren."
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9812E), contentColor = Color(0xFF21170A))) {
                        Text("E-Mail schreiben")
                    }
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Kontakt", email))
                        feedback = "E-Mail-Adresse kopiert."
                    }) { Text("E-Mail-Adresse kopieren", color = InfoGold) }
                    TextButton(onClick = {
                        feedback = if (openInfoIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))) null
                        else "Kein Browser verfügbar."
                    }) { Text("Datenschutzerklärung öffnen", color = InfoGold) }
                    HorizontalDivider(color = InfoEdge)
                    Text("Neu anfangen", color = InfoIvory, fontFamily = FontFamily.Serif,
                        fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Setzt Sterne, Upgrades, Levelwertungen, Freischaltungen, Erfolge und Endlosrekorde zurück.",
                        color = InfoMuted, fontSize = 14.sp, lineHeight = 19.sp)
                    TextButton(onClick = { showResetConfirmation = true }) {
                        Text("Gesamten Fortschritt zurücksetzen", color = Color(0xFFFFB4AB))
                    }
                    feedback?.let { Text(it, color = InfoMuted, fontSize = 14.sp) }
                }
            }
        }
    }
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Gesamten Fortschritt zurücksetzen?") },
            text = { Text("Alle Sterne, permanenten Upgrades, Levelwertungen, Freischaltungen, Erfolgsfortschritte und Endlosrekorde werden gelöscht. Danach ist nur Waldpfad freigeschaltet. Das lässt sich nicht rückgängig machen.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirmation = false
                    feedback = if (metaViewModel.resetAllProgress()) "Fortschritt zurückgesetzt."
                    else "Das Zurücksetzen ist noch nicht gespeichert. Bitte erneut versuchen."
                }) { Text("ALLES ZURÜCKSETZEN", color = Color(0xFFB3261E)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("ABBRECHEN") }
            }
        )
    }
}

private fun openInfoIntent(context: Context, intent: Intent): Boolean = try {
    context.startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
