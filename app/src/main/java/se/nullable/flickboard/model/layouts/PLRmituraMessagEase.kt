// Polish keyboard layout based on letter frequency https://www.sttmedia.com/characterfrequency-polish
// with accent characters kept together with base ones
// |v x|   |l q|
// | A | N | I£|
// | ą | ń |ł  |
//  _____________
// |cć |óup|   | 
// | W |kOb|mR |
// |   |gdj|   |
// ______________
// | y | h |f  |
// | Zż|ęEt|śS | 
// | ź |   |   |

package se.nullable.flickboard.model.layouts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.ui.KeyboardLayoutPreview

val PL_RMITURA_MESSAGEASE_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.TOP_LEFT to Action.Text("v"),
                    Direction.TOP_RIGHT to Action.Text("x"),
                    Direction.BOTTOM to Action.Text("ą")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM to Action.Text("ń")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP_LEFT to Action.Text("l"),
                    Direction.TOP_RIGHT to Action.Text("q"),
                    Direction.RIGHT to Action.Text("£"),
                    Direction.BOTTOM_LEFT to Action.Text("ł")
                )
            ),
        ),
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("w"),
                    Direction.TOP to Action.Text("c"),
                    Direction.TOP_RIGHT to Action.Text("ć")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("o"),
                    Direction.TOP_LEFT to Action.Text("ó"),
                    Direction.TOP to Action.Text("u"),
                    Direction.TOP_RIGHT to Action.Text("p"),
                    Direction.LEFT to Action.Text("k"),
                    Direction.RIGHT to Action.Text("b"),
                    Direction.BOTTOM_LEFT to Action.Text("g"),
                    Direction.BOTTOM to Action.Text("d"),
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
                    Direction.CENTER to Action.Text("z"),
                    Direction.TOP_RIGHT to Action.Text("y"),
                    Direction.RIGHT to Action.Text("ż"),
                    Direction.BOTTOM to Action.Text("ź")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("e"),
                    Direction.TOP to Action.Text("h"),
                    Direction.LEFT to Action.Text("ę"),
                    Direction.RIGHT to Action.Text("t")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("s"),
                    Direction.TOP_LEFT to Action.Text("f"),
                    Direction.LEFT to Action.Text("ś")
                )
            ),
        ),
        listOf(SPACE)
    )
)

val PL_RMITURA_MESSAGEASE = Layout(
    mainLayer = PL_RMITURA_MESSAGEASE_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun PlRmituraKeyboardPreview() {
    KeyboardLayoutPreview(layout = Layout(PL_RMITURA_MESSAGEASE_MAIN_LAYER))
}

@Composable
@Preview
fun PlRmituraFullKeyboardPreview() {
    KeyboardLayoutPreview(layout = PL_RMITURA_MESSAGEASE, showAllModifiers = true)
}
