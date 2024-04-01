package se.nullable.flickboard.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import se.nullable.flickboard.PiF
import se.nullable.flickboard.R
import se.nullable.flickboard.model.ActionClass
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.layouts.DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_MESSAGEASE
import se.nullable.flickboard.model.layouts.ES_MESSAGEASE
import se.nullable.flickboard.model.layouts.FR_EXT_MESSAGEASE
import se.nullable.flickboard.model.layouts.FR_MESSAGEASE
import se.nullable.flickboard.model.layouts.MESSAGEASE_NUMERIC_CALCULATOR_LAYER
import se.nullable.flickboard.model.layouts.MESSAGEASE_NUMERIC_PHONE_LAYER
import se.nullable.flickboard.model.layouts.RU_MESSAGEASE
import se.nullable.flickboard.model.layouts.RU_PHONETIC_MESSAGEASE
import se.nullable.flickboard.model.layouts.SV_DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.SV_MESSAGEASE
import se.nullable.flickboard.model.layouts.UK_MESSAGEASE
import se.nullable.flickboard.util.Boxed
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SettingsHomePage(
    onNavigateToSection: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val appSettings = LocalAppSettings.current
    Column {
        LazyColumn(modifier.weight(1F)) {
            item {
                OnboardingPrompt()
            }
            items(appSettings.all, key = { it.key }) { section ->
                Box(Modifier.clickable { onNavigateToSection(section) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(painterResource(section.icon), null)
                        Text(
                            section.label,
                            Modifier
                                .weight(1F)
                                .padding(horizontal = 8.dp)
                        )
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                }
            }
        }
        SettingsKeyboardPreview()
    }
}

@Composable
fun SettingsSectionPage(section: SettingsSection, modifier: Modifier = Modifier) {
    Column {
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .weight(1F)
                .safeGesturesPadding()
        ) {
            section.settings.forEach { setting ->
                when (setting) {
                    is Setting.Bool -> BoolSetting(setting)
                    is Setting.Integer -> {} // Not rendered right now, implement if used anywhere
                    is Setting.FloatSlider -> FloatSliderSetting(setting)
                    is Setting.EnumList<*> -> EnumListSetting(setting)
                    is Setting.Enum -> EnumSetting(setting)
                    is Setting.Image -> ImageSetting(setting)
                    is Setting.Colour -> ColourSetting(setting)
                }
            }
        }
        SettingsKeyboardPreview()
    }
}

@Composable
fun SettingsKeyboardPreview() {
    Box {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
            Column {
                Text(
                    text = "Preview keyboard",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(8.dp)
                )
                ProvideDisplayLimits {
                    ConfiguredKeyboard(
                        onAction = { }, // Keyboard provides internal visual feedback if enabled
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                Text(text = setting.render(state.value))
            }
            Row {
                Slider(
                    value = state.value,
                    onValueChange = { setting.currentValue = it },
                    valueRange = setting.range,
                    modifier = Modifier.weight(1F)
                )
                IconButton(onClick = { setting.resetToDefault() }) {
                    Icon(painterResource(R.drawable.baseline_clear_24), "Reset to default")
                }
            }
        }
    }
}

@Composable
fun <T : Labeled> EnumListSetting(setting: Setting.EnumList<T>) {
    fun toggleOption(option: T, add: Boolean? = null) {
        val old = setting.currentValue
        setting.currentValue = when {
            add ?: !old.contains(option) -> old + option
            else -> old - option
        }
    }
    BaseEnumSetting(
        setting,
        valueLabel = { it.singleOrNull()?.label ?: "${it.size} enabled" },
        options = setting.options,
        optionSelectionControl = { selected, option ->
            Switch(
                checked = selected.contains(option),
                onCheckedChange = { toggleOption(option, add = it) }
            )
        },
        optionIsSelected = List<T>::contains,
        onOptionSelected = { toggleOption(it) },
        collapseOnOptionSelected = false,
        writePreviewSettings = { prefs, option ->
            setting.writePreviewSettings(prefs)
            setting.writeTo(prefs, listOf(option))
        },
    )
}

@Composable
fun <T : Labeled> EnumSetting(setting: Setting.Enum<T>) {
    BaseEnumSetting(
        setting,
        valueLabel = { it.label },
        options = setting.options,
        optionSelectionControl = { selected, option ->
            RadioButton(selected = selected == option, onClick = { setting.currentValue = option })
        },
        optionIsSelected = { selected, option -> option == selected },
        onOptionSelected = { setting.currentValue = it },
        collapseOnOptionSelected = true,
        writePreviewSettings = { prefs, option ->
            setting.writePreviewSettings(prefs)
            setting.writeTo(prefs, option)
        },
    )
}

@Composable
fun ImageSetting(setting: Setting.Image) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        var uri = it
        if (uri != null) {
            val cachedFile = context.filesDir.resolve("background-image")
            // Copy image to ensure that that it'll stay present,
            // and that we will keep permission to access it.
            // takePersistableUriPermission is not always granted
            // by all implementations.
            context.contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input?.copyTo(output)
                }
            }
            uri = Uri.fromFile(cachedFile).buildUpon()
                // Bust caches and make sure clients load it
                .fragment(Random.Default.nextLong().toString())
                .build()
        }
        setting.currentValue = uri
    }
    SettingRow {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingLabel(setting)
            Row {
                IconButton(onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Icon(painterResource(R.drawable.baseline_image_search_24), "Set image")
                }
                IconButton(onClick = { setting.currentValue = null }) {
                    Icon(painterResource(R.drawable.baseline_clear_24), "Clear")
                }
            }
        }
    }
}

@Composable
fun ColourSetting(setting: Setting.Colour) {
    val state = setting.state
    val expanded = remember { mutableStateOf(false) }
    SettingRow {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingLabel(setting)
                Row {
                    state.value?.let {
                        // Not actually a button, but we want to share the same sizing...
                        IconButton(onClick = {}, enabled = false) {
                            Image(
                                ColorPainter(it), "Selected colour",
                                Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                    IconButton(onClick = {
                        expanded.value = !expanded.value
                    }) {
                        Icon(painterResource(R.drawable.baseline_color_lens_24), "Set colour")
                    }
                    IconButton(onClick = { setting.currentValue = null }) {
                        Icon(painterResource(R.drawable.baseline_clear_24), "Clear")
                    }
                }
            }
            AnimatedVisibility(expanded.value, Modifier.align(Alignment.CenterHorizontally)) {
                ColourPicker(
                    onColourSelected = { setting.currentValue = it },
                    Modifier
                        .sizeIn(maxHeight = 200.dp)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Labeled, V : Any> BaseEnumSetting(
    setting: Setting<V>,
    valueLabel: (V) -> String,
    options: List<T>,
    optionSelectionControl: @Composable (V, T) -> Unit,
    optionIsSelected: (V, T) -> Boolean,
    onOptionSelected: (T) -> Unit,
    collapseOnOptionSelected: Boolean,
    writePreviewSettings: (SharedPreferences, T) -> Unit,
) {
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
                Text(valueLabel(setting.state.value))
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
                    items(options, key = { it.toString() }) { option ->
                        val appSettings = LocalAppSettings.current
                        val isSelected = optionIsSelected(setting.state.value, option)
                        val select = {
                            onOptionSelected(option)
                            if (collapseOnOptionSelected) {
                                collapse()
                            }
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
                                Row(
                                    Modifier.padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.label,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1F)
                                    )
                                    optionSelectionControl(setting.state.value, option)
                                }
                                val prefs = remember(appSettings, setting, option) {
                                    MockedSharedPreferences(appSettings.ctx.prefs)
                                        .also { writePreviewSettings(it, option) }
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
fun SettingsHomePreview() {
    FlickBoardParent {
        Surface {
            SettingsHomePage(
                onNavigateToSection = {},
//                modifier = Modifier.width(1000.dp)
            )
        }
    }
}

@Composable
@Preview
fun SettingsSectionPagePreview() {
    FlickBoardParent {
        Surface {
            SettingsSectionPage(LocalAppSettings.current.all[0], Modifier.width(1000.dp))
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
    val letterLayers = Setting.EnumList(
        // Renaming this would reset the people's selected layer..
        key = "layout",
        label = "Letter layouts",
        defaultValue = listOf(LetterLayerOption.English),
        options = LetterLayerOption.entries,
        fromString = LetterLayerOption::valueOf,
        ctx = ctx,
        writePreviewSettings = { prefs ->
            if (enabledLayers.readFrom(prefs) == EnabledLayers.Numbers) {
                enabledLayers.writeTo(prefs, EnabledLayers.Letters)
            }
        }
    )

    // Intentionally not surfaced as a settings option
    val activeLetterLayerIndex = Setting.Integer(
        key = "activeLetterLayerIndex",
        label = "Active letter layer index",
        defaultValue = 0,
        ctx = ctx
    )

    val secondaryLetterLayer = Setting.Enum(
        key = "secondaryLetterLayer",
        label = "Secondary letter layout",
        description = "Only applies when using double letter layers",
        defaultValue = LetterLayerOption.English,
        options = LetterLayerOption.entries,
        fromString = LetterLayerOption::valueOf,
        ctx = ctx,
        writePreviewSettings = { prefs ->
            enabledLayers.writeTo(prefs, EnabledLayers.DoubleLetters)
        }
    )

    val numericLayer = Setting.Enum(
        key = "numericLayer",
        label = "Number layout",
        defaultValue = NumericLayerOption.Phone,
        options = NumericLayerOption.entries,
        fromString = NumericLayerOption::valueOf,
        ctx = ctx,
        writePreviewSettings = { prefs ->
            if (enabledLayers.readFrom(prefs) == EnabledLayers.Letters) {
                enabledLayers.writeTo(prefs, EnabledLayers.Numbers)
            }
        }
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
        range = -1F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val landscapeScale = Setting.FloatSlider(
        key = "landscapeScale",
        label = "Landscape scale",
        defaultValue = 1F,
        range = 0.2F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val portraitLocation = Setting.FloatSlider(
        key = "portraitLocation",
        label = "Portrait location",
        defaultValue = 0F,
        range = -1F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val portraitScale = Setting.FloatSlider(
        key = "portraitScale",
        label = "Portrait scale",
        defaultValue = 1F,
        range = 0.2F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val currentLocation: Float
        @Composable get() = when {
            LocalDisplayLimits.current?.isLandscape ?: false -> landscapeLocation.state.value
            else -> portraitLocation.state.value
        }

    val currentScale: Float
        @Composable get() = when {
            LocalDisplayLimits.current?.isLandscape ?: false -> landscapeScale.state.value
            else -> portraitScale.state.value
        }

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

    val shownActionClasses: State<Set<ActionClass>>
        @Composable get() {
            val showLetters = showLetters.state
            val showSymbols = showSymbols.state
            val showNumbers = showNumbers.state
            return remember {
                derivedStateOf {
                    setOfNotNull(
                        ActionClass.Other,
                        ActionClass.Letter.takeIf { showLetters.value },
                        ActionClass.Symbol.takeIf { showSymbols.value },
                        ActionClass.Number.takeIf { showNumbers.value },
                    )
                }
            }
        }

    val enableHiddenActions = Setting.Bool(
        key = "enableHiddenActions",
        label = "Enable hidden actions",
        defaultValue = true,
        ctx = ctx
    )

    val keyRoundness = Setting.FloatSlider(
        key = "keyRoundness",
        label = "Key roundness",
        defaultValue = 0F,
        range = 0F..0.5F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val actionVisualBiasCenter = Setting.FloatSlider(
        key = "actionVisualBiasCenter",
        label = "Center key label scale",
        defaultValue = 1.5F,
        range = 1F..2F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val actionVisualScale = Setting.FloatSlider(
        key = "actionVisualScale",
        label = "Key label scale",
        defaultValue = 1F,
        range = 0.5F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val keyColour = Setting.Colour(
        key = "keyColour",
        label = "Key colour",
        ctx = ctx
    )

    val keyOpacity = Setting.FloatSlider(
        key = "keyOpacity",
        label = "Key opacity",
        defaultValue = 0.7F,
        range = 0F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val backgroundOpacity = Setting.FloatSlider(
        key = "backgroundOpacity",
        label = "Background opacity",
        defaultValue = 1F,
        range = 0F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val backgroundImage = Setting.Image(
        key = "backgroundImage",
        label = "Background image",
        ctx = ctx
    )

    val enablePointerTrail = Setting.Bool(
        key = "enablePointerTrail",
        label = "Enable pointer trail",
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

    val periodOnDoubleSpace = Setting.Bool(
        key = "periodOnDoubleSpace",
        label = "Period on double space",
        description = "Convert \"  \" into \". \"",
        defaultValue = false,
        ctx = ctx
    )

    val longHoldOnClockwiseCircle = Setting.Bool(
        key = "digitOnClockwiseCircle",
        label = "Type digit on clockwise circle",
        defaultValue = false,
        ctx = ctx
    )

    val keyHeight = Setting.FloatSlider(
        key = "keyHeight",
        label = "Key height",
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

    val circleJaggednessThreshold = Setting.FloatSlider(
        key = "circleJaggednessThreshold",
        label = "Circle jaggedness threshold",
        description = "How smooth a circle's radius must be",
        defaultValue = .5F,
        range = .01F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val circleDiscontinuityThreshold = Setting.FloatSlider(
        key = "circleDiscontinuityThreshold",
        label = "Circle discontinuity threshold",
        description = "How much a circle's angle is allowed to jump",
        defaultValue = .3F * PiF,
        range = 0F..PiF,
        ctx = ctx,
        render = Setting.FloatSlider::angle
    )

    val circleAngleThreshold = Setting.FloatSlider(
        key = "circleAngleThreshold",
        label = "Circle angle threshold",
        description = "How full the circle must be",
        defaultValue = 1.8F * PiF,
        range = 1.5F * PiF..3F * PiF,
        ctx = ctx,
        render = Setting.FloatSlider::angle
    )

    val enableHapticFeedback = Setting.Bool(
        key = "enableHapticFeedback",
        label = "Vibrate on key input",
        defaultValue = true,
        ctx = ctx
    )

    val enableVisualFeedback = Setting.Bool(
        key = "enableVisualFeedback",
        label = "Highlight taken actions",
        defaultValue = true,
        ctx = ctx
    )

    val all =
        listOf<SettingsSection>(
            SettingsSection(
                key = "layout", label = "Layout", icon = R.drawable.baseline_apps_24,
                settings = listOf(
                    letterLayers,
                    secondaryLetterLayer,
                    numericLayer,
                    enabledLayers,
                    handedness,
                    landscapeLocation,
                    landscapeScale,
                    portraitLocation,
                    portraitScale,
                )
            ),
            SettingsSection(
                key = "aesthetics", label = "Aesthetics", icon = R.drawable.baseline_palette_24,
                settings = listOf(
                    showLetters,
                    showSymbols,
                    showNumbers,
                    enableHiddenActions,
                    keyRoundness,
                    actionVisualBiasCenter,
                    actionVisualScale,
                    keyColour,
                    keyOpacity,
                    backgroundOpacity,
                    backgroundImage,
                    enablePointerTrail,
                )
            ),
            SettingsSection(
                key = "behaviour",
                label = "Behaviour",
                icon = R.drawable.baseline_app_settings_alt_24,
                settings = listOf(
                    enableFastActions,
                    periodOnDoubleSpace,
                    longHoldOnClockwiseCircle,
                    keyHeight,
                    swipeThreshold,
                    fastSwipeThreshold,
                    circleJaggednessThreshold,
                    circleDiscontinuityThreshold,
                    circleAngleThreshold,
                )
            ),
            SettingsSection(
                key = "feedback", label = "Feedback", icon = R.drawable.baseline_vibration_24,
                settings = listOf(
                    enableHapticFeedback,
                    enableVisualFeedback,
                ),
            )
        )
}

data class SettingsSection(
    val key: String,
    val label: String,
    val icon: Int,
    val settings: List<Setting<*>>
)

interface Labeled {
    val label: String
}

enum class EnabledLayers(override val label: String) : Labeled {
    All("All"),
    Letters("Letters only"),
    Numbers("Numbers only"),
    DoubleLetters("Double letters"),
    AllMiniNumbers("All (mini numbers)"),
    AllMiniNumbersMiddle("All (mini numbers in middle)"),
}

enum class Handedness(override val label: String) : Labeled {
    LeftHanded("Left-handed"),
    RightHanded("Right-handed");

    operator fun not(): Handedness = when (this) {
        LeftHanded -> RightHanded
        RightHanded -> LeftHanded
    }
}

enum class LetterLayerOption(override val label: String, val layout: Layout) : Labeled {
    English("English (MessagEase)", EN_MESSAGEASE),
    German("German (MessagEase)", DE_MESSAGEASE),
    GermanEnglish("German/English (MessagEase)", EN_DE_MESSAGEASE),
    Russian("Russian (MessagEase)", RU_MESSAGEASE),
    RussianPhonetic("Russian phonetic (MessagEase)", RU_PHONETIC_MESSAGEASE),
    Spanish("Spanish (MessagEase)", ES_MESSAGEASE),
    Swedish("Swedish (MessagEase)", SV_MESSAGEASE),
    SwedishDE("Swedish (MessagEase, German-style)", SV_DE_MESSAGEASE),
    Ukrainian("Ukrainian (MessagEase)", UK_MESSAGEASE),
    French("French (MessagEase)", FR_MESSAGEASE),
    FrenchExt("French (Extended MessagEase)", FR_EXT_MESSAGEASE),
}

enum class NumericLayerOption(override val label: String, val layer: Layer) : Labeled {
    Phone("Phone", MESSAGEASE_NUMERIC_PHONE_LAYER),
    Calculator("Calculator", MESSAGEASE_NUMERIC_CALCULATOR_LAYER),
}

class SettingsContext(val prefs: SharedPreferences, val coroutineScope: CoroutineScope)

sealed class Setting<T>(private val ctx: SettingsContext) {
    abstract val key: String
    abstract val label: String
    abstract val description: String?

    var currentValue: T
        get() = readFrom(ctx.prefs)
        set(value) = writeTo(ctx.prefs, value)

    fun resetToDefault() {
        resetIn(ctx.prefs)
    }

    abstract fun readFrom(prefs: SharedPreferences): T
    abstract fun writeTo(prefs: SharedPreferences, value: T)
    fun resetIn(prefs: SharedPreferences) {
        prefs.edit { remove(key) }
    }

    private var lastCachedValue: Boxed<T>? = null
    private val cachedValue: T
        get() {
            var v = lastCachedValue
            if (v == null) {
                v = Boxed(currentValue)
                lastCachedValue = v
            }
            return v.value
        }

    val watch: Flow<T> = callbackFlow {
//        println("starting flow: $key")
        // Type MUST Be initialized by name to ensure that the same object is passed to
        // register and unregister. Otherwise no strong reference is held to the listener,
        // meaning that the registration can be "lost" on any garbage collection.
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == this@Setting.key) {
                val v = currentValue
                lastCachedValue = Boxed(v)
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
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Boolean>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Boolean =
            prefs.getBoolean(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences, value: Boolean) =
            prefs.edit { putBoolean(key, value) }
    }

    class Integer(
        override val key: String,
        override val label: String,
        val defaultValue: Int,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Int>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Int =
            prefs.getInt(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences, value: Int) =
            prefs.edit { putInt(key, value) }
    }

    class FloatSlider(
        override val key: String,
        override val label: String,
        val defaultValue: Float,
        val range: ClosedFloatingPointRange<Float>,
        ctx: SettingsContext,
        override val description: String? = null,
        val render: (Float) -> String = { it.roundToInt().toString() },
    ) : Setting<Float>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Float =
            prefs.getFloat(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences, value: Float) =
            prefs.edit { putFloat(key, value.coerceIn(range)) }

        companion object {
            fun percentage(x: Float): String = "${(x * 100).roundToInt()}%"
            fun angle(x: Float): String = "${Math.toDegrees(x.toDouble()).roundToInt()}°"
        }
    }

    class EnumList<T : Labeled>(
        override val key: String,
        override val label: String,
        val defaultValue: List<T>,
        val options: List<T>,
        val fromString: (String) -> T?,
        ctx: SettingsContext,
        override val description: String? = null,
        val writePreviewSettings: (SharedPreferences) -> Unit = {},
    ) : Setting<List<T>>(ctx) {
        override fun readFrom(prefs: SharedPreferences): List<T> =
            prefs.getString(key, null)
                ?.split(',')
                ?.mapNotNull { it.takeIf { it.isNotEmpty() }?.let(fromString) }
                ?: defaultValue

        override fun writeTo(prefs: SharedPreferences, value: List<T>) =
            prefs.edit { putString(key, value.joinToString(",") { it.toString() }) }
    }

    class Enum<T : Labeled>(
        override val key: String,
        override val label: String,
        val defaultValue: T,
        val options: List<T>,
        val fromString: (String) -> T?,
        ctx: SettingsContext,
        override val description: String? = null,
        val writePreviewSettings: (SharedPreferences) -> Unit = {},
    ) : Setting<T>(ctx) {
        override fun readFrom(prefs: SharedPreferences): T =
            prefs.getString(key, null)?.let(fromString) ?: defaultValue

        override fun writeTo(prefs: SharedPreferences, value: T) =
            prefs.edit { putString(key, value.toString()) }
    }

    class Image(
        override val key: String,
        override val label: String,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Uri?>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Uri? =
            prefs.getString(key, null)?.let(Uri::parse)

        override fun writeTo(prefs: SharedPreferences, value: Uri?) =
            prefs.edit { putString(key, value?.toString()) }
    }

    class Colour(
        override val key: String,
        override val label: String,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Color?>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Color? =
            prefs.getInt(key, 0).takeUnless { it == 0 }?.let { Color(it) }

        override fun writeTo(prefs: SharedPreferences, value: Color?) =
            prefs.edit { putInt(key, value?.toArgb() ?: 0) }
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
        strings[key] ?: inner.getString(key, defValue)

    override fun getFloat(key: String, defValue: Float): Float =
        strings[key]?.toFloat() ?: inner.getFloat(key, defValue)

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

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                strings[key] = value.toString()
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
