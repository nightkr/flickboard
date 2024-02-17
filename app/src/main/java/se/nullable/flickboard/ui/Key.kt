package se.nullable.flickboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import se.nullable.flickboard.averageOf
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionClass
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.sqrt
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

@Composable
fun Key(
    key: KeyM,
    onAction: ((Action) -> Unit)?,
    modifierState: ModifierState,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    keyPointerTrailListener: State<KeyPointerTrailListener?> = remember { mutableStateOf(null) },
    keyWidth: Dp = 100.dp,
) {
    val haptic = LocalHapticFeedback.current
    val settings = LocalAppSettings.current
    val actionVisualScale = settings.actionVisualScale.state
    val scale = settings.currentScale
    val keyRoundness = settings.keyRoundness.state
    val keyOpacity = settings.keyOpacity.state
    val enableFastActions = settings.enableFastActions.state
    val longHoldOnClockwiseCircle = settings.longHoldOnClockwiseCircle.state
    val swipeThreshold = settings.swipeThreshold.state
    val fastSwipeThreshold = settings.fastSwipeThreshold.state
    val circleJaggednessThreshold = settings.circleJaggednessThreshold.state
    val circleDiscontinuityThreshold = settings.circleDiscontinuityThreshold.state
    val circleAngleThreshold = settings.circleAngleThreshold.state
    val enableHapticFeedback = settings.enableHapticFeedback.state
    val onActionModifier = if (onAction != null) {
        val handleAction = { action: Action ->
            if (enableHapticFeedback.value) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onAction(action)
        }
        Modifier.pointerInput(key) {
            awaitEachGesture {
                awaitGesture(
                    swipeThreshold = { swipeThreshold.value.dp },
                    fastSwipeThreshold = { fastSwipeThreshold.value.dp },
                    longHoldOnClockwiseCircle = { longHoldOnClockwiseCircle.value },
                    circleJaggednessThreshold = { circleJaggednessThreshold.value },
                    circleDiscontinuityThreshold = { circleDiscontinuityThreshold.value },
                    circleAngleThreshold = { circleAngleThreshold.value },
                    fastActions = key.fastActions.takeIf { enableFastActions.value }
                        ?: emptyMap(),
                    onFastAction = handleAction,
                    trailListenerState = keyPointerTrailListener,
                )?.let { gesture ->
                    val action = when {
                        gesture.longHold -> key.holdAction
                        else -> {
                            when {
                                gesture.shift -> key.shift
                                else -> key
                            }?.actions?.get(gesture.direction)
                        }
                    }
                    action?.let(handleAction)
                }
            }
        }
    } else {
        // No action handler defined => disable input
        Modifier
    }
    BoxWithConstraints(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = keyOpacity.value),
                shape = RoundedCornerShape((keyRoundness.value * 100).roundToInt())
            )
            .height(keyWidth)
            .then(onActionModifier)
    ) {
        key.actions.forEach { (direction, action) ->
            KeyActionIndicator(
                direction,
                action,
                enterKeyLabel = enterKeyLabel,
                cornerRadius = sqrt(max(maxWidth, maxHeight) * keyRoundness.value),
                scale = scale * actionVisualScale.value,
                modifiers = modifierState,
            )
        }
    }
}

@Composable
fun BoxScope.KeyActionIndicator(
    direction: Direction,
    action: Action,
    enterKeyLabel: String?,
    cornerRadius: Dp,
    scale: Float,
    modifiers: ModifierState,
) {
    var keyModifier = Modifier
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
    if (direction.isCorner()) {
        keyModifier = keyModifier.padding(all = cornerRadius)
    }
    val overrideActionVisual =
        enterKeyLabel.takeIf { action is Action.Enter }?.let { ActionVisual.Label(it) }
    val settings = LocalAppSettings.current
    val showAction = when (action.actionClass) {
        ActionClass.Letter -> settings.showLetters.state.value
        ActionClass.Symbol -> settings.showSymbols.state.value
        ActionClass.Number -> settings.showNumbers.state.value
        else -> true
    }
    val calcFontSize = if (direction == Direction.CENTER) {
        18.sp * scale
    } else {
        10.sp * scale
    }
    val calcIconSize = if (direction == Direction.CENTER) {
        24.dp * scale
    } else {
        18.dp * scale
    }
    val calcFontWeight = if (direction == Direction.CENTER) {
        FontWeight.ExtraBold
    } else {
        FontWeight.Normal
    }
    if (showAction) {
        val color = when (action.actionClass) {
            ActionClass.Symbol -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4F)
            else -> MaterialTheme.colorScheme.primary
        }
        when (val actionVisual = overrideActionVisual ?: action.visual(modifiers)) {
            is ActionVisual.Label -> {
                Text(
                    text = actionVisual.label,
                    color = color,
                    modifier = keyModifier.padding(all = 2.dp),
                    fontSize = calcFontSize,
                    fontWeight = calcFontWeight,
                    lineHeight = calcFontSize
                )
            }

            is ActionVisual.Icon -> Icon(
                painter = painterResource(id = actionVisual.resource),
                contentDescription = null,
                tint = color,
                modifier = keyModifier
                    .size(calcIconSize)
                    .padding(all = 4.dp)
            )

            ActionVisual.None -> {}
        }
    }
}

data class KeyPointerTrailListener(
    val onDown: () -> Unit = {},
    val onUp: () -> Unit = {},
    // Called on every pointer event in a gesture
    // The list will be MUTATED after this, and should not be considered stable
    // For example, if storing it in a MutableState, make sure to use neverEqualPolicy()
    val onTrailUpdate: (List<Offset>) -> Unit = {}
)

private suspend inline fun AwaitPointerEventScope.awaitGesture(
    swipeThreshold: () -> Dp,
    fastSwipeThreshold: () -> Dp,
    longHoldOnClockwiseCircle: () -> Boolean,
    circleJaggednessThreshold: () -> Float,
    circleDiscontinuityThreshold: () -> Float,
    circleAngleThreshold: () -> Float,
    fastActions: Map<Direction, Action>,
    onFastAction: (Action) -> Unit,
    // Passed as state to ensure that it's only grabbed once we have a down event
    trailListenerState: State<KeyPointerTrailListener?>,
): Gesture? {
    val down = awaitFirstDown()
    down.consume()
    val trailListener = trailListenerState.value
    trailListener?.onDown?.invoke()
    var isDragging = false
    var canBeFastAction = fastActions.isNotEmpty()
    var fastActionPerformed = false
    val positions = mutableListOf<Offset>()
    var mostExtremePosFromDown = Offset(0F, 0F)
    var mostExtremeDistanceFromDownSquared = 0F
    var fastActionTraveled = Offset(0F, 0F)
    // cache squared slops to avoid having to take square roots
    val swipeSlopSquared = swipeThreshold().toPx().pow(2)
    val fastSwipeSlopSquared = fastSwipeThreshold().toPx().pow(2)
    while (true) {
        val event =
            withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { awaitPointerEvent() }
        if (event == null && !isDragging) {
            trailListener?.onUp?.invoke()
            return Gesture(Direction.CENTER, longHold = true, shift = false)
        }
        for (change in event?.changes ?: emptyList()) {
            if (change.isConsumed || change.changedToUp()) {
                trailListener?.onUp?.invoke()
            }
            if (change.isConsumed) {
                return null
            }
            positions.add(change.position)
            trailListener?.onTrailUpdate?.invoke(positions)
            val posFromDown = change.position - down.position
            val distanceFromDownSquared = posFromDown.getDistanceSquared()
            if (!isDragging) {
                if (distanceFromDownSquared > swipeSlopSquared) {
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
                var isLongHold = false
                val direction = if (!isDragging) {
                    return Gesture(direction = Direction.CENTER, longHold = false, shift = false)
                } else {
                    val circleDirection = shapeLooksLikeCircle(
                        positions,
                        jaggednessThreshold = circleJaggednessThreshold(),
                        discontinuityThreshold = circleDiscontinuityThreshold(),
                        angleThreshold = circleAngleThreshold(),
                    )
                    if (circleDirection != null) {
                        if (circleDirection == CircleDirection.Clockwise && longHoldOnClockwiseCircle()) {
                            isLongHold = true
                        } else {
                            isRound = true
                        }
                        Direction.CENTER
                    } else {
                        mostExtremePosFromDown.direction()
                    }
                }
                return Gesture(
                    direction = direction,
                    longHold = isLongHold,
                    // shift if swipe is more than halfway to returned from the starting position (U shape),
                    // or a circle
                    shift = isRound ||
                            (posFromDown - mostExtremePosFromDown).getDistanceSquared() > mostExtremeDistanceFromDownSquared / 4,
                )
            } else if (canBeFastAction) {
                val posChange = change.positionChange()
                fastActionTraveled += posChange
                val fastActionCountSquared =
                    fastActionTraveled.getDistanceSquared() / fastSwipeSlopSquared
                if (fastActionCountSquared >= 1) {
                    val fastActionCount = sqrt(fastActionCountSquared)
                    val direction = posChange.direction()
                    val fastAction = fastActions[direction]
                    if (fastAction != null) {
                        fastActionPerformed = true
                        fastActionTraveled -= fastActionTraveled / fastActionCount
                        onFastAction(fastAction)
                    } else {
                        canBeFastAction = fastActionPerformed
                    }
                }
            }
        }
    }
}

private fun Offset.angle(): Float = atan2(y, x)

private fun Offset.direction(): Direction {
    val slice = (angle() * 4 / Math.PI)
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
        else -> throw RuntimeException("Offset has invalid direction slice=$slice (angle=${angle()}")
    }
}

enum class CircleDirection {
    Clockwise,
    CounterClockwise,
}

private fun shapeLooksLikeCircle(
    points: List<Offset>,
    jaggednessThreshold: Float,
    discontinuityThreshold: Float,
    angleThreshold: Float,
): CircleDirection? {
    if (points.size < 5) {
        // Not enough data for there to be any "jaggedness" to detect
        return null
    }
    val midPoint = Offset(
        x = points.averageOf { it.x },
        y = points.averageOf { it.y },
    )
    val radiuses = points.map { (it - midPoint).getDistanceSquared() }

    val averageRadius = radiuses.averageOf { it }
    // If the radius is too small -> too hard to tell
    if (averageRadius < 10) {
        return null
    }

    // If the shape is too jagged -> not a circle
    val jaggedness = radiuses.averageOf { (it - averageRadius).pow(2) } / averageRadius.pow(2)
    if (jaggedness > jaggednessThreshold) {
        return null
    }

    val angles = points
        .map { (it - midPoint).angle() }
        .zipWithNext { a, b -> (PI + (b - a)).mod(2 * PI) - PI }

    // If the angle is too small -> not a complete circle
    val totalAngle = angles.sum()
    if (totalAngle.absoluteValue < angleThreshold) {
        println("angle too small")
        return null
    }

    // If the rotation flips direction -> round? but not a circle stroke
    val sign = totalAngle.sign
    val epsilon = -0.05
    if (angles.any { it * sign < epsilon }) {
        println("flipped!")
        return null
    }

    // If there is too much of a gap in the angle -> probably not a circle
    if (angles.any { it.absoluteValue > discontinuityThreshold }) {
        println("discontinuity!")
        return null
    }

    return when {
        totalAngle > 0 -> CircleDirection.Clockwise
        else -> CircleDirection.CounterClockwise
    }
}

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
                    modifierState = ModifierState()
                )
            }
        }
    }
}