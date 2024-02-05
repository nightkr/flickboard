package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.Keyboard

val MESSAGEASE_NUMERIC_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("1"),
                    Direction.BOTTOM_LEFT to Action.Text("$"),
                    Direction.BOTTOM to Action.Text("…"),
                    Direction.RIGHT to Action.Text("-"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("2"),
                    Direction.TOP_LEFT to Action.Text("`"),
                    Direction.TOP to Action.Text("^"),
                    Direction.TOP_RIGHT to Action.Text("´"),
                    Direction.LEFT to Action.Text("+"),
                    Direction.RIGHT to Action.Text("!"),
                    Direction.BOTTOM_LEFT to Action.Text("/"),
                    Direction.BOTTOM_RIGHT to Action.Text("\\"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("3"),
                    Direction.LEFT to Action.Text("?"),
                    Direction.BOTTOM to Action.Text("="),
                    Direction.BOTTOM_RIGHT to Action.Text("€"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("4"),
                    Direction.TOP_LEFT to Action.Text("{"),
                    Direction.TOP_RIGHT to Action.Text("%"),
                    Direction.LEFT to Action.Text("("),
                    Direction.BOTTOM_LEFT to Action.Text("["),
                    Direction.BOTTOM_RIGHT to Action.Text("_"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("5"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("6"),
                    Direction.TOP_LEFT to Action.Text("|"),
                    Direction.TOP_RIGHT to Action.Text("}"),
                    Direction.RIGHT to Action.Text(")"),
                    Direction.BOTTOM_LEFT to Action.Text("@"),
                    Direction.BOTTOM_RIGHT to Action.Text("]"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("7"),
                    Direction.TOP_LEFT to Action.Text("~"),
                    Direction.TOP to Action.Text("¨"),
                    Direction.LEFT to Action.Text("<"),
                    Direction.RIGHT to Action.Text("*"),
                    Direction.BOTTOM_RIGHT to Action.Text("\t"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("8"),
                    Direction.TOP_LEFT to Action.Text("\""),
                    Direction.TOP_RIGHT to Action.Text("'"),
                    Direction.BOTTOM_LEFT to Action.Text(","),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text(":"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("9"),
                    Direction.TOP to Action.Text("&"),
                    Direction.TOP_RIGHT to Action.Text("°"),
                    Direction.LEFT to Action.Text("#"),
                    Direction.RIGHT to Action.Text(">"),
                    Direction.BOTTOM_LEFT to Action.Text(";"),
                )
            ),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("0"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("0"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("0"))),
        )
    )
)

@Composable
@Preview
fun NumericKeyboardPreview() {
    Keyboard(layout = Layout(MESSAGEASE_NUMERIC_LAYER), onAction = {})
}
