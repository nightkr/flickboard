package se.nullable.flickboard.ui

import android.content.SharedPreferences
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import se.nullable.flickboard.R

@Composable
fun BetaMenu(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appSettings = LocalAppSettings.current
    Column(modifier) {
        val itemModifier = Modifier.padding(8.dp)
        Text(
            "To open the secret beta menu in a release build, tap the version number 7 times",
            itemModifier,
        )
        Text(
            "All beta options are experimental, and you may still require a full reset. " +
                    "Sensitive settings will not be saved.",
            itemModifier,
        )
        val settingsMime = "application/json"
        val exportPicker =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(settingsMime)) { uri ->
                if (uri == null) {
                    return@rememberLauncherForActivityResult
                }
                JsonWriter(context.contentResolver.openOutputStream(uri)!!.bufferedWriter())
                    .use { json ->
                        json.beginObject()
                        appSettings.all.forEach { section ->
                            section.settings.forEach { setting ->
                                json.name(setting.key)
                                setting.exportToJson(appSettings.ctx.prefs, json)
                            }
                        }
                        json.endObject()
                    }
            }
        Button(onClick = { exportPicker.launch("flickboard-settings.json") }, itemModifier) {
            Text("Export settings")
        }
        val importPicker =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri == null) {
                    return@rememberLauncherForActivityResult
                }
                val settingsByKey = appSettings.all
                    .flatMap { it.settings }
                    .associateBy { it.key }
                try {
                    appSettings.ctx.prefs.edit {
                        val editPrefs = this
                        JsonReader(context.contentResolver.openInputStream(uri)!!.bufferedReader())
                            .use { json ->
                                json.beginObject()
                                while (json.peek() != JsonToken.END_OBJECT) {
                                    val key = json.nextName()
                                    val setting = settingsByKey[key]
                                    if (setting != null) {
                                        try {
                                            setting.importFromJson(editPrefs, json)
                                        } catch (err: Exception) {
                                            Log.w("BetaMenu", "Import failed for key $key", err)
                                        }
                                    } else {
                                        Log.w("BetaMenu", "Unable to import unknown key $key")
                                        json.skipValue()
                                    }
                                }
                                json.endObject()
                            }
                    }
                } catch (err: Exception) {
                    Log.w("BetaMenu", "Import failed", err)
                    Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                }
            }
        val showImportConfirmationDialog = remember { mutableStateOf(false) }
        if (showImportConfirmationDialog.value) {
            AlertDialog(
                icon = { Icon(painterResource(id = R.drawable.baseline_warning_24), "Warning") },
                title = { Text("Confirm import") },
                text = { Text("Import will overwrite existing settings. Proceed?") },
                onDismissRequest = { showImportConfirmationDialog.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showImportConfirmationDialog.value = false
                            importPicker.launch(arrayOf(settingsMime))
                        },
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportConfirmationDialog.value = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
        Button(onClick = { showImportConfirmationDialog.value = true }, itemModifier) {
            Text("Import settings")
        }
    }
}

private fun SharedPreferences.Editor.copyPreferencesFrom(from: SharedPreferences) {
    from.all.forEach { (key, value) ->
        // We have no way to detect StringSet specifically, thanks erasure...
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is String -> putString(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> throw Exception("unable to export $value")
        }
    }
}