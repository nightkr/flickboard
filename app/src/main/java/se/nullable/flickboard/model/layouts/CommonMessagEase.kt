package se.nullable.flickboard.model.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.CaseChangeDirection
import se.nullable.flickboard.model.Direction
import se.nullable.flickboard.model.KeyM
import se.nullable.flickboard.model.Layer
import se.nullable.flickboard.model.Layout
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.ShiftState
import se.nullable.flickboard.ui.KeyboardLayoutPreview

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

val OVERLAY_ADVANCED_MODIFIERS_MESSAGEASE_LAYER =
    Layer(
        keyRows = listOf(
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.TOP_LEFT to Action.ToggleAlt,
                        Direction.TOP_RIGHT to Action.ToggleCtrl,
                    )
                )
            ),
        )
    )

val OVERLAY_TOGGLE_SYMBOLS_MESSAGEASE_LAYER =
    Layer(
        keyRows = listOf(
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
                KeyM(actions = mapOf()),
            ),
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.TOP to Action.ToggleShowSymbols,
                    )
                )
            ),
        )
    )

val CONTROL_MESSAGEASE_LAYER =
    Layer(
        keyRows = listOf(
            // pointer
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.ToggleActiveLayer,
                        Direction.TOP to Action.AdjustCellHeight(amount = 1F),
                        Direction.RIGHT to Action.Settings,
                        Direction.BOTTOM_LEFT to Action.SwitchLetterLayer(SearchDirection.Backwards),
                        Direction.BOTTOM to Action.AdjustCellHeight(amount = -1F),
                        Direction.BOTTOM_RIGHT to Action.SwitchLetterLayer(SearchDirection.Forwards),
                    ),
                )
            ),
            // clipboard
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.ToggleSelect,
                        Direction.TOP_LEFT to Action.Cut,
                        Direction.TOP to Action.Copy,
                        Direction.TOP_RIGHT to Action.Cut,
                        Direction.BOTTOM to Action.Paste,
                        Direction.BOTTOM_LEFT to Action.EnableVoiceMode,
                        Direction.BOTTOM_RIGHT to Action.EnableVoiceMode,
                    ),
                    shift = KeyM(actions = mapOf(Direction.CENTER to Action.SelectAll)),
                )
            ),
            // backspace
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.Delete(),
                        // Hide since it's not the primary way, but
                        // it's a nice bit of symmetry with the right-hand
                        // version.
                        Direction.LEFT to Action.Delete(hidden = true),
                        Direction.RIGHT to Action.Delete(
                            direction = SearchDirection.Forwards,
                            hidden = true
                        ),
                    ),
                    fastActions = mapOf(
                        Direction.LEFT to Action.FastDelete(direction = SearchDirection.Backwards),
                        Direction.RIGHT to Action.FastDelete(direction = SearchDirection.Forwards),
                    ),
                )
            ),
            // enter
            listOf(
                KeyM(
                    actions = mapOf(
                        Direction.CENTER to Action.Enter(),
                        Direction.TOP_LEFT to Action.ToggleEmojiMode,
                        Direction.TOP_RIGHT to Action.ToggleEmojiMode,
                        Direction.BOTTOM to Action.ToggleZalgo,
                    )
                )
            ),
        )
    )

val OVERLAY_MESSAGEASE_LAYER = Layer(
    keyRows = listOf(
        listOf(
            KeyM(actions = mapOf()),
            KeyM(actions = mapOf()),
            KeyM(actions = mapOf(Direction.TOP to Action.JumpLineKeepPos(SearchDirection.Backwards))),
        ),
        listOf(
            KeyM(actions = mapOf()),
            KeyM(actions = mapOf()),
            KeyM(
                actions = mapOf(
                    Direction.TOP to Action.ToggleShift(ShiftState.Shift),
                    Direction.BOTTOM to Action.ToggleShift(ShiftState.Normal),
                ),
                transientShift = KeyM(
                    actions = mapOf(
                        Direction.TOP to Action.ToggleWordCase(CaseChangeDirection.Up),
                        Direction.BOTTOM to Action.ToggleWordCase(CaseChangeDirection.Down),
                    )
                )
            ),
        ),
        listOf(
            KeyM(actions = mapOf()),
            KeyM(actions = mapOf()),
            KeyM(actions = mapOf(Direction.BOTTOM to Action.JumpLineKeepPos(SearchDirection.Forwards))),
        ),
        listOf(KeyM(actions = mapOf()))
    )
)

@Composable
@Preview
fun CommonKeyboardPreview() {
    Box(Modifier.width(100.dp)) {
        KeyboardLayoutPreview(layout = Layout(CONTROL_MESSAGEASE_LAYER))
    }
}
