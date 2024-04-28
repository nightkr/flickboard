package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.R
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val MESSAGEASE_SYMBOLS_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.BOTTOM_LEFT to Action.Text("$"),
                    Direction.BOTTOM to Action.Text("…"),
                    Direction.RIGHT to Action.Text("-"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.RIGHT to Action.Text("÷"),
                        Direction.BOTTOM_LEFT to Action.Text("¥"),
                        Direction.BOTTOM to Action.Text("•")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("`"),
                    Direction.TOP to Action.Text("^"),
                    Direction.TOP_RIGHT to Action.Text("´"),
                    Direction.LEFT to Action.Text("+"),
                    Direction.RIGHT to Action.Text("!"),
                    Direction.BOTTOM_LEFT to Action.Text("/"),
                    Direction.BOTTOM_RIGHT to Action.Text("\\"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("‘"),
                        Direction.TOP to Action.Text("ˇ"),
                        Direction.TOP_RIGHT to Action.Text("’"),
                        Direction.LEFT to Action.Text("×"),
                        Direction.RIGHT to Action.Text("¡"),
                        Direction.BOTTOM_LEFT to Action.Text("–"),
                        Direction.BOTTOM_RIGHT to Action.Text("—")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.LEFT to Action.Text("?"),
                    Direction.BOTTOM to Action.Text("="),
                    Direction.BOTTOM_RIGHT to Action.Text("€"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.LEFT to Action.Text("¿"),
                        Direction.BOTTOM to Action.Text("±"),
                        Direction.BOTTOM_RIGHT to Action.Text("£")
                    )
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("{"),
                    Direction.TOP_RIGHT to Action.Text("%"),
                    Direction.LEFT to Action.Text("("),
                    Direction.BOTTOM_LEFT to Action.Text("["),
                    Direction.BOTTOM_RIGHT to Action.Text("_"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("}"),
                        Direction.TOP_RIGHT to Action.Text("‰"),
                        Direction.LEFT to Action.Text(")"),
                        Direction.BOTTOM_LEFT to Action.Text("]"),
                        Direction.BOTTOM to Action.Text("¯"),
                        Direction.BOTTOM_RIGHT to Action.Text("¬")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("|"),
                    Direction.TOP_RIGHT to Action.Text("}"),
                    Direction.RIGHT to Action.Text(")"),
                    Direction.BOTTOM_LEFT to Action.Text("@"),
                    Direction.BOTTOM_RIGHT to Action.Text("]"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("¶"),
                        Direction.TOP_RIGHT to Action.Text("{"),
                        Direction.RIGHT to Action.Text("("),
                        Direction.BOTTOM_LEFT to Action.Text("ª"),
                        Direction.BOTTOM_RIGHT to Action.Text("[")
                    )
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("~"),
                    Direction.TOP to Action.Text("¨"),
                    Direction.LEFT to Action.Text("<"),
                    Direction.RIGHT to Action.Text("*"),
                    Direction.BOTTOM_RIGHT to Action.Text(
                        "\t",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Icon(R.drawable.baseline_keyboard_tab_24)
                    ),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("˜"),
                        Direction.TOP to Action.Text("˝"),
                        Direction.LEFT to Action.Text("‹"),
                        Direction.RIGHT to Action.Text("†"),
                        Direction.BOTTOM_LEFT to Action.Text("«")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("\""),
                    Direction.TOP_RIGHT to Action.Text("'"),
                    Direction.BOTTOM_LEFT to Action.Text(","),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text(":"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("“"),
                        Direction.TOP_RIGHT to Action.Text("”"),
                        Direction.BOTTOM_LEFT to Action.Text("‚"),
                        Direction.BOTTOM to Action.Text("…"),
                        Direction.BOTTOM_RIGHT to Action.Text("„")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.Text("&"),
                    Direction.TOP_RIGHT to Action.Text("°"),
                    Direction.LEFT to Action.Text("#"),
                    Direction.RIGHT to Action.Text(">"),
                    Direction.BOTTOM_LEFT to Action.Text(";"),
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP to Action.Text("§"),
                        Direction.TOP_RIGHT to Action.Text("º"),
                        Direction.LEFT to Action.Text("£"),
                        Direction.RIGHT to Action.Text("›"),
                        Direction.BOTTOM_LEFT to Action.Text("¸"),
                        Direction.BOTTOM_RIGHT to Action.Text("»")
                    )
                )
            ),
        ),
        listOf(
            KeyM(actions = mapOf(), colspan = 2),
            SPACE.copy(colspan = 1)
        )
    )
)

val MESSAGEASE_NUMERIC_PHONE_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("1"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("2"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("3"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("4"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("5"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("6"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("7"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("8"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("9"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("0")), colspan = 2),
            SPACE.copy(colspan = 1)
        )
    )
)

val MESSAGEASE_NUMERIC_CALCULATOR_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("7"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("8"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("9"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("4"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("5"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("6"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("1"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("2"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("3"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("0")), colspan = 2),
            SPACE.copy(colspan = 1)
        )
    )
)

@Composable
@Preview
fun NumericKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(Layer.empty), onAction = {})
    }
}
