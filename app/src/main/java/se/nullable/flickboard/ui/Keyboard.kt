package se.nullable.flickboard.ui

import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.model.ShiftState
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.model.layouts.EN_MESSAGEASE
import se.nullable.flickboard.model.layouts.OVERLAY_ADVANCED_MODIFIERS_MESSAGEASE_LAYER
import se.nullable.flickboard.model.layouts.OVERLAY_MESSAGEASE_LAYER
import se.nullable.flickboard.model.layouts.OVERLAY_TOGGLE_SYMBOLS_MESSAGEASE_LAYER
import se.nullable.flickboard.ui.layout.Grid
import se.nullable.flickboard.util.toOnAccentContainer
import java.io.IOException

// Returns false if action could not be processed
typealias OnAction = (Action) -> Boolean

@Composable
fun Keyboard(
    layout: Layout,
    onAction: OnAction?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    onModifierStateUpdated: (ModifierState) -> Unit = {},
    showAllModifiers: Boolean = false,
    overrideEnabledLayers: EnabledLayers? = null,
) {
    val context = LocalContext.current
    val appSettings = LocalAppSettings.current
    val enabledLayersForCurrentOrientation = appSettings.enabledLayersForCurrentOrientation
    val overrideEnabledLayersState = rememberUpdatedState(overrideEnabledLayers)
    val enabledLayers = remember {
        derivedStateOf {
            when (val override = overrideEnabledLayersState.value) {
                null -> enabledLayersForCurrentOrientation.value
                else -> override
            }
        }
    }
    val numericLayer = appSettings.numericLayer.state
    val secondaryLetterLayer = appSettings.secondaryLetterLayer.state
    val handedness = appSettings.handedness.state
    val backgroundOpacity = appSettings.backgroundOpacity.state
    val enablePointerTrail = appSettings.enablePointerTrail.state
    val shownActionClasses = appSettings.shownActionClasses
    val enableHiddenActions = appSettings.enableHiddenActions.state
    val enableAdvancedModifiers = appSettings.enableAdvancedModifiers.state
    val enableToggleShowSymbols = appSettings.enableToggleShowSymbolsGesture.state
    val keyColour = appSettings.keyColour.state
    val keyColourChroma = appSettings.keyColourChroma.state
    val toneMode = appSettings.keyColourTone.state
    val toneConfig = rememberUpdatedState(toneMode.value.config)
    val backgroundImage = appSettings.backgroundImage.state
    val keyboardMargin = appSettings.keyboardMargin.state
    val noReverseRtlBrackets = appSettings.noReverseRtlBrackets.state
    var modifierState: ModifierState by remember { mutableStateOf(ModifierState()) }
    LaunchedEffect(modifierState) {
        onModifierStateUpdated(modifierState)
    }
    val layoutState = rememberUpdatedState(layout)
    val mergedFullSizedNumericLayer =
        remember {
            derivedStateOf {
                numericLayer.value.fullSizedLayer(layoutState.value)
                    .mergeFallback(layoutState.value.symbolLayer)
            }
        }
    val mergedMiniNumericLayer =
        remember {
            derivedStateOf {
                numericLayer.value.miniLayer(layoutState.value)
                    .mergeFallback(layoutState.value.miniSymbolLayer)
                    .let { it.setShift(it.autoShift()) }
            }
        }
    val layersByShiftState = remember {
        derivedStateOf {
            var mainLayer = layoutState.value.mainLayer
            if (enableAdvancedModifiers.value) {
                mainLayer = mainLayer.mergeFallback(OVERLAY_ADVANCED_MODIFIERS_MESSAGEASE_LAYER)
            }
            if (enableToggleShowSymbols.value) {
                mainLayer = mainLayer.mergeFallback(OVERLAY_TOGGLE_SYMBOLS_MESSAGEASE_LAYER)
            }
            val shift = layoutState.value.shiftLayer
                .mergeFallback(
                    OVERLAY_MESSAGEASE_LAYER.mergeFallback(mergedFullSizedNumericLayer.value)
                        .autoShift()
                )
            mapOf(
                ShiftState.Normal to mainLayer
                    .mergeFallback(
                        OVERLAY_MESSAGEASE_LAYER.mergeFallback(
                            mergedFullSizedNumericLayer.value
                        )
                    )
                    .setShift(shift),

                ShiftState.Shift to shift,

                // Don't shift numeric layer in caps lock
                ShiftState.CapsLock to layoutState.value.shiftLayer
                    .mergeFallback(
                        OVERLAY_MESSAGEASE_LAYER
                            .autoShift()
                            .mergeFallback(mergedFullSizedNumericLayer.value)
                    ),
            ).mapValues {
                it.value.filterActions(
                    shownActionClasses = shownActionClasses.value,
                    enableHiddenActions = enableHiddenActions.value,
                )
            }
        }
    }
    val layer by remember {
        derivedStateOf {
            val activeLayer = layersByShiftState.value[modifierState.shift]!!
            listOfNotNull(
                when (enabledLayers.value) {
                    EnabledLayers.All -> mergedFullSizedNumericLayer.value
                    EnabledLayers.AllMiniNumbers -> mergedMiniNumericLayer.value

                    EnabledLayers.DoubleLetters -> when {
                        modifierState.shift.isShifted -> secondaryLetterLayer.value.layout.shiftLayer
                        else -> secondaryLetterLayer.value.layout.mainLayer
                            .setShift(secondaryLetterLayer.value.layout.shiftLayer)
                    }.mergeFallback(mergedFullSizedNumericLayer.value)

                    else -> null
                },
                layoutState.value.controlLayer?.let { it.setShift(it.autoShift()) },
                when (enabledLayers.value) {
                    EnabledLayers.AllMiniNumbersMiddle -> mergedMiniNumericLayer.value
                    else -> null
                },
                when (enabledLayers.value) {
                    EnabledLayers.Numbers -> mergedFullSizedNumericLayer.value
                    EnabledLayers.Letters, EnabledLayers.DoubleLetters, EnabledLayers.All,
                    EnabledLayers.AllMiniNumbers, EnabledLayers.AllMiniNumbersMiddle,
                    EnabledLayers.AllMiniNumbersOpposite -> activeLayer
                },
                when (enabledLayers.value) {
                    EnabledLayers.AllMiniNumbersOpposite -> mergedMiniNumericLayer.value
                    else -> null
                },
            )
                .let {
                    when (handedness.value) {
                        Handedness.RightHanded -> it
                        Handedness.LeftHanded -> it.asReversed()
                    }
                }
                .fold(Layer.empty, Layer::chain)
                .let {
                    when (layoutState.value.textDirection) {
                        TextDirection.LeftToRight -> it
                        TextDirection.RightToLeft -> when {
                            noReverseRtlBrackets.value -> it
                            else -> it.flipBrackets()
                        }
                    }
                }
        }
    }
    var globalPosition: Offset by remember { mutableStateOf(Offset.Zero) }
    var activeKeyPosition: State<Offset> by remember { mutableStateOf(mutableStateOf(Offset.Zero)) }
    var pointerTrailRelativeToActiveKey: List<Offset> by remember {
        mutableStateOf(emptyList(), policy = neverEqualPolicy())
    }
    var pointerTrailActive by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = pointerTrailActive) {
        if (!pointerTrailActive) {
            delay(2000)
            if (pointerTrailRelativeToActiveKey.isNotEmpty()) {
                pointerTrailRelativeToActiveKey = emptyList()
            }
        }
    }
    val pointerTrailColor =
        keyColour.value?.toOnAccentContainer(keyColourChroma.value, toneConfig.value)
            ?: MaterialTheme.colorScheme.onPrimaryContainer
    BoxWithConstraints(
        modifier
            .onGloballyPositioned { globalPosition = it.positionInRoot() }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    if (enablePointerTrail.value) {
                        clipRect {
                            val keyPosition = activeKeyPosition.value
                            pointerTrailRelativeToActiveKey.forEach {
                                this.drawCircle(
                                    pointerTrailColor,
                                    center = it + keyPosition,
                                    radius = 10.dp.toPx(),
                                    alpha = 0.4f
                                )
                            }
                        }
                    }
                }
            }
            .semantics {
                this.contentDescription = "FlickBoard keyboard"
            }
    ) {
        var thisWidth = maxWidth
        LocalDisplayLimits.current?.let { limits ->
            // Enforce portrait aspect ratio in landscape mode
            thisWidth = min(thisWidth, limits.portraitWidth)
        }
        thisWidth *= appSettings.currentScale
        val backgroundColor = toneConfig.value.surfaceColour
        val backgroundImagePainter = remember {
            derivedStateOf {
                try {
                    backgroundImage.value?.let {
                        BitmapPainter(
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(context.contentResolver, it)
                            ).asImageBitmap()
                        )
                    }
                } catch (e: IOException) {
                    Log.w("Keyboard", "Failed to load background image", e)
                    null
                } ?: ColorPainter(backgroundColor)
            }
        }
        Image(
            backgroundImagePainter.value,
            null,
            contentScale = ContentScale.Crop,
            alpha = backgroundOpacity.value,
            modifier = Modifier.matchParentSize()
        )
        Grid(
            modifier = Modifier
                .width(thisWidth)
                .align(
                    BiasAbsoluteAlignment(
                        horizontalBias = appSettings.currentLocation,
                        verticalBias = 0F
                    )
                )
                .padding(keyboardMargin.value.dp),
            columnGap = 1.dp,
            rowGap = 1.dp,
            rows = layer.keyRows.map { row ->
                {
                    row.forEach { key ->
                        androidx.compose.runtime.key(key) {
                            val keyPosition = remember { mutableStateOf(Offset.Zero) }
                            val keyPointerTrailListener = remember {
                                derivedStateOf {
                                    when {
                                        enablePointerTrail.value -> KeyPointerTrailListener(
                                            onDown = {
                                                activeKeyPosition = keyPosition
                                                pointerTrailActive = true
                                                pointerTrailRelativeToActiveKey = emptyList()
                                            },
                                            onUp = { pointerTrailActive = false },
                                            onTrailUpdate = {
                                                pointerTrailRelativeToActiveKey = it
                                            },
                                        )

                                        else -> null
                                    }
                                }
                            }
                            Key(
                                key,
                                onAction = onAction?.let { onAction ->
                                    { action ->
                                        modifierState = when (action) {
                                            is Action.ToggleShift -> modifierState.copy(shift = action.state)
                                            Action.ToggleCtrl -> modifierState.copy(ctrl = !modifierState.ctrl)
                                            Action.ToggleAlt -> modifierState.copy(alt = !modifierState.alt)
                                            Action.ToggleZalgo -> modifierState.copy(zalgo = !modifierState.zalgo)
                                            Action.ToggleSelect -> modifierState.copy(select = !modifierState.select)
                                            is Action.Jump -> modifierState.next()
                                                .copy(select = modifierState.select)

                                            else -> when {
                                                action.isHiddenAction -> modifierState
                                                else -> modifierState.next()
                                            }
                                        }
                                        onAction(action)
                                    }
                                },
                                modifierState = modifierState.takeUnless { showAllModifiers },
                                modifier = Modifier
                                    .colspan(key.colspan)
                                    .onGloballyPositioned {
                                        keyPosition.value = it.positionInRoot() - globalPosition
                                    },
                                enterKeyLabel = enterKeyLabel,
                                keyPointerTrailListener = keyPointerTrailListener,
                                layoutTextDirection = layout.textDirection,
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ConfiguredKeyboard(
    onAction: OnAction?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    onModifierStateUpdated: (ModifierState) -> Unit = {},
    overrideEnabledLayers: EnabledLayers? = null,
) {
    val appSettings = LocalAppSettings.current
    val enabledLetterLayers = appSettings.letterLayers.state.value
    Keyboard(
        layout = enabledLetterLayers.getOrNull(appSettings.activeLetterLayerIndex.state.value)?.layout
            ?: enabledLetterLayers.firstOrNull()?.layout
            ?: EN_MESSAGEASE,
        onAction = onAction,
        modifier = modifier,
        enterKeyLabel = enterKeyLabel,
        onModifierStateUpdated = onModifierStateUpdated,
        overrideEnabledLayers = overrideEnabledLayers,
    )
}

@Composable
fun KeyboardLayoutPreview(layout: Layout, showAllModifiers: Boolean = false) {
    FlickBoardParent {
        Keyboard(layout = layout, showAllModifiers = showAllModifiers, onAction = { true })
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KeyboardPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    FlickBoardParent {
        Surface {
            Column {
                Row {
                    Text(text = "Tapped: $lastAction")
                }
                ConfiguredKeyboard(onAction = {
                    lastAction = it
                    true
                })
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 1024, heightDp = 500)
fun PlayKeyboardPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    FlickBoardParent {
        Surface {
            val appSettings = LocalAppSettings.current
            AppSettingsProvider(prefs = MockedSharedPreferences(appSettings.ctx.prefs).also {
                appSettings.keyHeight.writeTo(it, 128F)
            }) {
                ConfiguredKeyboard(onAction = {
                    lastAction = it
                    true
                })
            }
        }
    }
}
