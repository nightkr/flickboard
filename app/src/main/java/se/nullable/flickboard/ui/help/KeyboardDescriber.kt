package se.nullable.flickboard.ui.help

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.ShiftState
import se.nullable.flickboard.model.TextBoundary
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.MenuPageLink
import se.nullable.flickboard.ui.RenderActionVisual
import se.nullable.flickboard.ui.theme.BodyPlaceholder
import se.nullable.flickboard.ui.theme.SubTitle

@Composable
fun KeyboardDescriber(modifier: Modifier = Modifier, initialAction: Action? = null) {
    // first element is the visible action, the others are the back stack
    val selectedActionStack = remember {
        mutableStateListOf<Triple<Action, KeyM?, Gesture?>>()
            .also { list ->
                if (initialAction != null) {
                    list.add(Triple(initialAction, null, null))
                }
            }
    }
    val scrollState = rememberScrollState()
    Column(modifier) {
        Column(Modifier.weight(1F), verticalArrangement = Arrangement.Bottom) {
            Surface(
                Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    AnimatedContent(
                        Triple(
                            selectedActionStack.firstOrNull(),
                            selectedActionStack.lastOrNull(),
                            selectedActionStack.size,
                        ),
                        transitionSpec = {
                            val (_, targetBase, targetStackSize) = targetState
                            val (_, initialBase, initialStackSize) = initialState
                            when {
                                targetBase != initialBase ->
                                    // Default transition
                                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                            scaleIn(
                                                initialScale = 0.92f,
                                                animationSpec = tween(220, delayMillis = 90)
                                            ))
                                        .togetherWith(fadeOut(animationSpec = tween(90)))

                                targetStackSize >= initialStackSize ->
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()

                                else ->
                                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "action description"
                    ) { (action, _, stackSize) ->
                        ActionDescription(
                            action = action?.first,
                            key = action?.second,
                            gesture = action?.third,
                            onNavigateBack = when {
                                stackSize > 1 -> ({ selectedActionStack.removeAt(0) })
                                else -> null
                            },
                            onNavigateToAction = { action, key, gesture ->
                                selectedActionStack.add(
                                    0,
                                    Triple(action, key, gesture)
                                )
                            },
                        )
                    }
                }
            }
        }
        ConfiguredKeyboard(
            onAction = { action, key, gesture ->
                selectedActionStack.clear()
                selectedActionStack.add(Triple(action, key, gesture))
                true
            },
            allowFastActions = false,
            allowHideSettings = false,
            highlightedAction = selectedActionStack.firstOrNull()?.first,
        )
    }
}

@Composable
fun ActionDescription(
    action: Action?,
    key: KeyM?,
    gesture: Gesture?,
    onNavigateBack: (() -> Unit)?,
    onNavigateToAction: (Action, KeyM?, Gesture?) -> Unit
) {
    data class RelatedAction(
        val action: Action,
        val icon: Painter,
        val label: String,
        val iconModifier: Modifier = Modifier,
        val gesture: Gesture?,
        val sameKey: Boolean = gesture != null,
    )

    if (action != null) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                        )
                    }
                }
                RenderActionVisual(
                    action.withHidden(false),
                    enterKeyLabel = null,
                    modifiers = null,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(8.dp),
                    colour = MaterialTheme.colorScheme.onSurface,
                    activeColour = MaterialTheme.colorScheme.onSurface,
                    layoutTextDirection = TextDirection.LeftToRight,
                    allowFade = false,
                )
                SubTitle(action.title)
            }
            Text(action.description)
            if (gesture != null) {
                val flick = gesture.toFlick(
                    longHoldOnClockwiseCircle = false,
                    longHoldOnCounterClockwiseCircle = false,
                )
                val fastAction = key?.fastActions?.get(flick.direction)
                val relatedActions = mutableListOf<RelatedAction>()
                val withModifiers = mutableListOf<RelatedAction>()
                if (fastAction != null) {
                    relatedActions.add(
                        RelatedAction(
                            fastAction,
                            painterResource(R.drawable.baseline_speed_24),
                            "Fast Action",
                            gesture = flick,
                        )
                    )
                }
                val shiftAction = key?.shift?.actions?.get(flick.direction)
                if (shiftAction != null && shiftAction != action && shiftAction.showAsRelatedInHelp) {
                    withModifiers.add(
                        RelatedAction(
                            shiftAction,
                            painterResource(R.drawable.baseline_rotate_right_24),
                            "Shift",
                            gesture = flick.copy(shift = true),
                        )
                    )
                }
                if (gesture == Gesture.Tap && key?.holdAction != null) {
                    withModifiers.add(
                        RelatedAction(
                            key.holdAction,
                            painterResource(R.drawable.baseline_file_download_24),
                            "Hold",
                            gesture = flick.copy(longHold = true),
                        )
                    )
                }
                if (flick.shift) {
                    relatedActions.add(
                        RelatedAction(
                            Action.ToggleShift(ShiftState.Shift),
                            painterResource(R.drawable.baseline_arrow_drop_up_24),
                            "Reach",
                            gesture = null,
                        )
                    )
                }
                if (flick.direction == Direction.CENTER) {
                    val flickKey = when {
                        flick.shift -> key?.shift
                        else -> key
                    }
                    flickKey?.actions?.forEach { (direction, swipeAction) ->
                        if (
                            direction != Direction.CENTER &&
                            // Only show otherwise hidden icons
                            swipeAction.visual(ModifierState()) == ActionVisual.None
                        ) {
                            relatedActions.add(
                                RelatedAction(
                                    swipeAction,
                                    painterResource(R.drawable.baseline_swipe_up_alt_24),
                                    direction.title(),
                                    iconModifier = Modifier.rotate(
                                        direction.angleFromTop().toFloat()
                                    ),
                                    gesture = flick.copy(direction = direction),
                                )
                            )
                        }
                    }
                }
                @Composable
                fun RelatedActions(title: String, actions: List<RelatedAction>) {
                    if (actions.isNotEmpty()) {
                        SubTitle(title, Modifier.padding(top = 8.dp))
                        actions.forEach { relatedAction ->
                            MenuPageLink(
                                onClick = {
                                    onNavigateToAction(
                                        relatedAction.action,
                                        key.takeIf { relatedAction.sameKey },
                                        relatedAction.gesture
                                    )
                                },
                                icon = relatedAction.icon,
                                iconModifier = relatedAction.iconModifier,
                                label = "${relatedAction.label}: ${relatedAction.action.title}",
                            )
                        }
                    }
                }
                RelatedActions("With modifiers:", withModifiers)
                RelatedActions("Related gestures:", relatedActions)
            }
        }
    } else {
        BodyPlaceholder("Perform a gesture to see what it does!", Modifier.fillMaxWidth())
    }
}

@Composable
@Preview(
    showBackground = true,
)
@PreviewLightDark
private fun Preview(@PreviewParameter(PreviewActionsProvider::class) action: Action?) {
    FlickBoardParent {
        Scaffold { insets ->
            KeyboardDescriber(initialAction = action, modifier = Modifier.padding(insets))
        }
    }
}

private class PreviewActionsProvider :
    CollectionPreviewParameterProvider<Action?>(
        listOf(
            null,
            Action.Text("a"),
            Action.Delete(),
            Action.Delete(SearchDirection.Forwards, TextBoundary.Word),
            Action.Cut,
        )
    )