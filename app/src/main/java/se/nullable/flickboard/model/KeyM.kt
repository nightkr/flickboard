package se.nullable.flickboard.model

import se.nullable.flickboard.R
import se.nullable.flickboard.util.HardLineBreakIterator
import java.text.BreakIterator

data class Layout(
    val mainLayer: Layer,
    val shiftLayer: Layer = mainLayer.autoShift(),
    val controlLayer: Layer? = null
)

data class Layer(val keyRows: List<List<KeyM>>) {
    private inline fun zipKeys(other: Layer, f: (KeyM, KeyM) -> KeyM): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow.zip(otherRow) { thisKey, otherKey ->
                f(thisKey, otherKey)
            }
        })


    fun mergeFallback(fallback: Layer?): Layer =
        if (fallback != null) {
            zipKeys(fallback) { thisKey, fallbackKey -> thisKey.mergeFallback(fallbackKey) }
        } else {
            this
        }

    fun mergeShift(shift: Layer): Layer =
        zipKeys(shift) { thisKey, shiftKey ->
            thisKey.copy(
                shift = thisKey.shift?.mergeFallback(shiftKey) ?: shiftKey
            )
        }

    fun chain(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow + otherRow
        })

    fun autoShift(): Layer =
        copy(keyRows = keyRows.map { row -> row.map { key -> key.autoShift() } })

    companion object {
        val empty = Layer(
            // The number of rows must match any other layers being merged with
            keyRows = listOf(listOf(), listOf(), listOf(), listOf()),
        )
    }
}

data class KeyM(
    val actions: Map<Direction, Action>,
    // Fast actions are performed immediately when detected, rather than when the finger is released.
    val fastActions: Map<Direction, Action> = mapOf(),
    val holdAction: Action? = null,
    val colspan: Int = 1,
    val shift: KeyM? = null
) {
    fun mergeFallback(fallback: KeyM): KeyM = copy(
        actions = fallback.actions + actions,
        shift = when {
            shift == null -> fallback.shift
            fallback.shift == null -> shift
            else -> shift.mergeFallback(fallback.shift)
        },
        holdAction = this.holdAction ?: fallback.holdAction ?: fallback.actions[Direction.CENTER]
    )

    fun autoShift(): KeyM = (shift ?: this).copy(
        actions = actions.mapValues { it.value.shift() } + (shift?.actions ?: emptyMap()),
    )
}

sealed class Action {
    abstract fun visual(modifier: ModifierState): ActionVisual
    open val actionClass = ActionClass.Other
    open fun shift(): Action = this

    data class Text(val character: String, val label: String = character) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = ActionVisual.Label(label)
        override val actionClass: ActionClass = when {
            character.isEmpty() -> ActionClass.Other
            character.all { it.isDigit() } -> ActionClass.Number
            character.all { it.isLetter() } -> ActionClass.Letter
            else -> ActionClass.Symbol
        }

        override fun shift(): Action {
            return copy(character = character.uppercase(), label = label.uppercase())
        }
    }

    data class Delete(
        val direction: SearchDirection = SearchDirection.Backwards,
        val boundary: TextBoundary = TextBoundary.Character,
        val hidden: Boolean = false,
    ) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when {
            hidden -> ActionVisual.None
            else -> ActionVisual.Icon(R.drawable.baseline_backspace_24)
        }

        override fun shift(): Action = copy(boundary = TextBoundary.Word)
    }

    data object Enter : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_return_24)
    }

    data class Jump(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Character,
    ) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_left_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_right_24)
        }

        override fun shift(): Action = copy(boundary = TextBoundary.Word)
    }

    data class JumpLineKeepPos(val direction: SearchDirection) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_up_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_down_24)
        }
    }

    data class ToggleShift(val state: ShiftState) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when (state) {
            ShiftState.Normal -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_down_24)
            ShiftState.Shift -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_up_24)
            ShiftState.CapsLock -> ActionVisual.Icon(R.drawable.baseline_keyboard_capslock_24)
        }

        override fun shift(): Action {
            return when (state) {
                ShiftState.Shift -> copy(state = ShiftState.CapsLock)
                else -> return this
            }
        }
    }

    data object ToggleCtrl : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when {
            modifier.ctrl -> ActionVisual.Label("ctrl")
            else -> ActionVisual.None
        }
    }

    data object ToggleAlt : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when {
            modifier.alt -> ActionVisual.Label("alt")
            else -> ActionVisual.None
        }
    }

    data object Cut : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_cut_24)
    }

    data object Copy : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_copy_24)
    }

    data object Paste : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_paste_24)
    }

    data object Settings : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_settings_24)
    }

    data class SwitchLetterLayer(val direction: SearchDirection) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_24)
    }

    data object ToggleLayerOrder : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_flip_camera_android_24)
    }

    data class AdjustCellHeight(val amount: Float) : Action() {
        override fun visual(modifier: ModifierState): ActionVisual = when {
            amount >= 0 -> ActionVisual.Icon(R.drawable.baseline_zoom_in_24)
            else -> ActionVisual.Icon(R.drawable.baseline_zoom_out_24)
        }
    }

    data object SelectAll : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_select_all_24)
    }

    data object ToggleEmojiMode : Action() {
        override fun visual(modifier: ModifierState): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_emoji_emotions_24)
    }
}

enum class ActionClass {
    Letter, Number, Symbol, Other,
}

sealed class ActionVisual {
    data class Icon(val resource: Int) : ActionVisual()
    data class Label(val label: String) : ActionVisual()
    data object None : ActionVisual()
}

enum class TextBoundary {
    Character,
    Word,
    Line;

    fun breakIterator(): BreakIterator = when (this) {
        Character -> BreakIterator.getCharacterInstance()
        Word -> BreakIterator.getWordInstance()
        Line -> HardLineBreakIterator()
    }
}

enum class SearchDirection(val factor: Int) {
    Backwards(factor = -1),
    Forwards(factor = 1),
}

enum class Direction {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

    fun isCorner(): Boolean = when (this) {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT -> true
        else -> false
    }
}

data class Gesture(val direction: Direction, val longHold: Boolean, val shift: Boolean)