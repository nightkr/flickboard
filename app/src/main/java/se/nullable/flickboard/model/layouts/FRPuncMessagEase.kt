package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val FR_PUNC_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.TOP to Action.Text("*"),
                    Direction.TOP_RIGHT to Action.Text("à"),
                    Direction.BOTTOM_RIGHT to Action.Text("v"),
                    Direction.LEFT to Action.Text("«"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("l"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP_RIGHT to Action.Text("’"),
                    Direction.TOP_LEFT to Action.Text("·"),
                    Direction.RIGHT to Action.Text("»"),
                    Direction.BOTTOM_LEFT to Action.Text("x"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("u"),
                    Direction.TOP to Action.Text("ê"),
                    Direction.RIGHT to Action.Text("k"),
                    Direction.BOTTOM to Action.Text("ç"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("o"),
                    Direction.TOP_LEFT to Action.Text("q"),
                    Direction.TOP to Action.Text("h"),
                    Direction.TOP_RIGHT to Action.Text("p"),
                    Direction.RIGHT to Action.Text("b"),
                    Direction.BOTTOM_RIGHT to Action.Text("j"),
                    Direction.BOTTOM to Action.Text("d"),
                    Direction.BOTTOM_LEFT to Action.Text("g"),
                    Direction.LEFT to Action.Text("c"),
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
                    Direction.RIGHT to Action.Text("è"),
                    Direction.BOTTOM to Action.Text("ù"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("w"),
                    Direction.RIGHT to Action.Text("z"),
                    Direction.LEFT to Action.Text("é"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f"),
                )
            ),
        ),
        listOf(SPACE)
    )
)

val FR_PUNC_MESSAGEASE = Layout(
    mainLayer = FR_PUNC_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun FrPuncKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(FR_PUNC_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun FrPuncFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = FR_PUNC_MESSAGEASE)
}
