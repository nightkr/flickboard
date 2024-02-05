package se.nullable.flickboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.layouts.SV_MESSAGEASE

@Composable
fun Keyboard(layout: Layout, onAction: (Action) -> Unit, modifier: Modifier = Modifier) {
    var layer = layout.numericLayer ?: layout.mainLayer
    layout.controlLayer?.let { layer = layer.chain(it) }
    if (layout.numericLayer != null) {
        layer = layer.chain(layout.numericLayer.merge(layout.mainLayer))
    }
    Column(modifier) {
        layer.keyRows.forEach { row ->
            Row {
                row.forEach { key ->
                    Key(key, onAction = onAction)
                }
            }
        }
    }
}

@Composable
@Preview
fun KeyboardPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    Column {
        Row {
            Text(text = "Tapped: $lastAction")
        }
        Keyboard(layout = SV_MESSAGEASE, onAction = { lastAction = it })
    }
}