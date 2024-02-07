package se.nullable.flickboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.ShiftState

@Composable
fun Keyboard(
    layout: Layout,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
) {
    val layerOrder = LocalAppSettings.current.layerOrder.state
    var shiftState: ShiftState by remember(layout) { mutableStateOf(ShiftState.Normal) }
    val shiftLayer = remember(layout) { layout.shiftLayer.mergeFallback(layout.numericLayer) }
    val mainLayer =
        remember(layout) {
            layout.mainLayer.mergeFallback(layout.numericLayer).mergeShift(shiftLayer)
        }
    val layer by remember(layout) {
        derivedStateOf {
            val activeLayer = when {
                shiftState.isShifted -> shiftLayer
                else -> mainLayer
            }
            listOfNotNull(
                when (layerOrder.value) {
                    LayerOrder.LettersNumbers -> activeLayer
                    LayerOrder.NumbersLetters -> layout.numericLayer
                    LayerOrder.Letters, LayerOrder.Numbers -> null
                },
                layout.controlLayer?.let { it.mergeShift(it.autoShift()) },
                when (layerOrder.value) {
                    LayerOrder.Letters, LayerOrder.LettersNumbers -> layout.numericLayer
                    LayerOrder.Numbers, LayerOrder.NumbersLetters -> activeLayer
                },
            ).fold(Layer.empty, Layer::chain)
        }
    }
    val columns = layer.keyRows.maxOf { row -> row.sumOf { it.colspan } }
    BoxWithConstraints(modifier) {
        val columnWidth = this.maxWidth / columns
        Column {
            layer.keyRows.forEach { row ->
                Row {
                    row.forEach { key ->
                        Key(
                            key,
                            onAction = { action ->
                                shiftState = when (action) {
                                    is Action.Shift -> action.state
                                    else -> shiftState.next()
                                }
                                onAction(action)
                            },
                            modifier = Modifier.width(columnWidth * key.colspan),
                            enterKeyLabel = enterKeyLabel
                        )
                    }
                }
            }
        }
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
                Keyboard(layout = LocalAppSettings.current.layout, onAction = { lastAction = it })
            }
        }
    }
}