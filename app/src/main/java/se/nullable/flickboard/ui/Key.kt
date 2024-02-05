package se.nullable.flickboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun Key(key: KeyM, onAction: (Action) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White)
            .size(72.dp)
            .border(1.dp, Color.Black)
            .pointerInput(key) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    val up = awaitUp()
                    if (up != null) {
                        up.consume()
                        val diff = up.position - down.position
                        val direction = if (diff.getDistance() < viewConfiguration.touchSlop) {
                            Direction.CENTER
                        } else {
                            val angle = atan2(diff.y, diff.x)
                            val slice = (angle * 4 / Math.PI)
                                .roundToInt()
                                .mod(8)
                            when (slice) {
                                0 -> Direction.RIGHT
                                1 -> Direction.BOTTOM_RIGHT
                                2 -> Direction.BOTTOM
                                3 -> Direction.BOTTOM_LEFT
                                4 -> Direction.LEFT
                                5 -> Direction.TOP_LEFT
                                6 -> Direction.TOP
                                7 -> Direction.TOP_RIGHT
                                else -> null
                            }
                        }
                        key.actions[direction]?.let(onAction)
                    } else {
                        println("cancelled")
                    }
                }
            }
    ) {
        key.actions.forEach { (direction, action) ->
            Text(
                text = action.label,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .align(
                        when (direction) {
                            Direction.TOP_LEFT -> Alignment.TopStart
                            Direction.TOP -> Alignment.TopCenter
                            Direction.TOP_RIGHT -> Alignment.TopEnd
                            Direction.LEFT -> Alignment.CenterStart
                            Direction.CENTER -> Alignment.Center
                            Direction.RIGHT -> Alignment.CenterEnd
                            Direction.BOTTOM_LEFT -> Alignment.BottomStart
                            Direction.BOTTOM -> Alignment.BottomCenter
                            Direction.BOTTOM_RIGHT -> Alignment.BottomEnd
                        }
                    )
            )
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitUp(pass: PointerEventPass = PointerEventPass.Main): PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent(pass)
        val change = event.changes.findLast { it.changedToUp() }
        if (change != null) {
            return change
        }
    }
}

@Composable
@Preview
fun KeyPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    Column {
        Row {
            Text(text = "Tapped: $lastAction")
        }
        Key(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text(character = "A"),
                    Direction.TOP to Action.Text(character = "B"),
                    Direction.TOP_RIGHT to Action.Text(character = "C"),
                    Direction.LEFT to Action.Text(character = "D"),
                    Direction.CENTER to Action.Text(character = "E"),
                    Direction.RIGHT to Action.Text(character = "F"),
                    Direction.BOTTOM_LEFT to Action.Text(character = "G"),
                    Direction.BOTTOM to Action.Text(character = "H"),
                    Direction.BOTTOM_RIGHT to Action.Text(character = "I"),
                )
            ),
            onAction = { lastAction = it }
        )
    }
}