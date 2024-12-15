package se.nullable.flickboard.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import se.nullable.flickboard.BuildConfig
import se.nullable.flickboard.MainActivitySharedElement
import se.nullable.flickboard.PiF
import se.nullable.flickboard.R
import se.nullable.flickboard.model.ActionClass
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.layouts.AR_MESSAGEASE
import se.nullable.flickboard.model.layouts.DA_MESSAGEASE
import se.nullable.flickboard.model.layouts.DE_EO_MESSAGEASE
import se.nullable.flickboard.model.layouts.DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_MESSAGEASE
import se.nullable.flickboard.model.layouts.EN_THUMBKEY
import se.nullable.flickboard.model.layouts.ES_MESSAGEASE
import se.nullable.flickboard.model.layouts.FA_MESSAGEASE
import se.nullable.flickboard.model.layouts.FA_THUMBKEY
import se.nullable.flickboard.model.layouts.FR_EXT_MESSAGEASE
import se.nullable.flickboard.model.layouts.FR_MESSAGEASE
import se.nullable.flickboard.model.layouts.FR_PUNC_MESSAGEASE
import se.nullable.flickboard.model.layouts.HEB_MESSAGEASE
import se.nullable.flickboard.model.layouts.HU_DT_MESSAGEASE
import se.nullable.flickboard.model.layouts.HU_MESSAGEASE
import se.nullable.flickboard.model.layouts.HU_MF_MESSAGEASE
import se.nullable.flickboard.model.layouts.HU_UUP_MESSAGEASE
import se.nullable.flickboard.model.layouts.IT_MESSAGEASE
import se.nullable.flickboard.model.layouts.NO_THUMBKEY
import se.nullable.flickboard.model.layouts.PL_RMITURA_MESSAGEASE
import se.nullable.flickboard.model.layouts.PT_IOS_MESSAGEASE
import se.nullable.flickboard.model.layouts.PT_MESSAGEASE
import se.nullable.flickboard.model.layouts.RU_MESSAGEASE
import se.nullable.flickboard.model.layouts.RU_PHONETIC_MESSAGEASE
import se.nullable.flickboard.model.layouts.SV_DE_MESSAGEASE
import se.nullable.flickboard.model.layouts.SV_MESSAGEASE
import se.nullable.flickboard.model.layouts.TR_MESSAGEASE
import se.nullable.flickboard.model.layouts.UK_MESSAGEASE
import se.nullable.flickboard.model.layouts.UK_RU_MESSAGEASE
import se.nullable.flickboard.model.layouts.messageaseNumericCalculatorLayer
import se.nullable.flickboard.model.layouts.messageaseNumericPhoneLayer
import se.nullable.flickboard.model.layouts.miniNumbersCalculatorLayer
import se.nullable.flickboard.model.layouts.miniNumbersPhoneLayer
import se.nullable.flickboard.ui.theme.Typography
import se.nullable.flickboard.ui.util.isSamsungDevice
import se.nullable.flickboard.util.Boxed
import se.nullable.flickboard.util.MaterialToneMode
import se.nullable.flickboard.util.orNull
import tryEnumValueOf
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsHomePage(
    onNavigateToSection: (SettingsSection) -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToBetaMenu: () -> Unit,
    onNavigateToDescriber: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val appSettings = LocalAppSettings.current
    val tryText = remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    fun openUri(uri: Uri) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
    Column {
        LazyColumn(modifier.weight(1F)) {
            item {
                OnboardingPrompt()
            }
            item {
                MenuPageLink(
                    onClick = onNavigateToTutorial,
                    icon = painterResource(R.drawable.baseline_checklist_24),
                    label = "Tutorial"
                )
            }
            item {
                MenuPageLink(
                    onClick = onNavigateToDescriber,
                    icon = painterResource(R.drawable.baseline_help_24),
                    label = "What Does This Do?"
                )
            }
            item {
                SettingsTitle("Settings")
            }
            items(appSettings.all, key = { it.key }) { section ->
                MenuPageLink(
                    onClick = { onNavigateToSection(section) },
                    icon = painterResource(section.icon),
                    label = section.label
                )
            }
            item {
                TextField(
                    value = tryText.value,
                    onValueChange = { tryText.value = it },
                    label = { Text("Type here to try FlickBoard") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            item {
                SettingsTitle("About")
                val variant = when {
                    BuildConfig.BUILD_TYPE == "release" && (BuildConfig.FLAVOR == "plain" || BuildConfig.FLAVOR == "screengrab") -> ""
                    else -> " (${BuildConfig.BUILD_TYPE})"
                }
                val hiddenBetaTaps = remember { mutableIntStateOf(0) }
                val betaMenuHintToast = remember { Toast.makeText(context, "", Toast.LENGTH_SHORT) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val tapsToOpenBetaMenu = 7
                            when (val taps = ++hiddenBetaTaps.intValue) {
                                tapsToOpenBetaMenu -> {
                                    hiddenBetaTaps.intValue = 0
                                    betaMenuHintToast.cancel()
                                    onNavigateToBetaMenu()
                                }

                                in 4..tapsToOpenBetaMenu -> {
                                    betaMenuHintToast.cancel()
                                    betaMenuHintToast.setText("${tapsToOpenBetaMenu - taps} more taps to open the seeeecret beta menu")
                                    betaMenuHintToast.show()
                                }
                            }
                        }) {
                    Text(
                        "FlickBoard v${BuildConfig.VERSION_NAME}$variant",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (BuildConfig.FLAVOR == "beta") {
                    MenuPageLink(
                        onClick = { onNavigateToBetaMenu() },
                        icon = painterResource(R.drawable.baseline_bug_report_24),
                        label = "Beta Options",
                    )
                }
                MenuPageLink(
                    onClick = { openUri(Uri.parse("https://discord.gg/tVp8MGaeUr")) },
                    icon = painterResource(R.drawable.baseline_chat_24),
                    label = "Discuss on Discord",
                )
                MenuPageLink(
                    onClick = { openUri(Uri.parse("https://github.com/nightkr/flickboard")) },
                    icon = painterResource(id = R.drawable.baseline_code_24),
                    label = "View Source on GitHub"
                )
                MenuPageLink(
                    onClick = { openUri(Uri.parse("https://github.com/nightkr/flickboard/issues")) },
                    icon = painterResource(id = R.drawable.baseline_bug_report_24),
                    label = "Report Bugs on GitHub"
                )
            }
        }
        SettingsKeyboardPreview(sharedTransitionScope, animatedVisibilityScope)
    }

}

@Composable
fun SettingsTitle(text: String) {
    Text(
        text,
        style = Typography.titleLarge,
        modifier = Modifier
            .padding(8.dp)
            .padding(top = 16.dp)
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsSectionPage(
    section: SettingsSection,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    Column {
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .weight(1F)
        ) {
            section.settings.forEach { setting ->
                when (setting) {
                    is Setting.Bool -> BoolSetting(setting)
                    is Setting.Integer -> {} // Not rendered right now, implement if used anywhere
                    is Setting.Text -> TextSetting(setting)
                    is Setting.FloatSlider -> FloatSliderSetting(setting)
                    is Setting.EnumList<*> -> EnumListSetting(setting)
                    is Setting.Enum -> EnumSetting(setting)
                    is Setting.Image -> ImageSetting(setting)
                    is Setting.Colour -> ColourSetting(setting)
                }
            }
        }
        SettingsKeyboardPreview(sharedTransitionScope, animatedVisibilityScope)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsKeyboardPreview(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val enable = LocalAppSettings.current.enableKeyboardPreview
    val enableState = enable.state
    with(sharedTransitionScope) {
        Box {
            Column {
                val realKeyboardVisible = WindowInsets.isImeVisible
                val softwareKeyboardController = LocalSoftwareKeyboardController.current
                Surface(
                    Modifier
                        .clickable {
                            when {
                                realKeyboardVisible -> softwareKeyboardController?.hide()
                                else -> enable.currentValue = !enable.currentValue
                            }
                        }
                        .sharedElement(
                            rememberSharedContentState(MainActivitySharedElement.SettingsKeyboardPreviewHeader.toString()),
                            animatedVisibilityScope
                        ),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            realKeyboardVisible -> {
                                Text(
                                    text = "Keyboard active",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Icon(
                                    painterResource(R.drawable.baseline_keyboard_hide_24),
                                    "close",
                                    Modifier
                                        .padding(8.dp)
                                )
                            }

                            else -> {
                                Text(
                                    text = "Preview keyboard",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                                val hideIconAngle = animateFloatAsState(
                                    when {
                                        enableState.value -> 0F
                                        else -> 180F
                                    }, label = "hideIconAngle"
                                )
                                Icon(
                                    painterResource(R.drawable.baseline_arrow_drop_down_24),
                                    when {
                                        enableState.value -> "hide"
                                        else -> "show"
                                    },
                                    Modifier
                                        .padding(8.dp)
                                        .rotate(hideIconAngle.value)
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(enableState.value) {
                    ProvideDisplayLimits {
                        ConfiguredKeyboard(
                            onAction = { _, _, _ -> true }, // Keyboard provides internal visual feedback if enabled
                            modifier = Modifier
                                .fillMaxWidth()
                                .excludeFromBottomInset()
                                // sharedElement can't encompass AnimatedVisibility, or the size animation breaks
                                .sharedElement(
                                    rememberSharedContentState(MainActivitySharedElement.SettingsKeyboardPreview.toString()),
                                    animatedVisibilityScope
                                )
                        )
                    }
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
fun TextSetting(setting: Setting.Text) {
    val state = setting.state
    SettingRow {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingLabel(setting)
            }
            Row {
                TextField(
                    value = state.value,
                    onValueChange = { setting.currentValue = it },
                    placeholder = { setting.placeholder?.let { Text(it) } },
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
        writePreviewSettings = { readPrefs, prefs, option ->
            setting.writePreviewSettings(readPrefs, prefs)
            setting.writeTo(prefs, listOf(option))
        },
        previewOverride = null,
        previewForceLandscape = false,
    )
}

@Composable
fun <T : Labeled> EnumSetting(setting: Setting.Enum<T>) {
    if (setting.options.size > 1) {
        BaseEnumSetting(
            setting,
            valueLabel = { it.label },
            options = setting.options,
            optionSelectionControl = { selected, option ->
                RadioButton(
                    selected = selected == option,
                    onClick = { setting.currentValue = option })
            },
            optionIsSelected = { selected, option -> option == selected },
            onOptionSelected = { setting.currentValue = it },
            collapseOnOptionSelected = true,
            writePreviewSettings = { readPrefs, prefs, option ->
                setting.writePreviewSettings(readPrefs, prefs)
                setting.writeTo(prefs, option)
            },
            previewOverride = setting.previewOverride,
            previewForceLandscape = setting.previewForceLandscape,
        )
    }
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
    writePreviewSettings: (SharedPreferences, SharedPreferences.Editor, T) -> Unit,
    previewOverride: (@Composable (T) -> Unit)?,
    previewForceLandscape: Boolean,
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
                                        .also { it.edit { writePreviewSettings(it, this, option) } }
                                }
                                FlickBoardParent(prefs) {
                                    ProvideDisplayLimits(DisplayLimits.calculateCurrent().let {
                                        it.copy(isLandscape = previewForceLandscape || it.isLandscape)
                                    }) {
                                        when {
                                            previewOverride != null -> previewOverride(option)
                                            else -> ConfiguredKeyboard(
                                                onAction = null,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun SettingsHomePreview() {
    FlickBoardParent {
        Surface {
            SharedTransitionLayout {
                AnimatedVisibility(true) {
                    SettingsHomePage(
                        onNavigateToSection = {},
                        onNavigateToTutorial = {},
                        onNavigateToBetaMenu = {},
                        onNavigateToDescriber = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun SettingsSectionPagePreview() {
    FlickBoardParent {
        Surface {
            SharedTransitionLayout {
                AnimatedVisibility(true) {
                    SettingsSectionPage(
                        LocalAppSettings.current.all[0],
                        this@SharedTransitionLayout,
                        this@AnimatedVisibility,
                        Modifier.width(1000.dp)
                    )
                }
            }
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
                coroutineScope = rememberCoroutineScope()
            )
        )
    ) {
        content()
    }
}

class AppSettings(val ctx: SettingsContext) {
    // Intentionally not rendered
    val hasCompletedTutorial = Setting.Bool(
        key = "hasCompletedTutorial",
        label = "Has completed tutorial",
        defaultValue = false,
        ctx = ctx
    )

    val letterLayers = Setting.EnumList(
        // Renaming this would reset the people's selected layer..
        key = "layout",
        label = "Letter layouts",
        defaultValue = listOf(LetterLayerOption.English),
        options = LetterLayerOption.entries,
        tryEnumValueOfT = ::tryEnumValueOf,
        ctx = ctx,
        writePreviewSettings = { readPrefs, prefs ->
            if (enabledLayers.readFrom(readPrefs) == EnabledLayers.Numbers) {
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
        tryEnumValueOfT = ::tryEnumValueOf,
        ctx = ctx,
        writePreviewSettings = { _, prefs ->
            enabledLayers.writeTo(prefs, EnabledLayers.DoubleLetters)
        }
    )

    val numericLayer = Setting.Enum(
        key = "numericLayer",
        label = "Number layout",
        defaultValue = NumericLayerOption.Phone,
        options = NumericLayerOption.entries,
        tryEnumValueOfT = ::tryEnumValueOf,
        ctx = ctx,
        writePreviewSettings = { readPrefs, prefs ->
            if (enabledLayers.readFrom(readPrefs) == EnabledLayers.Letters) {
                enabledLayers.writeTo(prefs, EnabledLayers.Numbers)
            }
        }
    )

    val enabledLayers = Setting.Enum(
        key = "enabledLayers",
        label = "Enabled layers",
        defaultValue = EnabledLayers.All,
        options = EnabledLayers.entries,
        tryEnumValueOfT = ::tryEnumValueOf,
        ctx = ctx
    )

    val enabledLayersLandscape = Setting.Enum(
        key = "enabledLayersLandscape",
        label = "Enabled layers (landscape)",
        defaultValue = EnabledLayersLandscape.Inherit,
        options = EnabledLayersLandscape.entries,
        tryEnumValueOfT = EnabledLayersLandscape::tryEnumValueOf,
        ctx = ctx,
        previewForceLandscape = true,
    )

    /**
     * Used for actions that need to modify this setting depending on the active context.
     *
     * Should not be used for UI rendering, as [SettingProjection] does not include state tracking,
     * nor does this function try to track whether the projection "mode" is still valid. For these
     * use cases, prefer [enabledLayersForCurrentOrientation].
     *
     * [SettingProjection.currentValue] may be null if the projection is outdated. Note that this
     * is *not exhaustive*.
     */
    fun enabledLayersProjectionForOrientation(displayLimits: DisplayLimits?): SettingProjection<EnabledLayers?> =
        run {
            val landscapeSetting =
                enabledLayersLandscape.tryMap(
                    get = { setting ->
                        (setting as? EnabledLayersLandscape.Set)?.setting
                    },
                    set = { _, it ->
                        EnabledLayersLandscape.Set(it)
                    },
                )
            when {
                landscapeSetting.currentValue != null
                        && displayLimits?.isLandscape ?: false -> landscapeSetting

                else -> enabledLayers.tryMap(
                    get = { it },
                    set = { _, it -> it })
            }
        }

    val enabledLayersForCurrentOrientation: State<EnabledLayers>
        @Composable get() = when {
            LocalDisplayLimits.current?.isLandscape == true -> {
                val landscape = enabledLayersLandscape.state
                val portrait = enabledLayers.state
                remember {
                    derivedStateOf {
                        when (val landscapeValue = landscape.value) {
                            is EnabledLayersLandscape.Set -> landscapeValue.setting
                            EnabledLayersLandscape.Inherit -> portrait.value
                        }
                    }
                }
            }

            else -> enabledLayers.state
        }

    val handedness = Setting.Enum(
        key = "handedness",
        label = "Handedness",
        defaultValue = Handedness.RightHanded,
        options = Handedness.entries,
        tryEnumValueOfT = ::tryEnumValueOf,
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

    val landscapeSplit = Setting.FloatSlider(
        key = "landscapeSplit",
        label = "Landscape split",
        defaultValue = 0F,
        range = 0F..1F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val landscapeControlSection = Setting.Enum<ControlSectionOption>(
        key = "landscapeControlSection",
        label = "Control section mode in landscape",
        defaultValue = ControlSectionOption.Single,
        options = ControlSectionOption.entries,
        tryEnumValueOfT = { tryEnumValueOf<ControlSectionOption>(it) },
        ctx = ctx,
        previewForceLandscape = true,
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

    val enableToggleShowSymbolsGesture = Setting.Bool(
        key = "enableToggleShowSymbolsGesture",
        label = "Enable toggle show symbols/letters gestures",
        defaultValue = true,
        ctx = ctx,
        description = "To use, swipe up from spacebar",
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

    val keyboardMargin = Setting.FloatSlider(
        key = "keyboardMargin",
        label = "Keyboard margin",
        defaultValue = 0F,
        range = 0F..8F,
        ctx = ctx
    )

    val keyboardMarginBottomPortrait = Setting.FloatSlider(
        key = "keyboardMarginBottomPortrait",
        label = "Keyboard bottom margin (portrait)",
        defaultValue = 0F,
        range = 0F..100F,
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
    val keyColourChroma = Setting.FloatSlider(
        key = "keyColourChroma",
        label = "Key colour saturation",
        defaultValue = 20F,
        range = 0F..100F,
        ctx = ctx,
        description = "Only applies when using a custom colour",
    )
    val keyColourTone = Setting.Enum<MaterialToneMode>(
        key = "keyColourTone",
        label = "Key colour brightness",
        defaultValue = MaterialToneMode.System,
        options = MaterialToneMode.entries,
        tryEnumValueOfT = { tryEnumValueOf<MaterialToneMode>(it) },
        ctx = ctx,
        description = "Only applies when using a custom colour",
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

    val enableLongSwipes = Setting.Bool(
        key = "enableLongSwipes",
        label = "Enable long swipes",
        description = "Enables shortcut actions on extra long swipes",
        defaultValue = false,
        ctx = ctx
    )

    val longSwipeThreshold = Setting.FloatSlider(
        key = "longSwipeThreshold",
        label = "Long swipe threshold",
        range = 0.5F..4F,
        defaultValue = 1.5F,
        ctx = ctx,
        render = Setting.FloatSlider::percentage
    )

    val enableAdvancedModifiers = Setting.Bool(
        key = "enableAdvancedModifiers",
        label = "Enable advanced modifiers",
        description = "Allows ctrl and alt modifiers to be toggled by swiping diagonally from space",
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

    val longHoldOnCounterClockwiseCircle = Setting.Bool(
        key = "digitOnCounterClockwiseCircle",
        label = "Type digit on counter-clockwise circle",
        defaultValue = false,
        ctx = ctx
    )

    val disabledDeadkeys = Setting.Text(
        key = "disabledDeadkeys",
        label = "Disabled deadkeys",
        defaultValue = "",
        ctx = ctx,
        description = "Special characters that should never be merged into the previous character",
        placeholder = "(none)"
    )

    val keyHeight = Setting.FloatSlider(
        key = "keyHeight",
        label = "Key height",
        defaultValue = 72F,
        range = 48F..128F,
        ctx = ctx
    )

    val swipeThreshold = Setting.FloatSlider(
        key = "swipeThreshold",
        label = "Swipe threshold",
        description = "How far you need to drag before a tap becomes a swipe",
        defaultValue = 8F,
        range = 8F..96F,
        ctx = ctx
    )

    val fastSwipeThreshold = Setting.FloatSlider(
        key = "fastSwipeTreshold",
        label = "Fast swipe threshold",
        description = "How far you need to drag between each fast action tick",
        defaultValue = 16F,
        range = 8F..96F,
        ctx = ctx,
    )

    val gestureRecognizer = Setting.Enum(
        key = "gestureRecognizer",
        "Gesture recognizer",
        defaultValue = GestureRecognizer.Default,
        options = GestureRecognizer.entries,
        tryEnumValueOfT = ::tryEnumValueOf,
        ctx = ctx,
        previewOverride = { Text(it.description) },
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

    val enableHapticFeedbackOnGestureStart = Setting.Bool(
        key = "enableHapticFeedbackOnGestureStart",
        label = "Vibrate on gesture start",
        defaultValue = false,
        ctx = ctx
    )

    val enableHapticFeedbackOnGestureSuccess = Setting.Bool(
        key = "enableHapticFeedback",
        label = "Vibrate on gesture finish",
        defaultValue = true,
        ctx = ctx
    )

    val enableVisualFeedback = Setting.Bool(
        key = "enableVisualFeedback",
        label = "Highlight taken actions",
        defaultValue = true,
        ctx = ctx
    )

    val visualFeedbackInvertColourScheme = Setting.Bool(
        key = "visualFeedbackInvertColourScheme",
        label = "Make visual highlight extra prominent",
        defaultValue = false,
        ctx = ctx
    )

    val enableKeyboardPreview = Setting.Bool(
        key = "enableKeyboardPreview",
        label = "Enable keyboard preview",
        defaultValue = true,
        ctx = ctx
    )

    val dropLastGesturePoint = Setting.Bool(
        key = "dropLastGesturePoint",
        label = "Ignore last gesture point",
        defaultValue = false,
        ctx = ctx,
        description = "This can help against some devices that insert erroneous motion at the end of gestures",
    )

    val ignoreJumpsLongerThanPx = Setting.FloatSlider(
        key = "ignoreJumpsLongerThanPx",
        label = "Ignore jumps larger than limit",
        defaultValue = 600F,
        range = 16F..600F,
        ctx = ctx,
        description = "This can help against some devices that insert erroneous motion in the middle of " +
                "gestures, at the cost of sometimes causing misinput if the device lags",
    )

    val flicksMustBeLongerThanSeconds = Setting.FloatSlider(
        key = "flicksMustBeLongerThanSeconds",
        label = "Swipes must be longer than limit",
        defaultValue = 0F,
        range = 0F..1F,
        ctx = ctx,
        description = "All gestures shorter than the limit will be forcibly interpreted as taps, rather than swipes",
        render = { String.format(locale = Locale.getDefault(), "%.2fs", it) }
    )

    val noReverseRtlBrackets = Setting.Bool(
        key = "noReverseRtlBrackets",
        label = "Do not reverse brackets in right-to-left languages",
        defaultValue = isSamsungDevice,
        ctx = ctx,
        description = "This is required for the correct brackets to be typed on some devices (especially Samsung)",
    )

    // Pseudo-option used to store history
    val emojiHistory =
        Setting.Text(key = "emojiHistory", label = "Emoji history", defaultValue = "", ctx = ctx)

    val saveEmojiHistory = Setting.Bool(
        key = "saveEmojiHistory",
        label = "Remember recent emojis",
        defaultValue = false,
        ctx = ctx,
        description = "History is only saved locally, and will not be shared",
        onChange = {
            // Reset history on disable
            if (!it) {
                emojiHistory.currentValue = ""
            }
        },
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
                    enabledLayersLandscape,
                    handedness,
                    keyHeight,
                    keyboardMargin,
                    landscapeLocation,
                    landscapeScale,
                    landscapeSplit,
                    landscapeControlSection,
                    portraitLocation,
                    portraitScale,
                    keyboardMarginBottomPortrait,
                )
            ),
            SettingsSection(
                key = "aesthetics", label = "Aesthetics", icon = R.drawable.baseline_palette_24,
                settings = listOf(
                    showLetters,
                    showSymbols,
                    enableToggleShowSymbolsGesture,
                    showNumbers,
                    enableHiddenActions,
                    keyRoundness,
                    actionVisualBiasCenter,
                    actionVisualScale,
                    keyColour,
                    keyColourChroma,
                    keyColourTone,
                    keyOpacity,
                    backgroundOpacity,
                    backgroundImage,
                )
            ),
            SettingsSection(
                key = "behaviour",
                label = "Behaviour",
                icon = R.drawable.baseline_app_settings_alt_24,
                settings = listOf(
                    enableFastActions,
                    enableLongSwipes,
                    longSwipeThreshold,
                    enableAdvancedModifiers,
                    periodOnDoubleSpace,
                    longHoldOnClockwiseCircle,
                    longHoldOnCounterClockwiseCircle,
                    disabledDeadkeys,
                    swipeThreshold,
                    fastSwipeThreshold,
                    gestureRecognizer,
                    circleJaggednessThreshold,
                    circleDiscontinuityThreshold,
                    circleAngleThreshold,
                )
            ),
            SettingsSection(
                key = "feedback", label = "Feedback", icon = R.drawable.baseline_vibration_24,
                settings = listOf(
                    enableHapticFeedbackOnGestureStart,
                    enableHapticFeedbackOnGestureSuccess,
                    enableVisualFeedback,
                    visualFeedbackInvertColourScheme,
                    enablePointerTrail,
                ),
            ),
            SettingsSection(
                key = "workarounds",
                label = "Workarounds",
                icon = R.drawable.baseline_bug_report_24,
                settings = listOf(
                    dropLastGesturePoint,
                    ignoreJumpsLongerThanPx,
                    flicksMustBeLongerThanSeconds,
                    noReverseRtlBrackets,
                ),
            ),
            SettingsSection(
                key = "privacy",
                label = "Privacy",
                icon = R.drawable.baseline_fingerprint_24,
                settings = listOf(saveEmojiHistory)
            ),
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

enum class EnabledLayers(
    override val label: String,
    val isSingleSided: Boolean = false,
) : Labeled {
    All("All"),
    Letters("Letters only", isSingleSided = true),
    Numbers("Numbers only", isSingleSided = true),
    DoubleLetters("Double letters"),
    AllMiniNumbers("All (mini numbers)"),
    AllMiniNumbersMiddle("All (mini numbers in middle)"),
    AllMiniNumbersOpposite("All (mini numbers on opposite side)");

    val toggleNumbers: EnabledLayers?
        get() = when (this) {
            Letters -> Numbers
            Numbers -> Letters
            DoubleLetters -> All
            All -> DoubleLetters
            else -> null
        }
}

sealed interface EnabledLayersLandscape : Labeled {
    data class Set(val setting: EnabledLayers) : EnabledLayersLandscape {
        override val label: String = setting.label
        override fun toString(): String = setting.toString()
    }

    data object Inherit : EnabledLayersLandscape {
        override val label: String = "Same as portrait"
    }

    companion object {
        val entries = listOf(Inherit) + EnabledLayers.entries.map(::Set)
        fun tryEnumValueOf(str: String): EnabledLayersLandscape? = when (str) {
            "Inherit" -> Inherit
            else -> tryEnumValueOf<EnabledLayers>(str)?.let(::Set)
        }
    }
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
    Arabic("Arabic (MessagEase)", AR_MESSAGEASE),
    Danish("Danish (MessagEase)", DA_MESSAGEASE),
    English("English (MessagEase)", EN_MESSAGEASE),
    EnglishThumbKey("English (Thumb-Key)", EN_THUMBKEY),
    French("French (MessagEase)", FR_MESSAGEASE),
    FrenchExt("French (Extended MessagEase)", FR_EXT_MESSAGEASE),
    FrenchPunc("French (Punctuation MessagEase)", FR_PUNC_MESSAGEASE),
    German("German (MessagEase)", DE_MESSAGEASE),
    GermanEnglish("German/English (MessagEase-style)", EN_DE_MESSAGEASE),
    GermanEsperanto("German/Esperanto (MessagEase-style)", DE_EO_MESSAGEASE),
    Hebrew("Hebrew (MessagEase)", HEB_MESSAGEASE),
    Hungarian("Hungarian (MessagEase)", HU_MESSAGEASE),
    HungarianDT("Hungarian (MessagEase-style, by Dániel Tenke)", HU_DT_MESSAGEASE),
    HungarianMF("Hungarian (MessagEase-style, by Máté Farkas)", HU_MF_MESSAGEASE),
    HungarianUUp("Hungarian (MessagEase-style, U always up)", HU_UUP_MESSAGEASE),
    Italian("Italian (MessagEase)", IT_MESSAGEASE),
    NorwegianThumbKey("Norwegian (Thumb-Key)", NO_THUMBKEY),
    Persian("Persian (MessagEase)", FA_MESSAGEASE),
    PersianThumbKey("Persian (Thumb-Key)", FA_THUMBKEY),
    PolishRmitura("PL (MessagEase-style, Rmitura)", PL_RMITURA_MESSAGEASE),
    Portuguese("Portuguese (MessagEase)", PT_MESSAGEASE),
    PortugueseIos("Portuguese (MessagEase, iOS)", PT_IOS_MESSAGEASE),
    Russian("Russian (MessagEase)", RU_MESSAGEASE),
    RussianPhonetic("Russian phonetic (MessagEase)", RU_PHONETIC_MESSAGEASE),
    Spanish("Spanish (MessagEase)", ES_MESSAGEASE),
    Swedish("Swedish (MessagEase-style)", SV_MESSAGEASE),
    SwedishDE("Swedish (MessagEase, German-style)", SV_DE_MESSAGEASE),
    Turkish("Turkish (MessagEase)", TR_MESSAGEASE),
    Ukrainian("Ukrainian (MessagEase)", UK_MESSAGEASE),
    UkrainianRussian("Ukrainian Russian (MessagEase)", UK_RU_MESSAGEASE),
}

enum class NumericLayerOption(
    override val label: String,
    val fullSizedLayer: (Layout) -> Layer,
    val miniLayer: (Layout) -> Layer
) : Labeled {
    Phone(
        "Phone",
        fullSizedLayer = { messageaseNumericPhoneLayer(it.digits) },
        miniLayer = { miniNumbersPhoneLayer(it.digits) },
    ),
    Calculator(
        "Calculator",
        fullSizedLayer = { messageaseNumericCalculatorLayer(it.digits) },
        miniLayer = { miniNumbersCalculatorLayer(it.digits) },
    ),
}

enum class ControlSectionOption(override val label: String) : Labeled {
    Single("Single"),
    DoubleInside("Double (inside)"),
    DoubleOutside("Double (outside)"),
}

class SettingsContext(val prefs: SharedPreferences, val coroutineScope: CoroutineScope)

abstract class SettingProjection<T> {
    abstract var currentValue: T

    inline fun modify(f: (T) -> T) {
        currentValue = f(currentValue)
    }

    inline fun tryModify(f: (T) -> Boxed<T>?): Boolean {
        currentValue = (f(currentValue) ?: return false).value
        return true
    }

    fun <U> map(
        get: (T) -> U,
        set: (T, U) -> T,
    ): SettingProjection<U> = let { base ->
        object : SettingProjection<U>() {
            override var currentValue: U
                get() = get(base.currentValue)
                set(value) {
                    base.modify { set(it, value) }
                }
        }
    }

    fun <U : Any> tryMap(
        get: (T) -> U?,
        set: (T, U) -> T?,
    ): SettingProjection<U?> = let { base ->
        object : SettingProjection<U?>() {
            override var currentValue: U?
                get() = get(base.currentValue)
                set(value) {
                    base.tryModify { oldBase ->
                        set(
                            oldBase,
                            (value ?: return@tryModify null)
                        )?.let(::Boxed)
                    }
                }
        }
    }
}

sealed class Setting<T>(private val ctx: SettingsContext) : SettingProjection<T>() {
    abstract val key: String
    abstract val label: String
    abstract val description: String?

    override var currentValue: T
        get() = readFrom(ctx.prefs)
        set(value) = ctx.prefs.edit { writeTo(this, value) }

    fun resetToDefault() {
        ctx.prefs.edit { resetIn(this) }
    }

    abstract fun readFrom(prefs: SharedPreferences): T
    abstract fun writeTo(prefs: SharedPreferences.Editor, value: T)
    fun resetIn(prefs: SharedPreferences.Editor) {
        prefs.remove(key)
    }

    abstract fun readFromJson(json: JsonReader): T?
    abstract fun writeToJson(json: JsonWriter, value: T)

    fun exportToJson(prefs: SharedPreferences, json: JsonWriter) {
        writeToJson(json, readFrom(prefs))
    }

    fun importFromJson(prefs: SharedPreferences.Editor, json: JsonReader) {
        readFromJson(json)?.let { writeTo(prefs, it) }
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
        .shareIn(
            ctx.coroutineScope,
            // If the Flow is stopped once cachedValue is loaded then updates may be missed
            // Instead, make sure to constrain coroutineScope to the lifetime of the setting to
            // avoid leaks
            SharingStarted.Lazily,
            replay = 1
        )

    val state: State<T>
        @Composable
        get() = watch.collectAsState(initial = cachedValue)

    class Bool(
        override val key: String,
        override val label: String,
        val defaultValue: Boolean,
        ctx: SettingsContext,
        override val description: String? = null,
        val onChange: (Boolean) -> Unit = {},
    ) : Setting<Boolean>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Boolean =
            prefs.getBoolean(key, defaultValue)

        override fun writeTo(prefs: SharedPreferences.Editor, value: Boolean) {
            onChange(value)
            prefs.putBoolean(key, value)
        }

        override fun readFromJson(json: JsonReader): Boolean = json.nextBoolean()
        override fun writeToJson(json: JsonWriter, value: Boolean) {
            json.value(value)
        }
    }

    class Integer(
        override val key: String,
        override val label: String,
        val defaultValue: Int,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Int>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Int = prefs.getInt(key, defaultValue)
        override fun writeTo(prefs: SharedPreferences.Editor, value: Int) {
            prefs.putInt(key, value)
        }

        override fun readFromJson(json: JsonReader): Int = json.nextInt()
        override fun writeToJson(json: JsonWriter, value: Int) {
            json.value(value)
        }
    }

    class Text(
        override val key: String,
        override val label: String,
        val defaultValue: String,
        ctx: SettingsContext,
        override val description: String? = null,
        val placeholder: String? = null,
    ) : Setting<String>(ctx) {
        override fun readFrom(prefs: SharedPreferences): String =
            prefs.getString(key, defaultValue) ?: defaultValue

        override fun writeTo(prefs: SharedPreferences.Editor, value: String) {
            prefs.putString(key, value)
        }

        override fun readFromJson(json: JsonReader): String = json.nextString()
        override fun writeToJson(json: JsonWriter, value: String) {
            json.value(value)
        }
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
        override fun readFrom(prefs: SharedPreferences): Float = prefs.getFloat(key, defaultValue)
        override fun writeTo(prefs: SharedPreferences.Editor, value: Float) {
            prefs.putFloat(key, value.coerceIn(range))
        }

        override fun readFromJson(json: JsonReader): Float = json.nextDouble().toFloat()
        override fun writeToJson(json: JsonWriter, value: Float) {
            json.value(value)
        }

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
        /** Should be [tryEnumValueOf] */
        val tryEnumValueOfT: (String) -> T?,
        ctx: SettingsContext,
        override val description: String? = null,
        val writePreviewSettings: (SharedPreferences, SharedPreferences.Editor) -> Unit = { _, _ -> },
    ) : Setting<List<T>>(ctx) {
        override fun readFrom(prefs: SharedPreferences): List<T> =
            prefs.getString(key, null)
                ?.split(',')
                ?.mapNotNull { it.takeIf { it.isNotEmpty() }?.let(tryEnumValueOfT) }
                ?: defaultValue

        override fun writeTo(prefs: SharedPreferences.Editor, value: List<T>) {
            prefs.putString(key, value.joinToString(",") { it.toString() })
        }

        override fun readFromJson(json: JsonReader): List<T> = mutableListOf<T>().also { out ->
            json.beginArray()
            while (json.peek() != JsonToken.END_ARRAY) {
                tryEnumValueOfT(json.nextString())?.let(out::add)
            }
            json.endArray()
        }

        override fun writeToJson(json: JsonWriter, value: List<T>) {
            json.beginArray()
            value.forEach {
                json.value(it.toString())
            }
            json.endArray()
        }
    }

    class Enum<T : Labeled>(
        override val key: String,
        override val label: String,
        val defaultValue: T,
        val options: List<T>,
        /** Should be [tryEnumValueOf]<T> */
        val tryEnumValueOfT: (String) -> T?,
        ctx: SettingsContext,
        override val description: String? = null,
        val writePreviewSettings: (SharedPreferences, SharedPreferences.Editor) -> Unit = { _, _ -> },
        val previewOverride: (@Composable (T) -> Unit)? = null,
        val previewForceLandscape: Boolean = false,
    ) : Setting<T>(ctx) {
        override fun readFrom(prefs: SharedPreferences): T =
            prefs.getString(key, null)?.let(tryEnumValueOfT) ?: defaultValue

        override fun writeTo(prefs: SharedPreferences.Editor, value: T) {
            prefs.putString(key, value.toString())
        }

        override fun readFromJson(json: JsonReader): T? = tryEnumValueOfT(json.nextString())
        override fun writeToJson(json: JsonWriter, value: T) {
            json.value(value.toString())
        }
    }

    class Image(
        override val key: String,
        override val label: String,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Uri?>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Uri? =
            prefs.getString(key, null)?.let(Uri::parse)

        override fun writeTo(prefs: SharedPreferences.Editor, value: Uri?) {
            prefs.putString(key, value?.toString())
        }

        override fun readFromJson(json: JsonReader): Uri? =
            json.orNull { nextString() }?.let(Uri::parse)

        override fun writeToJson(json: JsonWriter, value: Uri?) {
            json.value(value?.toString())
        }
    }

    class Colour(
        override val key: String,
        override val label: String,
        ctx: SettingsContext,
        override val description: String? = null,
    ) : Setting<Color?>(ctx) {
        override fun readFrom(prefs: SharedPreferences): Color? =
            prefs.getInt(key, 0).takeUnless { it == 0 }?.let { Color(it) }

        override fun writeTo(prefs: SharedPreferences.Editor, value: Color?) {
            prefs.putInt(key, value?.toArgb() ?: 0)
        }

        override fun readFromJson(json: JsonReader): Color? =
            json.nextInt().takeUnless { it == 0 }?.let { Color(it) }

        override fun writeToJson(json: JsonWriter, value: Color?) {
            json.value(value?.toArgb() ?: 0)
        }
    }
}

enum class GestureRecognizer(override val label: String, val description: String) : Labeled {
    Default("Default", description = "The default FlickBoard gesture recognizer"),
    Dollar1(
        "$1 (OLD EXPERIMENT)",
        description = "Experimental legacy gesture recognizer (many recognition settings do not apply)"
    ),
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
