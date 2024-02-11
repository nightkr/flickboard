package se.nullable.flickboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import se.nullable.flickboard.model.layouts.EN_MESSAGEASE
import se.nullable.flickboard.model.layouts.MESSAGEASE_SYMBOLS_LAYER
import se.nullable.flickboard.model.layouts.OVERLAY_MESSAGEASE_LAYER

@Composable
fun Keyboard(
    layout: Layout,
    onAction: ((Action) -> Unit)?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    onModifierStateUpdated: (ModifierState) -> Unit = {},
) {
    val appSettings = LocalAppSettings.current
    val enabledLayers = appSettings.enabledLayers.state
    val numericLayer = appSettings.numericLayer.state
    val handedness = appSettings.handedness.state
    val enablePointerTrail = appSettings.enablePointerTrail.state
    var modifierState: ModifierState by remember { mutableStateOf(ModifierState()) }
    LaunchedEffect(modifierState) {
        onModifierStateUpdated(modifierState)
    }
    val mergedNumericLayer =
        remember { derivedStateOf { numericLayer.value.layer.mergeFallback(MESSAGEASE_SYMBOLS_LAYER) } }
    val mainLayerOverlay =
        remember { derivedStateOf { OVERLAY_MESSAGEASE_LAYER.mergeFallback(mergedNumericLayer.value) } }
    val shiftLayer =
        remember(layout) { derivedStateOf { layout.shiftLayer.mergeFallback(mainLayerOverlay.value.autoShift()) } }
    val mainLayer =
        remember(layout) {
            derivedStateOf {
                layout.mainLayer.mergeFallback(mainLayerOverlay.value).mergeShift(shiftLayer.value)
            }
        }
    val layer by remember(layout) {
        derivedStateOf {
            val activeLayer = when {
                modifierState.shift.isShifted -> shiftLayer.value
                else -> mainLayer.value
            }
            listOfNotNull(
                when (enabledLayers.value) {
                    EnabledLayers.All -> mergedNumericLayer.value
                    else -> null
                },
                layout.controlLayer?.let { it.mergeShift(it.autoShift()) },
                when (enabledLayers.value) {
                    EnabledLayers.Numbers -> mergedNumericLayer.value
                    EnabledLayers.Letters, EnabledLayers.All -> activeLayer
                },
            )
                .let {
                    when (handedness.value) {
                        Handedness.RightHanded -> it
                        Handedness.LeftHanded -> it.asReversed()
                    }
                }
                .fold(Layer.empty, Layer::chain)
        }
    }
    val columns = layer.keyRows.maxOf { row -> row.sumOf { it.colspan } }
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
    val pointerTrailColor = MaterialTheme.colorScheme.onSurface
    BoxWithConstraints(
        modifier
            .background(MaterialTheme.colorScheme.surface)
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
        val columnWidth = thisWidth / columns
        Column(
            Modifier
                .width(thisWidth)
                .align(
                    BiasAbsoluteAlignment(
                        horizontalBias = appSettings.currentLocation,
                        verticalBias = 0F
                    )
                )
        ) {
            layer.keyRows.forEachIndexed { rowI, row ->
                Row(Modifier.padding(top = rowI.coerceAtMost(1).dp)) {
                    row.forEachIndexed { keyI, key ->
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
                                        is Action.ToggleCtrl -> modifierState.copy(ctrl = !modifierState.ctrl)
                                        is Action.ToggleAlt -> modifierState.copy(alt = !modifierState.alt)
                                        else -> modifierState.next()
                                    }
                                    onAction(action)
                                }
                            },
                            modifierState = modifierState,
                            modifier = Modifier
                                .width(columnWidth * key.colspan)
                                .padding(start = keyI.coerceAtMost(1).dp)
                                .onGloballyPositioned {
                                    keyPosition.value = it.positionInRoot() - globalPosition
                                },
                            enterKeyLabel = enterKeyLabel,
                            keyPointerTrailListener = keyPointerTrailListener
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfiguredKeyboard(
    onAction: ((Action) -> Unit)?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    onModifierStateUpdated: (ModifierState) -> Unit = {},
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
    )
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
                ConfiguredKeyboard(onAction = { lastAction = it })
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
                ConfiguredKeyboard(onAction = { lastAction = it })
            }
        }
    }
}
