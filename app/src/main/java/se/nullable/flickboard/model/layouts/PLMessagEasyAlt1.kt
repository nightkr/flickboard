// Polish keyboard layout based on letter frequency https://www.sttmedia.com/characterfrequency-polish
// with accent characters kept together with base ones
// |   |   |q  |
// | A | N |lI£| 
// | ą |vńx|ł  |
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
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val PL_MESSAGEASEAlt1_MAIN_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("a"),
                    Direction.BOTTOM to Action.Text("ą")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("n"),
                    Direction.BOTTOM_LEFT to Action.Text("v"),
                    Direction.BOTTOM to Action.Text("ń"),
                    Direction.BOTTOM_RIGHT to Action.Text("x")
                )
            ),
            KeyM(
                actions = mapOf(
                    Direction.CENTER to Action.Text("i"),
                    Direction.TOP_LEFT to Action.Text("q"),
                    Direction.LEFT to Action.Text("l"),
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
                    Direction.LEFT to Action.Text("m"),
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

val PL_MESSAGEASEasyAlt1 = Layout(
    mainLayer = PL_MESSAGEASEAlt1_MAIN_LAYER,
    controlLayer = CONTROL_MESSAGEASE_LAYER
)

@Composable
@Preview
fun PlKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = Layout(PL_MESSAGEASEAlt1_MAIN_LAYER), onAction = {})
    }
}

@Composable
@Preview
fun PlFullKeyboardPreview() {
    FlickBoardParent {
        Keyboard(layout = PL_MESSAGEASEasyAlt1, showAllModifiers = true, onAction = {})
    }
}