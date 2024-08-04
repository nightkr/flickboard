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
import se.nullable.flickboard.ui.KeyboardLayoutPreview

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
                    Direction.BOTTOM_LEFT to Action.Text("ˇ"),
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
            KeyM(actions = mapOf(), colspan = 2F),
            SPACE.copy(colspan = 1F)
        )
    )
)

fun messageaseNumericPhoneLayer(digits: String) = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[1].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[2].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[3].toString()))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[4].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[5].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[6].toString()))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[7].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[8].toString()))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text(digits[9].toString()))),
        ),
        listOf(
            KeyM(
                actions = mapOf(Direction.CENTER to Action.Text(digits[0].toString())),
                colspan = 2F
            ),
            SPACE.copy(colspan = 1F)
        )
    )
)

fun messageaseNumericCalculatorLayer(digits: String) =
    messageaseNumericPhoneLayer(reorderDigitsForCalculatorLayout(digits))

fun reorderDigitsForCalculatorLayout(digits: String) =
    "${digits[0]}${digits.substring(7..9)}${digits.substring(4..6)}${digits.substring(1..3)}"

@Composable
@Preview
fun NumericKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(Layer.empty))
}
