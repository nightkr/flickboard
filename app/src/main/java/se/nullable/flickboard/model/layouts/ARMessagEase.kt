package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val AR_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ه"),
                    Direction.BOTTOM_RIGHT to Action.Text("ق"),
                    Direction.BOTTOM to Action.Text("ة"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ب"),
                    Direction.TOP to Action.Text("ُ"),
                    Direction.TOP_RIGHT to Action.Text("َ"),
                    Direction.BOTTOM to Action.Text("خ"),
                    Direction.BOTTOM_LEFT to Action.Text("ض"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("م"),
                    Direction.LEFT to Action.Text("؟"),
                    Direction.TOP_LEFT to Action.Text("ْ"),
                    Direction.BOTTOM_LEFT to Action.Text("إ"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ي"),
                    Direction.RIGHT to Action.Text("ح"),
                    Direction.BOTTOM_RIGHT to Action.Text("ط"),
                    Direction.TOP_RIGHT to Action.Text("ص"),
                    Direction.BOTTOM to Action.Text("ى"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ا"),
                    Direction.TOP_LEFT to Action.Text("ف"),
                    Direction.TOP to Action.Text("ج"),
                    Direction.TOP_RIGHT to Action.Text("ش"),
                    Direction.LEFT to Action.Text("س"),
                    Direction.RIGHT to Action.Text("آ"),
                    Direction.BOTTOM_LEFT to Action.Text("ل"),
                    Direction.BOTTOM to Action.Text("ت"),
                    Direction.BOTTOM_RIGHT to Action.Text("ك"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ر"),
                    Direction.LEFT to Action.Text("ز"),
                    Direction.BOTTOM_LEFT to Action.Text("ع"),
                ),
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("و"),
                    Direction.TOP to Action.Text("ّ"),
                    Direction.TOP_RIGHT to Action.Text("ؤ"),
                ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ن"),
                    Direction.TOP to Action.Text("ث"),
                    Direction.RIGHT to Action.Text("أ"),
                    Direction.LEFT to Action.Text("ء"),
                    Direction.BOTTOM_RIGHT to Action.Text("ئ"),
                    Direction.TOP_LEFT to Action.Text("ظ"),
                    Direction.TOP_RIGHT to Action.Text("غ"),

                    ),
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("د"),
                    Direction.TOP to Action.Text("ً"),
                    Direction.LEFT to Action.Text("ذ"),
                ),
            ),
        ),
        listOf(SPACE),
    ),
)

val AR_MESSAGEASE = Layout(
    mainLayer = AR_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
    digits = "٠١٢٣٤٥٦٧٨٩",
    textDirection = TextDirection.RightToLeft,
)

@Composable
@Preview
fun ArKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(AR_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun ArFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = AR_MESSAGEASE, showAllModifiers = true)
}
