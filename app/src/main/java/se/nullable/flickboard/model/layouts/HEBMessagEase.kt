package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.TextDirection
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val HEB_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ר"),
                    Direction.BOTTOM_RIGHT to Action.Text("ן")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ב"),
                    Direction.BOTTOM to Action.Text("ג")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("א"),
                    Direction.BOTTOM_LEFT to Action.Text("צ")
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.BOTTOM_LEFT to Action.Text("ץ")
                    )
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("מ"),
                    Direction.RIGHT to Action.Text("ם")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("י"),
                    Direction.TOP_LEFT to Action.Text("ק"),
                    Direction.TOP to Action.Text("ח"),
                    Direction.TOP_RIGHT to Action.Text("פ"),
                    Direction.LEFT to Action.Text("ע"),
                    Direction.RIGHT to Action.Text("ד"),
                    Direction.BOTTOM_LEFT to Action.Text("כ"),
                    Direction.BOTTOM to Action.Text("נ"),
                    Direction.BOTTOM_RIGHT to Action.Text("ש")
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.TOP_RIGHT to Action.Text("ף"),
                        Direction.BOTTOM_LEFT to Action.Text("ך")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ו")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ת"),
                    Direction.TOP_RIGHT to Action.Text("ז")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ה"),
                    Direction.TOP to Action.Text("ס")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("ל"),
                    Direction.TOP_LEFT to Action.Text("ט")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val HEB_MESSAGEASE = Layout(
    mainLayer = HEB_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER,
    textDirection = TextDirection.RightToLeft,
)

@Composable
@Preview
fun HebKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(HEB_MESSAGEASE_MAIN_LAYER), onAction = {})
    }
}

@Composable
@Preview
fun HebFullKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = HEB_MESSAGEASE, onAction = {})
    }
}
