package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val NO_THUMBKEY_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.BOTTOM_RIGHT to Action.Text("p"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.BOTTOM to Action.Text("h"),
                    Direction.LEFT to Action.Text("z"),
                    Direction.RIGHT to Action.Text("q"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("o"),
                    Direction.BOTTOM_LEFT to Action.Text("u"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.RIGHT to Action.Text("v"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("d"),
                    Direction.TOP_LEFT to Action.Text("j"),
                    Direction.TOP to Action.Text("y"),
                    Direction.TOP_RIGHT to Action.Text("ø"),
                    Direction.LEFT to Action.Text("c"),
                    Direction.RIGHT to Action.Text("å"),
                    Direction.BOTTOM_LEFT to Action.Text("b"),
                    Direction.BOTTOM to Action.Text("f"),
                    Direction.BOTTOM_RIGHT to Action.Text("æ"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.LEFT to Action.Text("g"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP_RIGHT to Action.Text("m"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP to Action.Text("k"),
                    Direction.RIGHT to Action.Text("x"),
                    Direction.LEFT to Action.Text("w"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP_LEFT to Action.Text("l"),
                ),
            ),
        ),
        listOf(SPACE),
    ),
)

val NO_THUMBKEY = Layout(
    mainLayer = NO_THUMBKEY_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
)

@Composable
@Preview
fun NoThumbKeyKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(NO_THUMBKEY_MAIN_LAYER))
}

@Composable
@Preview
fun NoThumbKeyFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = NO_THUMBKEY, showAllModifiers = true)
}
