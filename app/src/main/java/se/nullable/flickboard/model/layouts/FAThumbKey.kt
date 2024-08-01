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

val FA_THUMBKEY_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("د"),
                    Direction.BOTTOM to Action.Text("ض"),                    
                    Direction.BOTTOM_RIGHT to Action.Text("ص"),
                    
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ر"),
                    Direction.RIGHT to Action.Text("ژ"),
                    Direction.BOTTOM_LEFT to Action.Text("ق"),
                    Direction.BOTTOM to Action.Text("ز"),
                    Direction.BOTTOM_RIGHT to Action.Text("ف"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("و"),
                    Direction.TOP_RIGHT to Action.Text(
                        "\u200F", //RLM
                        visualOverride = ActionVisual.Label("¶‹"),
                    ),                    
                    Direction.BOTTOM_LEFT to Action.Text("ع"),
                    Direction.BOTTOM to Action.Text("ء"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ن"),
                    Direction.TOP_RIGHT to Action.Text("ح"),
                    Direction.RIGHT to Action.Text("ج"),
                    Direction.BOTTOM_RIGHT to Action.Text("چ"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ا"),
                    Direction.TOP_LEFT to Action.Text("ذ"),
                    Direction.TOP to Action.Text("ب"),
                    Direction.TOP_RIGHT to Action.Text("پ"),
                    Direction.LEFT to Action.Text("خ"),
                    Direction.RIGHT to Action.Text("س"),
                    Direction.BOTTOM_LEFT to Action.Text("آ"),
                    Direction.BOTTOM to Action.Text("ل"),
                    Direction.BOTTOM_RIGHT to Action.Text("ش"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("م"),
                    Direction.TOP_LEFT to Action.Text("غ"),
                    Direction.LEFT to Action.Text("ک"),
                    Direction.BOTTOM_LEFT to Action.Text("گ"),
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ت"),
                    Direction.TOP_RIGHT to Action.Text("ث"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ی"),
                    Direction.TOP to Action.Text(
                        "\u200C", //ZWNJ
                        visualOverride = ActionVisual.Label("‹›"),
                    ),                    
                    Direction.LEFT to Action.Text("؟"),
                    Direction.RIGHT to Action.Text("ئ"),
                    Direction.BOTTOM_LEFT to Action.Text("*"),
                    Direction.BOTTOM to Action.Text("."),
                    Direction.BOTTOM_RIGHT to Action.Text("ـ"),
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ه"),
                    Direction.TOP_LEFT to Action.Text("ط"),
                    Direction.TOP to Action.Text("ظ"),
                    Direction.LEFT to Action.Text("ۀ"),
                )
            ),
        ),
        listOf(SPACE)
    )
)

val FA_THUMBKEY = Layout(
    mainLayer = FA_THUMBKEY_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
    digits = "۰۱۲۳۴۵۶۷۸۹",
    textDirection = TextDirection.RightToLeft,
)

@Composable
@Preview
fun FaKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(FA_THUMBKEY_MAIN_LAYER))
}

@Composable
@Preview
fun FaFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = FA_THUMBKEY, showAllModifiers = true)
}
