package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.ActionVisual
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val FA_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ه"),
                    Direction.RIGHT to Action.Text("ـ"),
                    Direction.BOTTOM to Action.Text("ۀ"),
                    Direction.BOTTOM_RIGHT to Action.Text("ق"),

                    )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ب"),
                    Direction.BOTTOM_LEFT to Action.Text("ض"),
                    Direction.BOTTOM to Action.Text("خ"),
                    Direction.BOTTOM_RIGHT to Action.Text("پ"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("م"),
                    Direction.TOP_RIGHT to Action.Text(
                        "\u200F", //RLM
                        visualOverride = ActionVisual.Label(
                            "¶‹",
                            directionOverride = TextDirection.RightToLeft
                        ),
                    ),
                    Direction.BOTTOM_LEFT to Action.Text("چ"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ی"),
                    Direction.TOP_RIGHT to Action.Text("ص"),
                    Direction.RIGHT to Action.Text("ش"),
                    Direction.BOTTOM_RIGHT to Action.Text("ط"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ا"),
                    Direction.TOP_LEFT to Action.Text("ف"),
                    Direction.TOP to Action.Text("ح"),
                    Direction.TOP_RIGHT to Action.Text("ج"),
                    Direction.LEFT to Action.Text("س"),
                    Direction.RIGHT to Action.Text("آ"),
                    Direction.BOTTOM_LEFT to Action.Text("ل"),
                    Direction.BOTTOM to Action.Text("ت"),
                    Direction.BOTTOM_RIGHT to Action.Text("ک"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ر"),
                    Direction.TOP_LEFT to Action.Text("ژ"),
                    Direction.LEFT to Action.Text("ز"),
                    Direction.BOTTOM_LEFT to Action.Text("ع"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("و"),
                    Direction.TOP_RIGHT to Action.Text("ؤ"),
                    Direction.RIGHT to Action.Text(
                        "\u200C", //ZWNJ
                        visualOverride = ActionVisual.Label("‹›"),
                    ),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ن"),
                    Direction.TOP_LEFT to Action.Text("ظ"),
                    Direction.TOP to Action.Text("ث"),
                    Direction.TOP_RIGHT to Action.Text("غ"),
                    Direction.LEFT to Action.Text("ء"),
                    Direction.RIGHT to Action.Text("أ"),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text("ئ"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("د"),
                    Direction.TOP_LEFT to Action.Text("گ"),
                    Direction.LEFT to Action.Text("ذ"),
                )
            ),
        ),
        listOf(SPACE)
    )
)

val FA_MESSAGEASE = Layout(
    mainLayer = FA_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
    digits = "۰۱۲۳۴۵۶۷۸۹",
    textDirection = TextDirection.RightToLeft,
    symbolLayer = FA_SYMBOLS_LAYER,
    miniSymbolLayer = FA_MINI_NUMBERS_SYMBOLS_LAYER,
)

@Composable
@Preview
fun FaMessagEaseKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(FA_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun FaMessagEaseFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = FA_MESSAGEASE, showAllModifiers = true)
}
