package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val DE_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.BOTTOM to Action.Text("ä"),
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
                    Direction.BOTTOM_LEFT to Action.Text("x")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.Text("ü"),
                    Direction.CENTER to Action.Text("h"),
                    Direction.RIGHT to Action.Text("k"),
                    Direction.BOTTOM to Action.Text("ö")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("d"),
                    Direction.TOP_LEFT to Action.Text("q"),
                    Direction.TOP to Action.Text("u"),
                    Direction.TOP_RIGHT to Action.Text("p"),
                    Direction.LEFT to Action.Text("c"),
                    Direction.RIGHT to Action.Text("b"),
                    Direction.BOTTOM_LEFT to Action.Text("g"),
                    Direction.BOTTOM to Action.Text("o"),
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
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.BOTTOM to Action.Text("ß")
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.BOTTOM to Action.Text("ẞ")
                    )
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

val DE_MESSAGEASE = Layout(
    mainLayer = DE_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun DeKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(DE_MESSAGEASE_MAIN_LAYER), onAction = {})
    }
}

@Composable
@Preview
fun DeFullKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = DE_MESSAGEASE, onAction = {})
    }
}
