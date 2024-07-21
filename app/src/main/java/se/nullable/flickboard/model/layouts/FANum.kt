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

val FA_SYMBOLS_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.BOTTOM_LEFT to Action.Text("﷼"),
                    Direction.BOTTOM to Action.Text("٫"),
                    Direction.RIGHT to Action.Text(
                        "ّ",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Label("ـّ")
                    )                    
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
                    Direction.BOTTOM_RIGHT to Action.Text("\\")
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
                    Direction.LEFT to Action.Text("؟"),
                    Direction.BOTTOM to Action.Text("="),
                    Direction.BOTTOM_RIGHT to Action.Text("$"),
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
                    Direction.TOP_RIGHT to Action.Text("٪"),
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
                    Direction.TOP_LEFT to Action.Text(
                        "ُ",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Label("ـُ")
                    ),
                    Direction.TOP_RIGHT to Action.Text(
                        "َ",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Label("ـَ")
                    ),
                    Direction.BOTTOM to Action.Text(
                        "ِ",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Label("ـِ")
                    )
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
                    Direction.TOP_RIGHT to Action.Text(
                        "ً",
                        forceRawKeyEvent = true,
                        visualOverride = ActionVisual.Label("ـً")
                    ),                                       
                    Direction.LEFT to Action.Text("«"),
                    Direction.BOTTOM_LEFT to Action.Text("<"),
                    Direction.BOTTOM_RIGHT to Action.Text(":")
                ),    
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Text("˜"),
                        Direction.TOP to Action.Text("˝"),
                        Direction.LEFT to Action.Text("‹"),
                        Direction.RIGHT to Action.Text("†"),
                        Direction.BOTTOM_LEFT to Action.Text("«"),
                        Direction.BOTTOM_RIGHT to Action.Text(
                            "\t",
                            forceRawKeyEvent = true,
                            visualOverride = ActionVisual.Icon(R.drawable.baseline_keyboard_tab_24)
                        )
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP_LEFT to Action.Text("\""),
                    Direction.TOP_RIGHT to Action.Text("'"),
                    Direction.LEFT to Action.Text("،"),
                    Direction.BOTTOM_LEFT to Action.Text("*"),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text("-"),
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
                    Direction.RIGHT to Action.Text("»"),
                    Direction.BOTTOM_LEFT to Action.Text("؛"),
                    Direction.BOTTOM_RIGHT to Action.Text(">")
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

val FA_NUMERIC_PHONE_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۱"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۲"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۳"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۴"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۵"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۶"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۷"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۸"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۹"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۰")), colspan = 2),
            SPACE.copy(colspan = 1)
        )
    )
)

val FA_NUMERIC_CALCULATOR_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۷"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۸"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۹"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۴"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۵"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۶"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۱"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۲"))),
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۳"))),
        ),
        listOf(
            KeyM(actions = mapOf(Direction.CENTER to Action.Text("۰")), colspan = 2),
            SPACE.copy(colspan = 1)
        )
    )
)

@Composable
@Preview
fun FaNumericKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(Layer.empty), onAction = {})
    }
}
