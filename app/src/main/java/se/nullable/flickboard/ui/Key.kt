package se.nullable.flickboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionClass
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun Key(
    key: KeyM,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val settings = LocalAppSettings.current
    val cellHeight = settings.cellHeight.state.value
    val showLetters = settings.showLetters.state.value
    val showSymbols = settings.showSymbols.state.value
    val showNumbers = settings.showNumbers.state.value
    val enableFastActions = settings.enableFastActions.state.value
    val fastActions = key.fastActions.takeIf { enableFastActions } ?: mapOf()
    val handleAction = { action: Action ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAction(action)
    }
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
//            .aspectRatio(key.colspan.toFloat())
            .height(cellHeight.dp)
            .border(0.dp, MaterialTheme.colorScheme.surface)
            .pointerInput(key) {
                awaitEachGesture {
                    awaitGesture(
                        fastActions = fastActions,
                        onFastAction = handleAction
                    )?.let { gesture ->
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
                            ?.let(handleAction)
                    }
                }
            }
    ) {
        key.actions.forEach { (direction, action) ->
            val keyModifier = Modifier
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
            val overrideActionVisual =
                enterKeyLabel.takeIf { action is Action.Enter }?.let { ActionVisual.Label(it) }
            val showAction = when (action.actionClass) {
                ActionClass.Letter -> showLetters
                ActionClass.Symbol -> showSymbols
                ActionClass.Number -> showNumbers
                else -> true
            }
            if (showAction) {
                when (val actionVisual = overrideActionVisual ?: action.visual) {
                    is ActionVisual.Label -> Text(
                        text = actionVisual.label,
                        color = when (action.actionClass) {
                            ActionClass.Symbol -> Color.Gray
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = keyModifier.padding(horizontal = 2.dp)
                    )

                    is ActionVisual.Icon -> Icon(
                        painter = painterResource(id = actionVisual.resource),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = keyModifier
                            .size(24.dp)
                            .padding(all = 4.dp)
                    )

                    ActionVisual.None -> {}
                }
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitGesture(
    fastActions: Map<Direction, Action>,
    onFastAction: (Action) -> Unit
): Gesture? {
    val down = awaitFirstDown()
    down.consume()
    var isDragging = false
    var fastActionPerformed = false
    val positions = mutableListOf<Offset>()
    var mostExtremePosFromDown = Offset(0F, 0F)
    var mostExtremeDistanceFromDownSquared = 0F
    var fastActionTraveled = Offset(0F, 0F)
    // touchSlop is calibrated to distinguish between a tap and a drag, but
    // ends up still being too short to comfortably distinguish between individual "ticks"
    val fastActionSlop = viewConfiguration.touchSlop * 2
    // cache squared slops to avoid having to take square roots
    val touchSlopSquared = viewConfiguration.touchSlop.pow(2)
    val fastActionSlopSquared = fastActionSlop.pow(2)
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
            val distanceFromDownSquared = posFromDown.getDistanceSquared()
            if (!isDragging) {
                if (distanceFromDownSquared > touchSlopSquared) {
                    isDragging = true
                }
            }
            if (distanceFromDownSquared > mostExtremeDistanceFromDownSquared) {
                mostExtremePosFromDown = posFromDown
                mostExtremeDistanceFromDownSquared = distanceFromDownSquared
            }
            if (change.changedToUpIgnoreConsumed()) {
                change.consume()
                // If fast action was performed then the user is presumably already happy with the
                // state when they release, so suppress the release action.
                if (fastActionPerformed) {
                    return null
                }
                change.position - down.position
                var isRound = false
                val direction = if (!isDragging) {
                    Direction.CENTER
                } else if (shapeLooksRound(positions)) {
                    isRound = true
                    Direction.CENTER
                } else {
                    mostExtremePosFromDown.direction()
                }
                return Gesture(
                    direction = direction,
                    forceFallback = false,
                    // shift if swipe is more than halfway to returned from the starting position (U shape),
                    // or a circle
                    shift = isRound ||
                            (posFromDown - mostExtremePosFromDown).getDistanceSquared() > mostExtremeDistanceFromDownSquared / 4,
                )
            } else if (fastActions.isNotEmpty()) {
                val posChange = change.positionChange()
                fastActionTraveled += posChange
                val fastActionCountSquared =
                    fastActionTraveled.getDistanceSquared() / fastActionSlopSquared
                if (fastActionCountSquared >= 1) {
                    val fastActionCount = sqrt(fastActionCountSquared)
                    val direction = posChange.direction()
                    fastActions[direction]?.let {
                        fastActionPerformed = true
                        fastActionTraveled -= fastActionTraveled / fastActionCount
                        onFastAction(it)
                    }
                }
            }
        }
    }
}

private fun Offset.direction(): Direction {
    val angle = atan2(y, x)
    val slice = (angle * 4 / Math.PI)
        .roundToInt()
        .mod(8)
    return when (slice) {
        0 -> Direction.RIGHT
        1 -> Direction.BOTTOM_RIGHT
        2 -> Direction.BOTTOM
        3 -> Direction.BOTTOM_LEFT
        4 -> Direction.LEFT
        5 -> Direction.TOP_LEFT
        6 -> Direction.TOP
        7 -> Direction.TOP_RIGHT
        else -> throw RuntimeException("Offset has invalid direction slice=$slice (angle=$angle)")
    }
}

private fun shapeLooksRound(points: List<Offset>): Boolean {
    val midPoint = Offset(
        x = points.averageOf { it.x },
        y = points.averageOf { it.y },
    )
    val radiuses = points.map { (it - midPoint).getDistanceSquared() }
    val averageRadius = radiuses.averageOf { it }
    if (averageRadius < 10) {
        return false
    }
    val jaggedness = radiuses.averageOf { (it - averageRadius).absoluteValue } / averageRadius
    return jaggedness < 0.5
}

private inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()

@Composable
@Preview
fun KeyPreview() {
    var lastAction by remember { mutableStateOf<Action?>(null) }
    FlickBoardParent {
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
                    onAction = { lastAction = it },
                    modifier = Modifier.aspectRatio(1F),
                )
            }
        }
    }
}