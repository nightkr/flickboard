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

val DE_EO_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.BOTTOM to Action.Text("ä"),
                    Direction.BOTTOM_RIGHT to Action.Text("v"),
                    Direction.TOP_RIGHT to Action.Text("ŭ"),
                    Direction.TOP to Action.Text("ĝ"),
                    Direction.TOP_RIGHT to Action.Text("ŭ"),
                    Direction.LEFT to Action.Text("ŝ")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("l")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.BOTTOM_LEFT to Action.Text("x"),
                    Direction.TOP_LEFT to Action.Text("ĉ"),
                    Direction.TOP_RIGHT to Action.Text("ĵ"),
                    Direction.RIGHT to Action.Text("ĥ")

                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.Text("ü"),
                    Direction.CENTER to Action.Text("h"),
                    Direction.RIGHT to Action.Text("k"),
                    Direction.BOTTOM to Action.Text("ö")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("d"),
                    Direction.TOP_LEFT to Action.Text("q"),
                    Direction.TOP to Action.Text("u"),
                    Direction.TOP_RIGHT to Action.Text("p"),
                    Direction.LEFT to Action.Text("c"),
                    Direction.RIGHT to Action.Text("b"),
                    Direction.BOTTOM_LEFT to Action.Text("g"),
                    Direction.BOTTOM to Action.Text("o"),
                    Direction.BOTTOM_RIGHT to Action.Text("j")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("r"),
                    Direction.LEFT to Action.Text("m")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("t"),
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.BOTTOM to Action.Text("ß"),
                    Direction.BOTTOM_LEFT to Action.Text("ch"),
                ),
                shift = KeyM(
                  actions = mapOf(
                      Direction.BOTTOM to Action.Text("ẞ"),
                      Direction.BOTTOM_LEFT to Action.Text("Ch")
                  )
                )

            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("w"),
                    Direction.RIGHT to Action.Text("z"),
                    Direction.LEFT to Action.Text("ck")
                ),
                shift = KeyM(
                    actions = mapOf(
                    Direction.LEFT to Action.Text("Ck")
                    )
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f"),
                    Direction.BOTTOM_RIGHT to Action.Text("sch")
                ),
                shift = KeyM(
                    actions = mapOf(
                        Direction.BOTTOM_RIGHT to Action.Text("Sch")
                    )
                )
            ),
        ),
        listOf(SPACE)
    )
)

val DE_EO_MESSAGEASE = Layout(
    mainLayer = DE_EO_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun DeEoKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(DE_EO_MESSAGEASE_MAIN_LAYER), onAction = {})
    }
}

@Composable
@Preview
fun DeEoFullKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = DE_EO_MESSAGEASE, onAction = {})
    }
}
