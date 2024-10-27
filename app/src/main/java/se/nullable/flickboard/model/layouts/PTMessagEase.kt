package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val PT_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.TOP to Action.Text("ô"),
                    Direction.TOP_RIGHT to Action.Text("â"),
                    Direction.LEFT to Action.Text("ã"),
                    Direction.BOTTOM to Action.Text("á"),
                    Direction.BOTTOM_RIGHT to Action.Text("v")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("l")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.RIGHT to Action.Text("õ"),
                    Direction.BOTTOM_LEFT to Action.Text("x")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("d"),
                    Direction.RIGHT to Action.Text("k")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("o"),
                    Direction.TOP_LEFT to Action.Text("q"),
                    Direction.TOP to Action.Text("u"),
                    Direction.TOP_RIGHT to Action.Text("p"),
                    Direction.LEFT to Action.Text("c"),
                    Direction.RIGHT to Action.Text("b"),
                    Direction.BOTTOM_LEFT to Action.Text("g"),
                    Direction.BOTTOM to Action.Text("h"),
                    Direction.BOTTOM_RIGHT to Action.Text("j")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.LEFT to Action.Text("m")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP to Action.Text("ú"),
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.RIGHT to Action.Text("ê"),
                    Direction.BOTTOM to Action.Text("ó")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("w"),
                    Direction.RIGHT to Action.Text("z")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val PT_MESSAGEASE = Layout(
    mainLayer = PT_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun PtKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(PT_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun PtFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = PT_MESSAGEASE, showAllModifiers = true)
}
