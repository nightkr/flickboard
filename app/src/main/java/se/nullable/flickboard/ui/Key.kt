package se.nullable.flickboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun Key(key: KeyM, onAction: (Action) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .background(Color.White)
            .aspectRatio(key.colspan.toFloat())
            .border(0.dp, Color.Black)
            .pointerInput(key) {
                awaitEachGesture {
                    awaitGesture()?.let { gesture ->
//                        println(gesture)
                        var appliedKey: KeyM? = key
                        if (gesture.forceFallback) {
                            appliedKey = appliedKey?.fallback
                        }
                        if (gesture.shift) {
                            appliedKey = appliedKey?.shift
                        }
                        appliedKey?.actions
                            ?.get(gesture.direction)
                            ?.let {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(it)}
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

private suspend fun AwaitPointerEventScope.awaitGesture(): Gesture? {
    val down = awaitFirstDown()
    down.consume()
    var isDragging = false
    val positions = mutableListOf<Offset>()
    var mostExtremePosFromDown = Offset(0F, 0F)
    while (true) {
        val event =
            withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { awaitPointerEvent() }
        if (event == null && !isDragging) {
            return Gesture(Direction.CENTER, forceFallback = true, shift = false)
        }
        for (change in event?.changes ?: emptyList()) {
            if (change.isConsumed) {
                return null
            }
            positions.add(change.position)
            val posFromDown = change.position - down.position
            if (!isDragging) {
                if (posFromDown.getDistance() > viewConfiguration.touchSlop) {
                    isDragging = true
                }
            }
            if (isDragging) {
                change.consume()
            }
            if (posFromDown.getDistanceSquared() > mostExtremePosFromDown.getDistanceSquared()) {
                mostExtremePosFromDown = posFromDown
            }
            if (change.changedToUpIgnoreConsumed()) {
                change.position - down.position
                var isRound = false
                val direction = if (!isDragging) {
                    Direction.CENTER
                } else if (shapeLooksRound(positions)) {
                    isRound = true
                    Direction.CENTER
                } else {
                    val angle = atan2(mostExtremePosFromDown.y, mostExtremePosFromDown.x)
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
                        else -> return null
                    }
                }
                change.consume()
                return Gesture(
                    direction = direction,
                    forceFallback = false,
                    // shift if swipe is more than halfway to returned from the starting position (U shape)
                    shift = // posFromDown.getDistance() / mostExtremePosFromDown.getDistance() < 0.5 ||
                    isRound || (posFromDown - mostExtremePosFromDown).getDistanceSquared() > mostExtremePosFromDown.getDistanceSquared() / 4,
                )
            }
        }
    }
}

private fun shapeLooksRound(points: List<Offset>): Boolean {
    val midPoint = Offset(
        x = points.averageOf { it.x },
        y = points.averageOf { it.y },
    )
    val radiuses = points.map { (it - midPoint).getDistanceSquared() }
    val averageRadius = radiuses.averageOf { it }
    val jaggedness = radiuses.averageOf { (it - averageRadius).absoluteValue } / averageRadius
    return jaggedness < 0.5
}

private inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()

@Composable
@Preview
fun KeyPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    Column {
        Text(text = "Tapped: $lastAction")
        Row(Modifier.width(100.dp)) {
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
}