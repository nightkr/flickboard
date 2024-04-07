package se.nullable.flickboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.util.colourOfHctHue

@Composable
fun ColourPicker(onColourSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    val brush = remember {
        Brush.sweepGradient(
            (0..360 step 30).reversed()
                .map { colourOfHctHue(it) }
        )
    }
    val selectedColourPixel = remember { IntArray(1) }
    var bitmap = remember { ImageBitmap(1, 1) }

    Image(
        BrushPainter(brush),
        "colour picker",
        modifier = modifier
            .aspectRatio(1F)
            .clip(CircleShape)
            .drawWithCache {
                // Cache the drawn image into [bitmap], so that we can query it
                bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
                onDrawWithContent {
                    val parentCanvas = drawContext.canvas
                    drawContext.canvas = Canvas(bitmap)
                    drawContent()
                    drawContext.canvas = parentCanvas
                    drawImage(bitmap)
                }
            }
            .pointerInput(bitmap) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    while (true) {
                        val change = awaitDragOrCancellation(down.id) ?: break
                        change.consume()
                        bitmap.readPixels(
                            selectedColourPixel,
                            change.position.x
                                .toInt()
                                .coerceIn(0..<bitmap.width),
                            change.position.y
                                .toInt()
                                .coerceIn(0..<bitmap.height),
                            1,
                            1
                        )
                        onColourSelected(Color(selectedColourPixel[0]))
                    }
                }
            },
    )
}

@Composable
@Preview
fun ColourPickerPreview() {
    val colour = remember { mutableStateOf(Color.Transparent) }
    Box(Modifier.size(100.dp)) {
        Image(ColorPainter(colour.value), null)
        ColourPicker(onColourSelected = { colour.value = it })
    }
}