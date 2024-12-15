package se.nullable.flickboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.Gesture
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.theme.Typography
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialPage(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val fullOKey = KeyM(
        actions = mapOf(
            Direction.CENTER to Action.Text("o"),
            Direction.TOP_LEFT to Action.Text("q"),
            Direction.TOP to Action.Text("u"),
            Direction.TOP_RIGHT to Action.Text("p"),
            Direction.LEFT to Action.Text("c"),
            Direction.RIGHT to Action.Text("b"),
            Direction.BOTTOM_LEFT to Action.Text("g"),
            Direction.BOTTOM to Action.Text("d"),
            Direction.BOTTOM_RIGHT to Action.Text("j"),
        ),
    )

    val scope = rememberCoroutineScope()
    fun PagerState.animateScrollToNextPage(pageOffset: Int = 1) {
        scope.launch {
            animateScrollToPage(currentPage + pageOffset)
        }
    }

    val pages = listOf<@Composable (PagerState) -> Unit>(
        { pager ->
            val handedness = LocalAppSettings.current.handedness
            val handednessState = handedness.state
            Column(Modifier.padding(8.dp)) {
                Text("Personalization", style = Typography.titleMedium)
                Text("Which side is your dominant hand?")
                Row(
                    Modifier
                        .selectableGroup()
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                ) {
                    Handedness.entries.forEach { option ->
                        Column(
                            Modifier
                                .selectable(
                                    selected = handednessState.value == option,
                                    onClick = {
                                        handedness.currentValue = option
                                        pager.animateScrollToNextPage()
                                    },
                                    role = Role.RadioButton,
                                ),
                            horizontalAlignment = when (option) {
                                Handedness.LeftHanded -> AbsoluteAlignment.Left
                                Handedness.RightHanded -> AbsoluteAlignment.Right
                            },
                        ) {
                            Row {
                                RadioButton(
                                    selected = handednessState.value == option,
                                    onClick = null,
                                )
                                Text(option.label, Modifier)
                            }
                            TutorialKey(fullOKey, onAction = null)
                        }
                    }
                }
            }
        },
        { pager ->
            TutorialKeyStep(
                content = {
                    Text("Typing", style = Typography.titleMedium)
                    Text("To type the character in the middle of a key, tap it.")
                    Text("Try tapping the key now, to type an o!")
                },
                key = KeyM(actions = mapOf(Direction.CENTER to Action.Text("o"))),
                desiredGesture = Gesture.Flick(
                    direction = Direction.CENTER,
                    longHold = false,
                    longSwipe = false,
                    shift = false,
                ),
                onGesturePerformed = pager::animateScrollToNextPage,
            )
        },
        { pager ->
            TutorialKeyStep(
                content = {
                    Text("Typing (edge)", style = Typography.titleMedium)
                    Text("To type a character at the edge of a key, swipe in that direction.")
                    Text("Try swiping towards the right from the key, to type a b!")
                },
                key = KeyM(actions = mapOf(Direction.RIGHT to Action.Text("b"))),
                desiredGesture = Gesture.Flick(
                    direction = Direction.RIGHT,
                    longHold = false,
                    longSwipe = false,
                    shift = false,
                ),
                onGesturePerformed = pager::animateScrollToNextPage,
            )
        },
        { pager ->
            TutorialKeyStep(
                content = {
                    Text("Typing (corner)", style = Typography.titleMedium)
                    Text("Characters can also appear at a corner of the key.")
                    Text("Try swiping towards the bottom-left from key, to type a g!")
                },
                key = KeyM(actions = mapOf(Direction.BOTTOM_LEFT to Action.Text("g"))),
                desiredGesture = Gesture.Flick(
                    direction = Direction.BOTTOM_LEFT,
                    longHold = false,
                    longSwipe = false,
                    shift = false,
                ),
                onGesturePerformed = pager::animateScrollToNextPage,
            )
        },
        { pager ->
            TutorialKeyStep(
                content = {
                    Text("Upper-case", style = Typography.titleMedium)
                    Text("To type an upper-case character in the middle of a key, draw a circle.")
                    Text("Try typing an upper-case O now!")
                },
                key = fullOKey,
                desiredGesture = Gesture.Flick(
                    direction = Direction.CENTER,
                    longHold = false,
                    longSwipe = false,
                    shift = true,
                ),
                onGesturePerformed = pager::animateScrollToNextPage,
            )
        },
        { pager ->
            TutorialKeyStep(
                content = {
                    Text("Upper-case (edge)", style = Typography.titleMedium)
                    Text("To type an upper-case character at an edge, swipe towards it and then return to where you started.")
                    Text("Try typing an upper-case G now!")
                },
                key = fullOKey,
                desiredGesture = Gesture.Flick(
                    direction = Direction.BOTTOM_LEFT,
                    longHold = false,
                    longSwipe = false,
                    shift = true,
                ),
                onGesturePerformed = pager::animateScrollToNextPage,
            )
        },
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                Text("Great, you've got the hang of it!")
                Button(onClick = onFinish) {
                    Icon(painterResource(R.drawable.baseline_check_24), null)
                    Text("Finish")
                }
            }
        },
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(8.dp)
            .fillMaxHeight(),
    ) {
        Box {}
        Column {
            Text("Welcome to FlickBoard!", style = Typography.titleLarge)
            Text("FlickBoard is a pretty unusual keyboard, so let's get you a quick run-down of how to use it.")
        }
        Column {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(192.dp),
            ) {
                Card {
                    pages[it](pagerState)
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = { pagerState.animateScrollToNextPage(-1) }) {
                    Icon(painterResource(R.drawable.baseline_keyboard_arrow_left_24), "Previous")
                }
                Row {
                    repeat(pagerState.pageCount) { page ->
                        val colour = animateColorAsState(
                            when {
                                page == pagerState.currentPage -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
                        Box(
                            Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(colour.value)
                                .size(16.dp),
                        )
                    }
                }
                IconButton(onClick = { pagerState.animateScrollToNextPage(1) }) {
                    Icon(painterResource(R.drawable.baseline_keyboard_arrow_right_24), "Next")
                }
            }
        }
    }
}

@Composable
fun TutorialKeyStep(
    content: @Composable () -> Unit,
    key: KeyM,
    desiredGesture: Gesture.Flick,
    onGesturePerformed: () -> Unit
) {
    val normalizedKey = key.copy(shift = key.autoShift(Locale.ENGLISH))
    val desiredAction = desiredGesture.resolveAction(normalizedKey)
    val handedness = LocalAppSettings.current.handedness.state

    @Composable
    fun RowScope.WrappedContent() {
        Column(
            Modifier
                .padding(8.dp)
                .weight(1F),
        ) {
            content()
        }
    }
    Row(
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (handedness.value == Handedness.RightHanded) {
            WrappedContent()
        }
        TutorialKey(
            normalizedKey,
            onAction = { action, _, _ ->
                when {
                    action == desiredAction -> onGesturePerformed()
                }
                true
            },
        )
        if (handedness.value == Handedness.LeftHanded) {
            WrappedContent()
        }
    }
}

@Composable
fun TutorialKey(key: KeyM, onAction: OnAction?) {
    Key(
        key,
        onAction = onAction,
        modifierState = null,
        modifier = Modifier
            .size(96.dp)
            .padding(8.dp)
            .border(Dp.Hairline, MaterialTheme.colorScheme.surface),
        layoutTextDirection = TextDirection.LeftToRight,
    )
}

@Composable
@Preview
fun TutorialPreview() {
    FlickBoardParent {
        Surface {
            TutorialPage(onFinish = {})
        }
    }
}