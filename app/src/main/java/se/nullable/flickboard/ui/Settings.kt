package se.nullable.flickboard.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Settings(modifier: Modifier = Modifier) {
    val appSettings = AppSettings.current
    Column(modifier) {
        appSettings.all.forEach { setting ->
            val state = setting.state
            Box(modifier = Modifier.clickable {
                setting.setValue(!state.value)
            }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = setting.label)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = state.value,
                        onCheckedChange = setting::setValue,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SettingsPreview() {
    Settings(Modifier.width(1000.dp))
}

class AppSettings(sharedPreferences: SharedPreferences) {
    val showLetters = BooleanSetting(
        key = "showLetters",
        label = "Show letters",
        defaultValue = true,
        prefs = sharedPreferences
    )

    val showSymbols = BooleanSetting(
        key = "showSymbols",
        label = "Show symbols",
        defaultValue = true,
        prefs = sharedPreferences
    )

    val showNumbers = BooleanSetting(
        key = "showNumbers",
        label = "Show numbers",
        defaultValue = true,
        prefs = sharedPreferences
    )

    val enableFastActions = BooleanSetting(
        key = "enableFastActions",
        label = "Enable fast actions",
        defaultValue = true,
        prefs = sharedPreferences
    )

    val all = listOf(showLetters, showSymbols, showNumbers, enableFastActions)

    companion object {
        val current: AppSettings
            @Composable
            get() = AppSettings(
                LocalContext.current.getSharedPreferences(
                    "flickboard",
                    Context.MODE_PRIVATE
                )
            )
    }
}

class BooleanSetting(
    val key: String,
    val label: String,
    val defaultValue: Boolean,
    private val prefs: SharedPreferences
) {
    fun setValue(value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    val state: State<Boolean>
        @Composable
        get() {
            val trigger = remember { mutableStateOf(Unit, policy = neverEqualPolicy()) }
            val state = remember {
                derivedStateOf {
                    trigger.value
                    prefs.getBoolean(key, defaultValue)
                }
            }
            DisposableEffect(key, defaultValue) {
                val listener = { sharedPreferences: SharedPreferences, key: String? ->
                    if (key == this@BooleanSetting.key) {
                        trigger.value = Unit
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
            return state
        }
}
