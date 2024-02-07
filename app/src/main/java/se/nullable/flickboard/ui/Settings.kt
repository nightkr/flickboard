package se.nullable.flickboard.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.shareIn
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.layouts.DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.SV_MESSAGEASE
import kotlin.math.roundToInt

@Composable
fun Settings(modifier: Modifier = Modifier) {
    val appSettings = LocalAppSettings.current
    Column(modifier) {
        appSettings.all.forEach { setting ->
            when (setting) {
                is Setting.Bool -> BoolSetting(setting)
                is Setting.FloatSlider -> FloatSliderSetting(setting)
            }
        }
    }
}

@Composable
fun BoolSetting(setting: Setting.Bool) {
    val state = setting.state
    Box(modifier = Modifier.clickable {
        setting.currentValue = !state.value
    }
    ) {
        SettingRow {
            SettingLabel(setting)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = state.value,
                onCheckedChange = { setting.currentValue = it },
            )
        }
    }
}

@Composable
fun FloatSliderSetting(setting: Setting.FloatSlider) {
    val state = setting.state
    SettingRow {
        Column {
            Row {
                SettingLabel(setting)
                Spacer(Modifier.weight(1f))
                Text(text = state.value.roundToInt().toString())
            }
            Slider(
                value = state.value,
                onValueChange = { setting.currentValue = it },
                valueRange = setting.range
            )
        }
    }
}

@Composable
fun SettingRow(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        content()
    }
}

@Composable
fun SettingLabel(setting: Setting<*>) {
    Text(text = setting.label)
}

@Composable
@Preview
fun SettingsPreview() {
    FlickBoardParent {
        Surface {
            Settings(Modifier.width(1000.dp))
        }
    }
}

val LocalAppSettings = staticCompositionLocalOf<AppSettings> {
    error("Tried to use LocalAppSettings without an AppSettingsProvider")
}

@Composable
fun AppSettingsProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppSettings provides AppSettings(
            SettingsContext(
                prefs = LocalContext.current.getSharedPreferences(
                    "flickboard",
                    Context.MODE_PRIVATE
                ),
                coroutineScope = LocalLifecycleOwner.current.lifecycleScope
            )
        )
    ) {
        content()
    }
}

class AppSettings(ctx: SettingsContext) {
    val showLetters = Setting.Bool(
        key = "showLetters",
        label = "Show letters",
        defaultValue = true,
        ctx = ctx
    )

    val showSymbols = Setting.Bool(
        key = "showSymbols",
        label = "Show symbols",
        defaultValue = true,
        ctx = ctx
    )

    val showNumbers = Setting.Bool(
        key = "showNumbers",
        label = "Show numbers",
        defaultValue = true,
        ctx = ctx
    )

    val enableFastActions = Setting.Bool(
        key = "enableFastActions",
        label = "Enable fast actions",
        defaultValue = true,
        ctx = ctx
    )

    val germanLayout = Setting.Bool(
        key = "germanLayout",
        label = "German layout",
        defaultValue = false,
        ctx = ctx
    )

    val cellHeight = Setting.FloatSlider(
        key = "cellHeight",
        label = "Cell height",
        defaultValue = 72F,
        range = 48F..96F,
        ctx = ctx
    )

    val all =
        listOf<Setting<*>>(
            showLetters,
            showSymbols,
            showNumbers,
            enableFastActions,
            germanLayout,
            cellHeight
        )

    val layout: Layout
        @Composable
        get() = if (germanLayout.state.value) {
            DE_MESSAGEASE
        } else {
            SV_MESSAGEASE
        }
}

class SettingsContext(val prefs: SharedPreferences, val coroutineScope: CoroutineScope)

sealed class Setting<T : Any>(private val ctx: SettingsContext) {
    abstract val key: String
    abstract val label: String

    abstract var currentValue: T

    private var lastCachedValue: T? = null
    private val cachedValue: T
        get() {
            var v = lastCachedValue
            if (v == null) {
                v = currentValue
                lastCachedValue = v
            }
            return v
        }

    val watch: Flow<T> = callbackFlow {
        println("starting flow: $key")
        val listener = { _: SharedPreferences, key: String? ->
            if (key == this@Setting.key) {
                val v = currentValue
                lastCachedValue = v
                trySendBlocking(v)
            }
        }
        send(cachedValue)
        ctx.prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { ctx.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
        .conflate()
        .shareIn(ctx.coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)

    val state: State<T>
        @Composable
        get() = watch.collectAsState(initial = cachedValue)

    class Bool(
        override val key: String,
        override val label: String,
        val defaultValue: Boolean,
        private val ctx: SettingsContext,
    ) : Setting<Boolean>(ctx) {
        override var currentValue: Boolean
            get() = ctx.prefs.getBoolean(key, defaultValue)
            set(value) = ctx.prefs.edit { putBoolean(key, value) }
    }

    class FloatSlider(
        override val key: String,
        override val label: String,
        val defaultValue: Float,
        val range: ClosedFloatingPointRange<Float>,
        private val ctx: SettingsContext
    ) : Setting<Float>(ctx) {
        override var currentValue: Float
            get() = ctx.prefs.getFloat(key, defaultValue)
            set(value) = ctx.prefs.edit { putFloat(key, value) }

    }
}
