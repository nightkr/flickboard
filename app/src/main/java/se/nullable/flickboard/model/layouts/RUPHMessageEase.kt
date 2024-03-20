package se.nullable.flickboard.model.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val RU_PHONETIC_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("а"),
                    Direction.BOTTOM to Action.Text("ч"),
                    Direction.BOTTOM_RIGHT to Action.Text("ж")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.Text("й"),
                    Direction.CENTER to Action.Text("н"),
                    Direction.BOTTOM to Action.Text("л")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("и"),
                    Direction.BOTTOM_LEFT to Action.Text("х")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("в"),
                    Direction.TOP to Action.Text("ъ"),
                    Direction.RIGHT to Action.Text("к"),
                    Direction.BOTTOM to Action.Text("ь"),
                    Direction.BOTTOM_RIGHT to Action.Text("ы")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("о"),
                    Direction.TOP_LEFT to Action.Text("я"),
                    Direction.TOP to Action.Text("у"),
                    Direction.TOP_RIGHT to Action.Text("п"),
                    Direction.LEFT to Action.Text("ц"),
                    Direction.RIGHT to Action.Text("б"),
                    Direction.BOTTOM_LEFT to Action.Text("г"),
                    Direction.BOTTOM to Action.Text("д"),
                    Direction.BOTTOM_RIGHT to Action.Text("й")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("р"),
                    Direction.LEFT to Action.Text("м")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("т"),
                    Direction.TOP to Action.Text("ё"),
                    Direction.TOP_RIGHT to Action.Text("ю"),
                    Direction.RIGHT to Action.Text("щ")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("е"),
                    Direction.LEFT to Action.Text("ш"),
                    Direction.TOP to Action.Text("э"),
                    Direction.RIGHT to Action.Text("з")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("с"),
                    Direction.TOP_LEFT to Action.Text("ф")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val RU_PHONETIC_MESSAGEASE = Layout(
    mainLayer = RU_PHONETIC_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun RuPhKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(RU_PHONETIC_MESSAGEASE_MAIN_LAYER), onAction = {})
    }
}

@Composable
@Preview
fun RuPhFullKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = RU_PHONETIC_MESSAGEASE, onAction = {})
    }
}
