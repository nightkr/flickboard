package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val FR_EXT_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.TOP_LEFT to Action.Text("ï"),
                    Direction.TOP to Action.Text("à"),
                    Direction.TOP_RIGHT to Action.Text("â"),
                    Direction.LEFT to Action.Text("ç"),
                    Direction.BOTTOM_RIGHT to Action.Text("v"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("z"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP_LEFT to Action.Text("û"),
                    Direction.TOP_RIGHT to Action.Text("ô"),
                    Direction.RIGHT to Action.Text("î"),
                    Direction.BOTTOM_LEFT to Action.Text("x"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("l"),
                    Direction.TOP to Action.Text("ù"),
                    Direction.RIGHT to Action.Text("h"),
                    Direction.BOTTOM to Action.Text("w"),
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
                    Direction.BOTTOM to Action.Text("d"),
                    Direction.BOTTOM_RIGHT to Action.Text("j"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.LEFT to Action.Text("m"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.BOTTOM_LEFT to Action.Text("ë"),
                    Direction.BOTTOM to Action.Text("k"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("é"),
                    Direction.LEFT to Action.Text("è"),
                    Direction.RIGHT to Action.Text("ê")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f"),
                    Direction.BOTTOM_RIGHT to Action.Text("ü")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val FR_EXT_MESSAGEASE = Layout(
    mainLayer = FR_EXT_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun FrExtKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(FR_EXT_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun FrExtFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = FR_EXT_MESSAGEASE)
}
