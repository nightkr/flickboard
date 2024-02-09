package se.nullable.flickboard.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import kotlinx.coroutines.launch
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.layouts.DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_MESSAGEASE
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
                is Setting.Enum -> EnumSetting(setting)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Labeled> EnumSetting(setting: Setting.Enum<T>) {
    val state = setting.state
    val sheetState = rememberModalBottomSheetState()
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun collapse() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                expanded = false
            }
        }
    }
    Box(Modifier.clickable { expanded = true }) {
        SettingRow {
            SettingLabel(setting)
            Row {
                Text(state.value.label)
                val angle: Float by animateFloatAsState(
                    when {
                        expanded -> 180F
                        else -> 0F
                    },
                    label = "angle"
                )
                Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.rotate(angle))
            }
        }
        if (expanded) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { expanded = false },
            ) {
                LazyColumn {
                    items(setting.options, key = { it.toString() }) { option ->
                        val appSettings = LocalAppSettings.current
                        val isSelected = setting.state.value == option
                        val select = {
                            setting.currentValue = option
                            collapse()
                        }
                        Card(
                            onClick = select,
                            colors = when {
                                isSelected -> CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                )

                                else -> CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                )
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = option.label,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val prefs = remember(appSettings, setting, option) {
                                    MockedSharedPreferences(appSettings.ctx.prefs).also {
                                        setting.writeTo(
                                            it,
                                            option
                                        )
                                    }
                                }
                                AppSettingsProvider(prefs) {
                                    ConfiguredKeyboard(onAction = null)
                                }
                            }
                        }
                    }
                }
            }
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
fun AppSettingsProvider(prefs: SharedPreferences? = null, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppSettings provides AppSettings(
            SettingsContext(
                prefs = prefs ?: LocalContext.current.getSharedPreferences(
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

class AppSettings(val ctx: SettingsContext) {
    val layout = Setting.Enum(
        key = "layout",
        label = "Layout",
        defaultValue = LayoutOption.English,
        options = LayoutOption.entries,
        fromString = LayoutOption::valueOf,
        ctx = ctx
    )

    val enabledLayers = Setting.Enum(
        key = "enabledLayers",
        label = "Enabled layers",
        defaultValue = EnabledLayers.All,
        options = EnabledLayers.entries,
        fromString = EnabledLayers::valueOf,
        ctx = ctx
    )

    val handedness = Setting.Enum(
        key = "handedness",
        label = "Handedness",
        defaultValue = Handedness.RightHanded,
        options = Handedness.entries,
        fromString = Handedness::valueOf,
        ctx = ctx
    )

    val landscapeLocation = Setting.FloatSlider(
        key = "landscapeLocation",
        label = "Landscape location",
        defaultValue = 0F,
        range = -100F..100F,
        ctx = ctx
    )

    val landscapeScale = Setting.FloatSlider(
        key = "landscapeScale",
        label = "Landscape scale",
        defaultValue = 100F,
        range = 20F..100F,
        ctx = ctx,
    )

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

    val keyRoundness = Setting.FloatSlider(
        key = "keyRoundness",
        label = "Key roundness",
        defaultValue = 0F,
        range = 0f..50f,
        ctx = ctx
    )

    val enablePointerTrail = Setting.Bool(
        key = "enablePointerTrail",
        label = "Enable pointer trail",
        defaultValue = false,
        ctx = ctx
    )

    val enableFastActions = Setting.Bool(
        key = "enableFastActions",
        label = "Enable fast actions",
        description = "Allows certain actions to be performed before the tap is released",
        defaultValue = true,
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
            Setting.Section("Layout", ctx),
            layout,
            enabledLayers,
            handedness,
            landscapeLocation,
            landscapeScale,
            Setting.Section("Aesthetics", ctx),
            showLetters,
            showSymbols,
            showNumbers,
            keyRoundness,
            enablePointerTrail,
            Setting.Section("Behaviour", ctx),
            enableFastActions,
            cellHeight,
            swipeThreshold,
            fastSwipeThreshold,
            circleThreshold,
        )
}

interface Labeled {
    val label: String
}

enum class EnabledLayers(override val label: String) : Labeled {
    All("All"),
    Letters("Letters only"),
    Numbers("Numbers only"),
}

enum class Handedness(override val label: String) : Labeled {
    LeftHanded("Left-handed"),
    RightHanded("Right-handed");

    operator fun not(): Handedness = when (this) {
        LeftHanded -> RightHanded
        RightHanded -> LeftHanded
    }
}

enum class LayoutOption(override val label: String, val layout: Layout) : Labeled {
    English("English (MessagEase)", EN_MESSAGEASE),
    Swedish("Swedish (MessagEase)", SV_MESSAGEASE),
    German("German (MessagEase)", DE_MESSAGEASE);
}

class SettingsContext(val prefs: SharedPreferences, val coroutineScope: CoroutineScope)

sealed class Setting<T : Any>(private val ctx: SettingsContext) {
    abstract val key: String
    abstract val label: String
    abstract val description: String?

    var currentValue: T
        get() = readFrom(ctx.prefs)
        set(value) = writeTo(ctx.prefs, value)

    abstract fun readFrom(prefs: SharedPreferences): T
    abstract fun writeTo(prefs: SharedPreferences, value: T)

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
//        println("starting flow: $key")
        // Type MUST Be initialized by name to ensure that the same object is passed to
        // register and unregister. Otherwise no strong reference is held to the listener,
        // meaning that the registration can be "lost" on any garbage collection.
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
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

        override fun readFrom(prefs: SharedPreferences) {}
        override fun writeTo(prefs: SharedPreferences, value: Unit) {}
    }

    class Bool(
        override val key: String,
        override val label: String,
        val defaultValue: Boolean,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Boolean>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Boolean =
            prefs.getBoolean(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences, value: Boolean) =
            prefs.edit { putBoolean(key, value) }
    }

    class FloatSlider(
        override val key: String,
        override val label: String,
        val defaultValue: Float,
        val range: ClosedFloatingPointRange<Float>,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Float>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Float =
            prefs.getFloat(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences, value: Float) =
            prefs.edit { putFloat(key, value.coerceIn(range)) }
    }

    class Enum<T : Labeled>(
        override val key: String,
        override val label: String,
        val defaultValue: T,
        val options: List<T>,
        val fromString: (String) -> T?,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<T>(ctx) {
        override fun readFrom(prefs: SharedPreferences): T =
            prefs.getString(key, null)?.let(fromString) ?: defaultValue

        override fun writeTo(prefs: SharedPreferences, value: T) =
            prefs.edit { putString(key, value.toString()) }
    }
}

/**
 * Limited in-memory variant of SharedPreferences, used to generated previews.
 *
 * Does not support watchers, and changes are not transactional.
 */
class MockedSharedPreferences(val inner: SharedPreferences) : SharedPreferences by inner {
    private val strings = mutableMapOf<String, String>()

    override fun getString(key: String, defValue: String?): String? =
        strings.getOrElse(key) { inner.getString(key, defValue) }

    override fun edit(): SharedPreferences.Editor {
        return object : SharedPreferences.Editor {
            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                if (value != null) {
                    strings[key] = value
                } else {
                    strings.remove(key)
                }
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun remove(key: String): SharedPreferences.Editor {
                strings.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                TODO("Not yet implemented")
            }

            override fun commit(): Boolean {
                return true
            }

            override fun apply() {
            }
        }
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }
}