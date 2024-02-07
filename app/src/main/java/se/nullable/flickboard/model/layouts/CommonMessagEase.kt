package se.nullable.flickboard.model.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard

val SPACE = KeyM(
    actions = mapOf(
        Direction.CENTER to Action.Text(" "),
        Direction.LEFT to Action.Jump(direction = SearchDirection.Backwards),
        Direction.RIGHT to Action.Jump(direction = SearchDirection.Forwards),
    ),
    fastActions = mapOf(
        Direction.LEFT to Action.Jump(direction = SearchDirection.Backwards),
        Direction.RIGHT to Action.Jump(direction = SearchDirection.Forwards),
    ),
    colspan = 3
)

val COMMON_MESSAGEASE_LAYER =
    Layer(
        keyRows = listOf(
            // pointer
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.ToggleLayerOrder,
                        Direction.TOP to Action.Settings
                    )
                )
            ),
            // clipboard
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.Cut,
                        Direction.TOP to Action.Copy,
                        Direction.TOP_RIGHT to Action.Cut,
                        Direction.BOTTOM to Action.Paste,
                    )
                )
            ),
            // backspace
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.Delete(),
                        Direction.RIGHT to Action.Delete(
                            direction = SearchDirection.Forwards,
                            hidden = true
                        )
                    )
                )
            ),
            // enter
            listOf(KeyM(actions = mapOf(Direction.CENTER to Action.Enter))),
        )
    )

@Composable
@Preview
fun CommonKeyboardPreview() {
    FlickBoardParent {
        Box(Modifier.width(100.dp)) {
            Keyboard(layout = Layout(COMMON_MESSAGEASE_LAYER), onAction = {})
        }
    }
}
