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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.ModifierState

@Composable
fun Keyboard(
    layout: Layout,
    onAction: ((Action) -> Unit)?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    onModifierStateUpdated: (ModifierState) -> Unit = {},
) {
    val enabledLayers = LocalAppSettings.current.enabledLayers.state
    val handedness = LocalAppSettings.current.handedness.state
    val landscapeLocation = LocalAppSettings.current.landscapeLocation.state
    val enablePointerTrail = LocalAppSettings.current.enablePointerTrail.state
    var modifierState: ModifierState by remember { mutableStateOf(ModifierState()) }
    LaunchedEffect(modifierState) {
        onModifierStateUpdated(modifierState)
    }
    val shiftLayer = remember(layout) { layout.shiftLayer.mergeFallback(layout.numericLayer) }
    val mainLayer =
        remember(layout) {
            layout.mainLayer.mergeFallback(layout.numericLayer).mergeShift(shiftLayer)
        }
    val layer by remember(layout) {
        derivedStateOf {
            val activeLayer = when {
                modifierState.shift.isShifted -> shiftLayer
                else -> mainLayer
            }
            listOfNotNull(
                when (enabledLayers.value) {
                    EnabledLayers.All -> layout.numericLayer
                    else -> null
                },
                layout.controlLayer?.let { it.mergeShift(it.autoShift()) },
                when (enabledLayers.value) {
                    EnabledLayers.Numbers -> layout.numericLayer
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
    ) {
        // Enforce portrait aspect ratio in landscape mode
        var thisWidth = maxWidth
        LocalDisplayLimits.current?.let { limits ->
            thisWidth = min(thisWidth, limits.portraitWidth)
        }
        val columnWidth = thisWidth / columns
        Column(
            Modifier
                .width(thisWidth)
                .align(
                    BiasAbsoluteAlignment(
                        horizontalBias = landscapeLocation.value / 100,
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
    Keyboard(
        layout = LocalAppSettings.current.layout.state.value.layout,
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