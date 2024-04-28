package se.nullable.flickboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
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
import se.nullable.flickboard.util.hctSetChroma
import se.nullable.flickboard.util.hctSetHue
import se.nullable.flickboard.util.toHct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColourPicker(colour: Color, onColourSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    val hueBrush = remember {
        Brush.linearGradient(
            (0..360 step 30).reversed()
                .map { colourOfHctHue(it) }
        )
    }
    val colourState = rememberUpdatedState(colour)
    val chromaBrush = remember {
        derivedStateOf {
            Brush.linearGradient((0..100 step 2).map { colourState.value.hctSetChroma(it.toDouble()) })
        }
    }
    val hct = remember { derivedStateOf { colourState.value.toHct() } }
    val selectedColourPixel = remember { IntArray(1) }
    var bitmap = remember { ImageBitmap(1, 1) }

    //
    @Composable
    fun BrushSliderTrack(brush: Brush) {
        androidx.compose.foundation.Canvas(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            drawLine(
                brush,
                Offset(0F, center.y),
                Offset(size.width, center.y),
                strokeWidth = 4.dp.toPx()
            )
        }
    }

    Column {
        Slider(
            value = hct.value.hue.toFloat(),
            onValueChange = { onColourSelected(colour.hctSetHue(it.toDouble())) },
            valueRange = 0F..360F,
            track = { BrushSliderTrack(hueBrush) }
        )
        Slider(
            value = hct.value.chroma.toFloat(),
            onValueChange = { onColourSelected(colour.hctSetChroma(it.toDouble())) },
            valueRange = 0F..100F,
            track = { BrushSliderTrack(chromaBrush.value) }
        )
        Image(
            BrushPainter(hueBrush),
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

}

@Composable
@Preview
fun ColourPickerPreview() {
    val colour = remember { mutableStateOf(Color.Transparent) }
    Box(Modifier.size(100.dp)) {
        Image(ColorPainter(colour.value), null)
        ColourPicker(colour = Color.Green, onColourSelected = { colour.value = it })
    }
}