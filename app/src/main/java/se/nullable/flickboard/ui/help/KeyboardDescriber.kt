package se.nullable.flickboard.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import se.nullable.flickboard.model.SearchDirection
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
        Spacer(Modifier.weight(1F))
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
                val action = selectedActionStack.firstOrNull()
                ActionDescription(
                    action = action?.first,
                    key = action?.second,
                    gesture = action?.third,
                    onNavigateBack = when {
                        selectedActionStack.size > 1 -> ({ selectedActionStack.removeAt(0) })
                        else -> null
                    },
                    onNavigateToAction = { selectedActionStack.add(0, Triple(it, null, null)) },
                )
            }
        }
        ConfiguredKeyboard(
            onAction = { action, key, gesture ->
                selectedActionStack.clear()
                selectedActionStack.add(Triple(action, key, gesture))
                true
            },
            allowFastActions = false,
        )
    }
}

@Composable
fun ActionDescription(
    action: Action?,
    key: KeyM?,
    gesture: Gesture?,
    onNavigateBack: (() -> Unit)?,
    onNavigateToAction: (Action) -> Unit
) {
    if (action != null) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        Modifier.clickable { onNavigateBack() }
                    )
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
                if (fastAction != null) {
                    MenuPageLink(
                        onClick = {
                            onNavigateToAction(fastAction)
                        },
                        icon = painterResource(R.drawable.baseline_speed_24),
                        label = "Fast Action: ${fastAction.title}"
                    )
                }
                val shiftAction = key?.shift?.actions?.get(flick.direction)
                if (shiftAction != null && shiftAction != action && shiftAction.showAsRelatedInHelp) {
                    MenuPageLink(
                        onClick = {
                            onNavigateToAction(shiftAction)
                        },
                        icon = painterResource(R.drawable.baseline_rotate_right_24),
                        label = "Shift: ${shiftAction.title}"
                    )
                }
                if (gesture == Gesture.Tap && key?.holdAction != null) {
                    MenuPageLink(
                        onClick = {
                            onNavigateToAction(key.holdAction)
                        },
                        icon = painterResource(R.drawable.baseline_file_download_24),
                        label = "Hold: ${key.holdAction.title}"
                    )
                }
                if (flick.direction == Direction.CENTER) {
                    key?.actions?.forEach { (direction, swipeAction) ->
                        if (
                            direction != Direction.CENTER &&
                            // Only show otherwise hidden icons
                            (swipeAction.isHiddenAction || swipeAction.visual(null) == ActionVisual.None)
                        ) {
                            MenuPageLink(
                                onClick = {
                                    onNavigateToAction(swipeAction)
                                },
                                icon = painterResource(R.drawable.baseline_swipe_up_alt_24),
                                label = "${direction.title()}: ${swipeAction.title}",
                                iconModifier = Modifier.rotate(direction.angleFromTop().toFloat()),
                            )
                        }
                    }
                }
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