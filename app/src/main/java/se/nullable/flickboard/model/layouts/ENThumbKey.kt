package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val EN_THUMBKEY_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.BOTTOM_RIGHT to Action.Text("w"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.BOTTOM to Action.Text("g"),
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
                    Direction.RIGHT to Action.Text("m"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("h"),
                    Direction.TOP_LEFT to Action.Text("j"),
                    Direction.TOP to Action.Text("q"),
                    Direction.TOP_RIGHT to Action.Text("b"),
                    Direction.LEFT to Action.Text("k"),
                    Direction.RIGHT to Action.Text("p"),
                    Direction.BOTTOM_LEFT to Action.Text("v"),
                    Direction.BOTTOM to Action.Text("x"),
                    Direction.BOTTOM_RIGHT to Action.Text("y"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.LEFT to Action.Text("l"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP_RIGHT to Action.Text("c"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP to Action.Text("f"),
                    Direction.RIGHT to Action.Text("z"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP_LEFT to Action.Text("d"),
                ),
            ),
        ),
        listOf(SPACE),
    ),
)

val EN_THUMBKEY = Layout(
    mainLayer = EN_THUMBKEY_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
)

@Composable
@Preview
fun EnThumbKeyKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(EN_THUMBKEY_MAIN_LAYER))
}

@Composable
@Preview
fun EnThumbKeyFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = EN_THUMBKEY, showAllModifiers = true)
}
