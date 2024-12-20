package se.nullable.flickboard.ui

import android.gesture.GestureLibrary
import android.gesture.GesturePoint
import android.gesture.GestureStroke
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import se.nullable.flickboard.angle
import se.nullable.flickboard.averageOf
import se.nullable.flickboard.direction
import se.nullable.flickboard.div
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionClass
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.CircleDirection
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.FastActionType
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.times
import se.nullable.flickboard.ui.layout.KeyLabelGrid
import se.nullable.flickboard.ui.theme.LocalKeyboardTheme
import se.nullable.flickboard.ui.util.LocalDollar1GestureLibrary
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Key(
    key: KeyM,
    onAction: OnAction?,
    modifierState: ModifierState?,
    modifier: Modifier = Modifier,
    enterKeyLabel: String? = null,
    keyPointerTrailListener: State<KeyPointerTrailListener?> = remember { mutableStateOf(null) },
    layoutTextDirection: TextDirection,
    allowFastActions: Boolean = true,
    highlightedAction: Action? = null,
) {
    if (!key.rendered) {
        Box(modifier)
        return
    }

    val view = LocalView.current
//    val haptic = LocalHapticFeedback.current

    val settings = LocalAppSettings.current
    val actionVisualScale = settings.actionVisualScale.state
    val scale = settings.currentScale
    val keyHeight = settings.keyHeight.state.value * scale
    val keyRoundness = settings.keyRoundness.state
    val keyOutline = settings.keyOutline.state
    val keyOpacity = settings.keyOpacity.state
    val enableFastActions = settings.enableFastActions.state
    val longHoldOnClockwiseCircle = settings.longHoldOnClockwiseCircle.state
    val longHoldOnCounterClockwiseCircle = settings.longHoldOnCounterClockwiseCircle.state
    val swipeThreshold = settings.swipeThreshold.state
    val fastSwipeThreshold = settings.fastSwipeThreshold.state
    val enableLongSwipes = settings.enableLongSwipes.state
    val longSwipeThreshold = settings.longSwipeThreshold.state
    val gestureRecognizer = settings.gestureRecognizer.state
    val circleJaggednessThreshold = settings.circleJaggednessThreshold.state
    val circleDiscontinuityThreshold = settings.circleDiscontinuityThreshold.state
    val circleAngleThreshold = settings.circleAngleThreshold.state
    val enableHapticFeedbackOnGestureStart = settings.enableHapticFeedbackOnGestureStart.state
    val enableHapticFeedbackOnGestureSuccess = settings.enableHapticFeedbackOnGestureSuccess.state
    val enableVisualFeedback = settings.enableVisualFeedback.state
    val dropLastGesturePoint = settings.dropLastGesturePoint.state
    val ignoreJumpsLongerThanPx = settings.ignoreJumpsLongerThanPx.state
    val flicksMustBeLongerThanSeconds = settings.flicksMustBeLongerThanSeconds.state
    val keyboardTheme = LocalKeyboardTheme.current
    var lastActionTaken: TakenAction? by remember { mutableStateOf(null, neverEqualPolicy()) }
    var lastActionIsVisible by remember { mutableStateOf(false) }
    val lastActionAlpha = animateFloatAsState(1F * lastActionIsVisible, label = "lastActionAlpha") {
        if (!lastActionIsVisible) {
            lastActionTaken = null
        }
    }
    LaunchedEffect(lastActionTaken) {
        lastActionIsVisible = lastActionTaken != null
        if (lastActionTaken != null) {
            delay(300.milliseconds)
            lastActionIsVisible = false
        }
    }
    val gestureLibrary = LocalDollar1GestureLibrary.current?.value
    val onActionModifier = if (onAction != null) {
        fun onGestureStart() {
            if (enableHapticFeedbackOnGestureStart.value) {
                // This gesture type is not supported by Compose 1.7.x, see #254
                // Using Views gestures as a workaround
                view.performHapticFeedback(
                    when {
                        android.os.Build.VERSION.SDK_INT >= 30 -> HapticFeedbackConstants.GESTURE_START
                        else -> HapticFeedbackConstants.KEYBOARD_TAP
                    },
                )
            }
        }

        fun handleAction(action: Action, gesture: Gesture?, isFast: Boolean): Boolean {
            if (enableHapticFeedbackOnGestureSuccess.value || (isFast && enableHapticFeedbackOnGestureStart.value)) {
                // This gesture type is not supported by Compose 1.7.x, see #254
                // Using Views gestures as a workaround
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            if (enableVisualFeedback.value) {
                if (!action.isHiddenAction) {
                    lastActionTaken = TakenAction(action)
                }
            }
            return onAction.onAction(action, key, gesture)
        }
        Modifier.pointerInput(key) {
            awaitEachGesture {
                awaitGesture(
                    swipeThreshold = { swipeThreshold.value.dp },
                    fastSwipeThreshold = { fastSwipeThreshold.value.dp },
                    longSwipeThreshold = {
                        when {
                            enableLongSwipes.value -> longSwipeThreshold.value
                            else -> Float.POSITIVE_INFINITY
                        }
                    },
                    circleJaggednessThreshold = { circleJaggednessThreshold.value },
                    circleDiscontinuityThreshold = { circleDiscontinuityThreshold.value },
                    circleAngleThreshold = { circleAngleThreshold.value },
                    gestureRecognizer = { gestureRecognizer.value },
                    fastActions = key.fastActions.takeIf { enableFastActions.value && allowFastActions }
                        ?: emptyMap(),
                    onGestureStart = ::onGestureStart,
                    onFastAction = { action ->
                        handleAction(
                            action,
                            gesture = null,
                            isFast = true,
                        )
                    },
                    trailListenerState = keyPointerTrailListener,
                    gestureLibrary = { gestureLibrary },
                    dropLastGesturePoint = { dropLastGesturePoint.value },
                    ignoreJumpsLongerThanPx = { ignoreJumpsLongerThanPx.value },
                    flicksMustBeLongerThanSeconds = { flicksMustBeLongerThanSeconds.value },
                )?.let { gesture ->
                    val flick =
                        gesture.toFlick(
                            longHoldOnClockwiseCircle = key.holdAction != null && longHoldOnClockwiseCircle.value,
                            longHoldOnCounterClockwiseCircle = key.holdAction != null && longHoldOnCounterClockwiseCircle.value,
                        )
                    flick.resolveAction(key)?.let { handleAction(it, gesture, isFast = false) }
                }
            }
        }
    } else {
        // No action handler defined => disable input
        Modifier
    }
    val shape = RoundedCornerShape((keyRoundness.value * 100).roundToInt())
    val outlineModifier = when {
        keyOutline.value > 0.05F -> Modifier.border(
            keyOutline.value.dp,
            keyboardTheme.keyIndicatorColour,
            shape = shape,
        )

        else -> Modifier
    }
    Box(
        modifier
            .background(
                keyboardTheme.keySurfaceColour.copy(alpha = keyOpacity.value),
                shape = shape,
            )
            .height(keyHeight.dp)
            .then(outlineModifier)
            .then(onActionModifier),
    ) {
        KeyLabelGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            cornerRoundness = keyRoundness.value,
        ) {
            key.actions.forEach { (direction, action) ->
                key(direction) {
                    var actionModifier = Modifier
                        .direction(direction)
                        .scale(actionVisualScale.value)
                    actionModifier = when {
                        action.isModifier -> actionModifier.unrestrictedWidth()
                        else -> actionModifier
                    }
                    RenderActionVisual(
                        action,
                        enterKeyLabel = enterKeyLabel,
                        modifiers = modifierState,
                        colour = keyboardTheme.keyIndicatorColour,
                        activeColour = keyboardTheme.activeKeyIndicatorColour,
                        style = when {
                            action == highlightedAction -> ActionStyle.Normal
                            // Dim all non-highlighted actions if one is active
                            highlightedAction != null -> ActionStyle.Dim
                            else -> ActionStyle.default(action, modifierState)
                        },
                        layoutTextDirection = layoutTextDirection,
                        modifier = actionModifier,
                    )
                }
            }
        }
        lastActionTaken?.let {
            KeyActionTakenIndicator(
                action = it.action,
                enterKeyLabel = enterKeyLabel,
                shape = shape,
                colour = keyboardTheme.lastActionColour,
                surfaceColour = keyboardTheme.lastActionSurfaceColour,
                layoutTextDirection = layoutTextDirection,
                modifier = Modifier.alpha(lastActionAlpha.value),
            )
        }
    }
}

// NOT a data class, so that we get a new identity every time
class TakenAction(val action: Action)

@Composable
fun KeyActionTakenIndicator(
    action: Action,
    enterKeyLabel: String?,
    shape: Shape,
    colour: Color,
    surfaceColour: Color,
    layoutTextDirection: TextDirection,
    modifier: Modifier = Modifier
) {
    Surface(color = surfaceColour, shape = shape, modifier = modifier) {
        Box(Modifier.fillMaxSize()) {
            RenderActionVisual(
                action = action.withHidden(false),
                enterKeyLabel = enterKeyLabel,
                modifiers = null,
                modifier = Modifier.align(Alignment.Center),
                colour = colour,
                activeColour = colour,
                style = ActionStyle.Normal,
                layoutTextDirection = layoutTextDirection,
            )
        }
    }
}

enum class ActionStyle {
    Normal, Active, Dim;

    companion object {
        fun default(action: Action, modifiers: ModifierState?): ActionStyle = when {
            action.actionClass == ActionClass.Symbol -> Dim
            action.isActive(modifiers) -> Active
            else -> Normal
        }
    }
}

@Composable
fun RenderActionVisual(
    action: Action,
    enterKeyLabel: String?,
    modifiers: ModifierState?,
    colour: Color,
    activeColour: Color,
    layoutTextDirection: TextDirection,
    modifier: Modifier = Modifier,
    style: ActionStyle = ActionStyle.default(action, modifiers),
) {
    val overrideActionVisual =
        enterKeyLabel.takeIf { action is Action.Enter }?.let { ActionVisual.Label(it) }
    val usedColour = animateColorAsState(
        when (style) {
            ActionStyle.Normal -> colour
            ActionStyle.Dim -> colour.copy(alpha = 0.4F)
            ActionStyle.Active -> activeColour
        },
        label = "key colour",
    )
    when (val actionVisual = overrideActionVisual ?: action.visual(modifiers)) {
        is ActionVisual.Label -> {
            BoxWithConstraints(modifier.padding(horizontal = 2.dp)) {
                val density = LocalDensity.current
                Text(
                    text = actionVisual.label,
                    color = usedColour.value,
                    fontSize = with(density) {
                        min(
                            maxWidth,
                            // Make room for descenders
                            maxHeight * 0.8F,
                        ).toSp()
                    },
                    style = LocalTextStyle.current.merge(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                        textDirection = when (actionVisual.directionOverride) {
                            TextDirection.LeftToRight -> androidx.compose.ui.text.style.TextDirection.Ltr
                            TextDirection.RightToLeft -> androidx.compose.ui.text.style.TextDirection.Rtl
                            else -> when (layoutTextDirection) {
                                TextDirection.LeftToRight -> androidx.compose.ui.text.style.TextDirection.Ltr
                                TextDirection.RightToLeft ->
                                    when {
                                        LocalAppSettings.current.noReverseRtlBrackets.state.value ->
                                            androidx.compose.ui.text.style.TextDirection.Content

                                        else ->
                                            androidx.compose.ui.text.style.TextDirection.Rtl
                                    }
                            }
                        },
                    ),
                )
            }
        }

        is ActionVisual.Icon -> Icon(
            painter = painterResource(id = actionVisual.resource),
            contentDescription = null,
            tint = usedColour.value,
            modifier = modifier,
        )

        ActionVisual.None -> {}
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
    longSwipeThreshold: () -> Float,
    circleJaggednessThreshold: () -> Float,
    circleDiscontinuityThreshold: () -> Float,
    circleAngleThreshold: () -> Float,
    gestureRecognizer: () -> GestureRecognizer,
    fastActions: Map<Direction, Action>,
    onGestureStart: () -> Unit,
    onFastAction: (Action) -> Boolean,
    // Passed as state to ensure that it's only grabbed once we have a down event
    trailListenerState: State<KeyPointerTrailListener?>,
    gestureLibrary: () -> GestureLibrary?,
    dropLastGesturePoint: () -> Boolean,
    ignoreJumpsLongerThanPx: () -> Float,
    flicksMustBeLongerThanSeconds: () -> Float,
): Gesture? {
    val down = awaitFirstDown()
    down.consume()
    onGestureStart()
    val gestureStartedAtNanos = System.nanoTime()
    val trailListener = trailListenerState.value
    trailListener?.onDown?.invoke()
    var isDragging = false
    var canBeFastAction = fastActions.isNotEmpty()
    var fastActionBegan = false
    var fastActionPerformed = false
    // Must be separate from fastActionPerformed, since not all fast actions have an associated type
    var fastActionType: FastActionType? = null
    val positions = mutableListOf<Offset>()
    var mostExtremePosFromDown = Offset(0F, 0F)
    var mostExtremeDistanceFromDownSquared = 0F
    var fastActionTraveled = Offset(0F, 0F)
    // cache squared slops to avoid having to take square roots
    val swipeSlopSquared = swipeThreshold().toPx().pow(2)
    val fastSwipeSlopSquared = fastSwipeThreshold().toPx().pow(2)
    val longSwipeSlopSquared = longSwipeThreshold().pow(2)
    while (true) {
        val event: PointerEvent =
            when {
                isDragging -> awaitPointerEvent()
                else -> {
                    // initial check: wait for touch slop (indicating a drag),
                    // release (indicating a tap), or timeout (indicating a long hold)
                    val event = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                // Add pre-slop positions to trail, too
                                positions.add(change.position)
                                trailListener?.onTrailUpdate?.invoke(positions)

                                when {
                                    change.changedToUp() -> return@withTimeoutOrNull event

                                    (change.position - down.position).getDistanceSquared()
                                            > swipeSlopSquared -> {
                                        isDragging = true
                                        return@withTimeoutOrNull event
                                    }
                                }
                            }
                        }
                        // unreachable, but Kotlin typechecking doesn't recognize that the loop
                        // either diverges or returns
                        @Suppress("UNREACHABLE_CODE")
                        null
                    }
                    when (event) {
                        // withTimeoutOrNull returns null on timeout, indicating a long hold
                        null -> {
                            trailListener?.onUp?.invoke()
                            return Gesture.Flick(
                                Direction.CENTER,
                                longHold = true,
                                longSwipe = false,
                                shift = false,
                            )
                        }

                        else -> event
                    }
                }
            }
        for (changeVal in event.changes) {
            // HACK: kotlin doesn't support for (var ... in ...)
            var change = changeVal
            if (change.isConsumed || change.changedToUp()) {
                trailListener?.onUp?.invoke()
            }
            if (change.isConsumed) {
                return null
            }
            var ignoreCurrentChangePosition = false
            if (change.changedToDown()
                || change.positionChange().getDistance() > ignoreJumpsLongerThanPx()
            ) {
                ignoreCurrentChangePosition = true
            }
            if (dropLastGesturePoint() && change.changedToUp()) {
                ignoreCurrentChangePosition = true
            }
            if (ignoreCurrentChangePosition) {
                change = change.copy(currentPosition = positions.lastOrNull() ?: change.position)
            } else {
                positions.add(change.position)
                trailListener?.onTrailUpdate?.invoke(positions)
            }
            val posFromDown = change.position - down.position
            val distanceFromDownSquared = posFromDown.getDistanceSquared()
            if (distanceFromDownSquared > mostExtremeDistanceFromDownSquared) {
                mostExtremePosFromDown = posFromDown
                mostExtremeDistanceFromDownSquared = distanceFromDownSquared
            }
            if (change.changedToUpIgnoreConsumed()) {
                change.consume()
                // If fast action was performed then the user is presumably already happy with the
                // state when they release, so suppress the release action.
                if (fastActionPerformed) {
                    fastActionType
                        ?.let(Action::FastActionDone)
                        ?.let(onFastAction)
                    return null
                }
                if (!isDragging ||
                    System.nanoTime() - gestureStartedAtNanos <
                    flicksMustBeLongerThanSeconds() * 10.toDouble().pow(9)
                ) {
                    return Gesture.Tap
                }
                when (gestureRecognizer()) {
                    GestureRecognizer.Dollar1 -> {
                        val gesture = android.gesture.Gesture()
                            .also { g ->
                                g.addStroke(
                                    GestureStroke(
                                        positions.mapIndexedTo(ArrayList()) { i, pos ->
                                            GesturePoint(pos.x, pos.y, i.toLong())
                                        },
                                    ),
                                )
                            }
                        val predictions = gestureLibrary()?.recognize(gesture)
                        return Gesture.names[predictions?.firstOrNull()?.name]
                    }

                    GestureRecognizer.Default -> {
                        val circleDirection: CircleDirection? = shapeLooksLikeCircle(
                            positions,
                            jaggednessThreshold = circleJaggednessThreshold(),
                            discontinuityThreshold = circleDiscontinuityThreshold(),
                            angleThreshold = circleAngleThreshold(),
                        )
                        val direction = when {
                            circleDirection != null -> Direction.CENTER
                            else -> mostExtremePosFromDown.direction()
                        }

                        return when {
                            circleDirection != null -> Gesture.Circle(circleDirection)
                            else -> {
                                Gesture.Flick(
                                    direction = direction,
                                    longHold = false,
                                    longSwipe = (posFromDown / size).getDistanceSquared() > longSwipeSlopSquared,
                                    // shift if swipe is more than halfway to returned from the starting position (U shape)
                                    shift = (posFromDown - mostExtremePosFromDown).getDistanceSquared() > mostExtremeDistanceFromDownSquared / 4,
                                )
                            }
                        }
                    }
                }
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
                        if (!fastActionBegan) {
                            onFastAction(Action.BeginFastAction)
                            fastActionBegan = true
                        }
                        if (onFastAction(fastAction)) {
                            fastActionPerformed = true
                            fastActionType = fastAction.fastActionType
                            fastActionTraveled -= fastActionTraveled / fastActionCount
                        }
                    } else {
                        canBeFastAction = fastActionPerformed
                    }
                }
            }
        }
    }
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
                        ),
                    ),
                    onAction = { action, _, _ ->
                        lastAction = action
                        true
                    },
                    layoutTextDirection = TextDirection.LeftToRight,
                    modifier = Modifier.aspectRatio(1F),
                    modifierState = ModifierState(),
                )
            }
        }
    }
}

@Composable
@Preview
fun KeyActionTakenPreview() {
    FlickBoardParent {
        Column {
            Box(Modifier.size(100.dp)) {
                KeyActionTakenIndicator(
                    action = Action.Text("A"),
                    enterKeyLabel = null,
                    colour = MaterialTheme.colorScheme.onTertiary,
                    surfaceColour = MaterialTheme.colorScheme.tertiary,
                    shape = RectangleShape,
                    layoutTextDirection = TextDirection.LeftToRight,
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Box(Modifier.size(100.dp)) {
                KeyActionTakenIndicator(
                    action = Action.Text("{"),
                    enterKeyLabel = null,
                    colour = MaterialTheme.colorScheme.onTertiary,
                    surfaceColour = MaterialTheme.colorScheme.tertiary,
                    shape = RectangleShape,
                    layoutTextDirection = TextDirection.LeftToRight,
                )
            }
        }
    }
}
