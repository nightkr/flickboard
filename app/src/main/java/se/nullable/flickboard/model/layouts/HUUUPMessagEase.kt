package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val HU_UUP_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.TOP to Action.Text("ú"),
                    Direction.TOP_RIGHT to Action.Text("á"),
                    Direction.BOTTOM to Action.Text("ó"),
                    Direction.BOTTOM_RIGHT to Action.Text("v"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("l"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP_LEFT to Action.Text("í"),
                    Direction.BOTTOM_LEFT to Action.Text("x"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("h"),
                    Direction.TOP to Action.Text("ü"),
                    Direction.RIGHT to Action.Text("k"),
                    Direction.BOTTOM to Action.Text("ö"),
                ),
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
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.LEFT to Action.Text("m"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP to Action.Text("ű"),
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.BOTTOM to Action.Text("ő"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("w"),
                    Direction.LEFT to Action.Text("é"),
                    Direction.RIGHT to Action.Text("z"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f"),
                ),
            ),
        ),
        listOf(SPACE),
    ),
)

val HU_UUP_MESSAGEASE = Layout(
    mainLayer = HU_UUP_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
)

@Composable
@Preview
fun HuUUpKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(HU_UUP_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun HuUUpFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = HU_UUP_MESSAGEASE)
}
