package se.nullable.flickboard.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
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
    Column(modifier.verticalScroll(rememberScrollState())) {
        appSettings.all.forEach { setting ->
            when (setting) {
                is Setting.Section -> SettingsSection(setting)
                is Setting.Bool -> BoolSetting(setting)
                is Setting.FloatSlider -> FloatSliderSetting(setting)
            }
        }
    }
}

@Composable
fun SettingsSection(setting: Setting.Section) {
    SettingRow {
        Text(setting.label, color = MaterialTheme.colorScheme.primary)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingLabel(setting)
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
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun RowScope.SettingLabel(setting: Setting<*>) {
    Column(
        Modifier
            .weight(1f, false)
            .padding(end = 8.dp)
    ) {
        Text(text = setting.label)
        val description = setting.description
        if (description != null) {
            Text(
                text = description,
                softWrap = true,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7F),
                fontWeight = FontWeight.Light
            )
        }
    }
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
        description = "Allows certain actions to be performed before the tap is released",
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

    val swipeThreshold = Setting.FloatSlider(
        key = "swipeThreshold",
        label = "Swipe threshold",
        description = "How far you need to drag before a tap becomes a swipe",
        defaultValue = 8F,
        range = 8F..24F,
        ctx = ctx
    )

    val fastSwipeThreshold = Setting.FloatSlider(
        key = "fastSwipeTreshold",
        label = "Fast swipe threshold",
        description = "How far you need to drag between each fast action tick",
        defaultValue = 16F,
        range = 8F..24F,
        ctx = ctx,
    )

    val circleThreshold = Setting.FloatSlider(
        key = "circleThreshold",
        label = "Circle threshold",
        description = "How round a shape must be to be recognized as a circle",
        defaultValue = 50F,
        range = 1F..100F,
        ctx = ctx,
    )

    val all =
        listOf<Setting<*>>(
            germanLayout,
            Setting.Section("Aesthetics", ctx),
            showLetters,
            showSymbols,
            showNumbers,
            Setting.Section("Behaviour", ctx),
            enableFastActions,
            cellHeight,
            swipeThreshold,
            fastSwipeThreshold,
            circleThreshold,
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
    abstract val description: String?

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

    class Section(override val label: String, ctx: SettingsContext) : Setting<Unit>(ctx) {
        override val key: String = "section-dummy-key"
        override val description: String? = null
        override var currentValue: Unit
            get() {}
            set(_) {}

    }

    class Bool(
        override val key: String,
        override val label: String,
        val defaultValue: Boolean,
        private val ctx: SettingsContext,
        override val description: String? = null,
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
        private val ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Float>(ctx) {
        override var currentValue: Float
            get() = ctx.prefs.getFloat(key, defaultValue)
            set(value) = ctx.prefs.edit { putFloat(key, value) }

    }
}
