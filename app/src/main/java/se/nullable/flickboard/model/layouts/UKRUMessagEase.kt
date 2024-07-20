package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val UK_RU_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("с"),
                    Direction.BOTTOM to Action.Text("ц"),
                    Direction.BOTTOM_RIGHT to Action.Text("п")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.Text("й"),
                    Direction.CENTER to Action.Text("и"),
                    Direction.BOTTOM to Action.Text("к")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("т"),
                    Direction.BOTTOM_LEFT to Action.Text("ь"),
                    Direction.BOTTOM_RIGHT to Action.Text("э")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("в"),
                    Direction.TOP to Action.Text("б"),
                    Direction.RIGHT to Action.Text("ы"),
                    Direction.BOTTOM to Action.Text("і")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("о"),
                    Direction.TOP_LEFT to Action.Text("ч"),
                    Direction.TOP to Action.Text("м"),
                    Direction.TOP_RIGHT to Action.Text("х"),
                    Direction.LEFT to Action.Text("ж"),
                    Direction.RIGHT to Action.Text("г"),
                    Direction.BOTTOM_LEFT to Action.Text("щ"),
                    Direction.BOTTOM to Action.Text("я"),
                    Direction.BOTTOM_RIGHT to Action.Text("ш")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("а"),
                    Direction.LEFT to Action.Text("л")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("е"),
                    Direction.TOP to Action.Text("ї"),
                    Direction.TOP_RIGHT to Action.Text("д"),
                    Direction.RIGHT to Action.Text("є")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("р"),
                    Direction.LEFT to Action.Text("ю"),
                    Direction.TOP to Action.Text("у"),
                    Direction.RIGHT to Action.Text("з")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("н"),
                    Direction.TOP_LEFT to Action.Text("ф"),
                    Direction.TOP_RIGHT to Action.Text("ё")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val UK_RU_MESSAGEASE = Layout(
    mainLayer = UK_RU_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun UkRuKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(UK_RU_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun UkRuFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = UK_RU_MESSAGEASE)
}
