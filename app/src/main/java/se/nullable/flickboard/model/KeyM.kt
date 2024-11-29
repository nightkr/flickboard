package se.nullable.flickboard.model

import androidx.compose.ui.unit.Dp
import se.nullable.flickboard.R
import se.nullable.flickboard.model.layouts.MESSAGEASE_SYMBOLS_LAYER
import se.nullable.flickboard.model.layouts.MINI_NUMBERS_SYMBOLS_LAYER
import se.nullable.flickboard.util.HardLineBreakIterator
import se.nullable.flickboard.util.flipIfBracket
import java.text.BreakIterator
import java.util.Locale

data class Layout(
    val mainLayer: Layer,
    val locale: Locale = Locale.ENGLISH,
    val shiftLayer: Layer = mainLayer.autoShift(locale),
    val controlLayer: Layer? = null,
    val symbolLayer: Layer = MESSAGEASE_SYMBOLS_LAYER,
    val miniSymbolLayer: Layer = MINI_NUMBERS_SYMBOLS_LAYER,
    val digits: String = "0123456789",
    val textDirection: TextDirection = TextDirection.LeftToRight,
)

data class Layer(val keyRows: List<List<KeyM>>) {
    private inline fun zipKeys(other: Layer, f: (KeyM, KeyM) -> KeyM): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow.zip(otherRow) { thisKey, otherKey ->
                f(thisKey, otherKey)
            }
        })


    fun mergeFallback(fallback: Layer?, holdForFallback: Boolean = false): Layer =
        if (fallback != null) {
            zipKeys(fallback) { thisKey, fallbackKey ->
                thisKey.mergeFallback(
                    fallbackKey,
                    holdForFallback = holdForFallback
                )
            }
        } else {
            this
        }

    fun setShift(shift: Layer): Layer =
        zipKeys(shift) { thisKey, shiftKey -> thisKey.copy(shift = shiftKey) }

    fun chain(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow -> thisRow + otherRow })

    private inline fun mapKeys(f: (KeyM) -> KeyM): Layer =
        copy(keyRows = keyRows.map { row -> row.map { key -> f(key) } })

    fun flipBrackets(): Layer = mapKeys { it.flipBrackets() }

    fun autoShift(locale: Locale): Layer =
        mapKeys { it.autoShift(locale) }

    fun filterActions(shownActionClasses: Set<ActionClass>, enableHiddenActions: Boolean) =
        mapKeys {
            it.filterActions(
                shownActionClasses = shownActionClasses,
                enableHiddenActions = enableHiddenActions
            )
        }

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
    val fastActions: Map<Direction, Action> = emptyMap(),
    val longActions: Map<Direction, Action> = emptyMap(),
    val holdAction: Action? = null,
    val colspan: Int = 1,
    val fixedWidth: Dp? = null,
    val rendered: Boolean = true,
    val shift: KeyM? = null,
    val transientShift: KeyM? = null
) {
    fun mergeFallback(fallback: KeyM, holdForFallback: Boolean): KeyM = copy(
        actions = fallback.actions + actions,
        shift = when {
            shift == null -> fallback.shift
            fallback.shift == null -> shift
            else -> shift.mergeFallback(fallback.shift, holdForFallback = holdForFallback)
        },
        longActions = fallback.longActions + longActions,
        holdAction = this.holdAction ?: fallback.holdAction ?: when {
            holdForFallback -> fallback.actions[Direction.CENTER]
            else -> null
        },
        transientShift = this.transientShift ?: fallback.transientShift
    )

    fun autoShift(locale: Locale): KeyM = (shift ?: this).copy(
        actions = actions.mapValues { it.value.shift(locale) } + (shift?.actions ?: emptyMap()),
        fastActions = fastActions.mapValues { it.value.shift(locale) } + (shift?.fastActions
            ?: emptyMap())
    )

    fun filterActions(shownActionClasses: Set<ActionClass>, enableHiddenActions: Boolean) =
        copy(actions = actions.mapNotNull { (direction, action) ->
            val isShown = shownActionClasses.contains(action.actionClass)
            when {
                isShown -> direction to action
                enableHiddenActions -> direction to action.withHidden(true)
                else -> null
            }
        }.toMap())

    fun flipBrackets(): KeyM = copy(
        actions = actions.mapValues {
            when (val action = it.value) {
                is Action.Text -> action.copy(character = action.character.flipIfBracket())
                else -> action
            }
        },
        shift = shift?.flipBrackets(),
        transientShift = transientShift?.flipBrackets(),
    )
}

sealed class Action {
    abstract fun visual(modifier: ModifierState?): ActionVisual

    // FIXME: make title and description abstract before releasing
    open val title: String = "(TODO title)"
    open val description: String = "(TODO description)"
    open fun isActive(modifier: ModifierState?): Boolean = false
    open val actionClass = ActionClass.Other
    open val fastActionType: FastActionType? = null
    open fun shift(locale: Locale): Action = this
    open fun withHidden(hidden: Boolean): Action = this

    open val isModifier = false
    open val isHiddenAction: Boolean
        get() = isModifier

    data class Text(
        val character: String,
        val forceRawKeyEvent: Boolean = false,
        val visualOverride: ActionVisual? = null,
        val hidden: Boolean = false,
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            when {
                hidden -> ActionVisual.None
                else -> visualOverride ?: ActionVisual.Label(character)
            }

        private val characterName = when (character) {
            " " -> "space"
            "\t" -> "tab"
            else -> character
        }
        override val title: String = "Type $characterName"
        override val description: String =
            "Types the character $characterName into the active text field."

        override val actionClass: ActionClass = when {
            character.isBlank() -> ActionClass.Other
            character.all { it.isDigit() } -> ActionClass.Number
            character.all { it.isLetter() } -> ActionClass.Letter
            else -> ActionClass.Symbol
        }

        override fun shift(locale: Locale): Action =
            copy(character = character.uppercase(locale = locale))

        override fun withHidden(hidden: Boolean): Action = copy(hidden = hidden)
    }

    data class Delete(
        val direction: SearchDirection = SearchDirection.Backwards,
        val boundary: TextBoundary = TextBoundary.Character,
        val hidden: Boolean = false,
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when {
            hidden -> ActionVisual.None
            else -> ActionVisual.Icon(R.drawable.baseline_backspace_24)
        }

        override fun withHidden(hidden: Boolean): Action = copy(hidden = hidden)

        override val title: String = run {
            val actionName = when (direction) {
                SearchDirection.Backwards -> "Backspace"
                SearchDirection.Forwards -> "Delete"
            }
            when {
                boundary == TextBoundary.Character -> actionName
                else -> "$actionName ${boundary.nameInSentence}"
            }
        }
        override val description: String = run {
            val directionName = when (direction) {
                SearchDirection.Backwards -> "previous"
                SearchDirection.Forwards -> "next"
            }
            "Deletes the $directionName ${boundary.nameInSentence}."
        }

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data class FastDelete(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Character
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_backspace_24)

        override val fastActionType: FastActionType = FastActionType.Delete

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data object BeginFastAction : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = ActionVisual.None
        override val isHiddenAction: Boolean = true
    }

    data class FastActionDone(val type: FastActionType) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = ActionVisual.None
        override val isHiddenAction: Boolean = true
    }

    data class Enter(val shift: Boolean = false) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_return_24)

        override fun shift(locale: Locale): Action = copy(shift = true)
    }

    data object Escape : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual {
            return ActionVisual.Label("esc")
        }
    }

    data class Jump(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Character,
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_left_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_right_24)
        }

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data class JumpLineKeepPos(val direction: SearchDirection, val rawEvent: Boolean = false) :
        Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_up_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_down_24)
        }

        override fun shift(locale: Locale): Action = copy(rawEvent = true)
    }

    data class ToggleShift(val state: ShiftState) : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when (state) {
            ShiftState.Normal -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_down_24)
            ShiftState.Shift -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_up_24)
            ShiftState.CapsLock -> ActionVisual.Icon(R.drawable.baseline_keyboard_capslock_24)
        }

        override fun isActive(modifier: ModifierState?): Boolean = state == modifier?.shift

        override fun shift(locale: Locale): Action {
            return when (state) {
                ShiftState.Shift -> copy(state = ShiftState.CapsLock)
                else -> return this
            }
        }
    }

    /**
     * Shift the case for the current word up/down one step (lower <-> Title <-> UPPER )
     */
    data class ToggleWordCase(val direction: CaseChangeDirection, val locale: Locale) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            CaseChangeDirection.Up -> ActionVisual.Icon(R.drawable.baseline_keyboard_double_arrow_up_24)
            CaseChangeDirection.Down -> ActionVisual.Icon(R.drawable.baseline_keyboard_double_arrow_down_24)
        }
    }

    data object ToggleCtrl : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when {
            modifier?.ctrl ?: true -> ActionVisual.Label("ctrl")
            else -> ActionVisual.None
        }
    }

    data object ToggleAlt : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when {
            modifier?.alt ?: true -> ActionVisual.Label("alt")
            else -> ActionVisual.None
        }
    }

    data object ToggleZalgo : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.outline_cell_merge_24)

        override fun isActive(modifier: ModifierState?): Boolean = modifier?.zalgo ?: false
    }

    data object ToggleSelect : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual =
            when {
                modifier?.select
                    ?: false -> ActionVisual.Icon(R.drawable.outline_text_select_end_24)

                else -> ActionVisual.Icon(R.drawable.outline_text_select_start_24)
            }

        override fun isActive(modifier: ModifierState?): Boolean = modifier?.select ?: false
    }

    data object Cut : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_cut_24)
    }

    data object Copy : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_copy_24)
    }

    data object Paste : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_paste_24)
    }

    data object Settings : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_settings_24)
    }

    data class SwitchLetterLayer(val direction: SearchDirection) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_24)
    }

    data object SwitchSystemKeyboard : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_24)
    }

    /**
     * Toggles which layer is "active"
     *
     * Intent: "I want easy access to the opposite layer with my current hand"
     */
    data object ToggleActiveLayer : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_flip_camera_android_24)

        override fun shift(locale: Locale): Action = ToggleHandedness
    }

    /**
     * Toggles between left- and right-handed mode
     *
     * Intent: "I want to switch the active hand comfortably"
     */
    data object ToggleHandedness : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_flip_camera_android_24)
    }

    /**
     * Toggles whether a numbers layer is "active"
     *
     * Intent: "I want easy access to the numbers layer"
     *
     * [ToggleActiveLayer] will also do this, if there is only one active layer.
     */
    data object ToggleNumbersLayer : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_flip_camera_android_24)
    }

    data class AdjustCellHeight(val amount: Float) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when {
            amount >= 0 -> ActionVisual.Icon(R.drawable.baseline_zoom_in_24)
            else -> ActionVisual.Icon(R.drawable.baseline_zoom_out_24)
        }
    }

    data object SelectAll : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_select_all_24)
    }

    data object ToggleEmojiMode : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_emoji_emotions_24)
    }

    data object ToggleShowSymbols : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.None

        override fun shift(locale: Locale): Action = ToggleShowLetters
    }

    data object ToggleShowLetters : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.None
    }

    data object EnableVoiceMode : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_mic_24)
    }
}

enum class FastActionType {
    Delete,
}

enum class ActionClass {
    Letter, Number, Symbol, Other,
}

sealed class ActionVisual {
    data class Icon(val resource: Int) : ActionVisual()
    data class Label(
        val label: String,
        val directionOverride: TextDirection? = null
    ) : ActionVisual()

    data object None : ActionVisual()
}

enum class TextBoundary(val nameInSentence: String) {
    Character(nameInSentence = "character"),
    Word(nameInSentence = "word"),
    Line(nameInSentence = "line");

    fun breakIterator(): BreakIterator = when (this) {
        Character -> BreakIterator.getCharacterInstance()
        Word -> BreakIterator.getWordInstance()
        Line -> HardLineBreakIterator()
    }
}

enum class TextDirection {
    LeftToRight,
    RightToLeft,
}

enum class SearchDirection(val factor: Int) {
    Backwards(factor = -1),
    Forwards(factor = 1),
}

enum class CaseChangeDirection { Up, Down }

enum class Direction {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

    fun isCorner(): Boolean = when (this) {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT -> true
        else -> false
    }
}

interface Gesture {
    data class Flick(
        val direction: Direction,
        val longHold: Boolean,
        val longSwipe: Boolean,
        val shift: Boolean
    ) :
        Gesture {
        override fun toFlick(
            longHoldOnClockwiseCircle: Boolean,
            longHoldOnCounterClockwiseCircle: Boolean
        ): Flick = this

        fun resolveAction(key: KeyM): Action? = when {
            longHold -> key.holdAction
            else -> when {
                shift -> key.transientShift?.actions?.get(direction)
                    ?: key.shift?.actions?.get(direction)

                else -> null
            } ?: when {
                longSwipe -> key.longActions[direction]
                else -> null
            } ?: key.actions[direction]
        }
    }

    data class Circle(val direction: CircleDirection) : Gesture {
        override fun toFlick(
            longHoldOnClockwiseCircle: Boolean,
            longHoldOnCounterClockwiseCircle: Boolean
        ): Flick {
            return when {
                direction == CircleDirection.Clockwise && longHoldOnClockwiseCircle ->
                    Flick(Direction.CENTER, longHold = true, longSwipe = false, shift = false)

                direction == CircleDirection.CounterClockwise && longHoldOnCounterClockwiseCircle ->
                    Flick(Direction.CENTER, longHold = true, longSwipe = false, shift = false)

                else -> Flick(Direction.CENTER, longHold = false, longSwipe = false, shift = true)
            }
        }
    }

    fun toFlick(
        longHoldOnClockwiseCircle: Boolean,
        longHoldOnCounterClockwiseCircle: Boolean
    ): Flick

    companion object {
        val names = Direction.entries
            .flatMap {
                listOf(
                    "flick.${it.name}" to Flick(
                        direction = it,
                        longHold = false,
                        longSwipe = false,
                        shift = false
                    ),
                    "flick.${it.name}.shift" to Flick(
                        direction = it,
                        longHold = false,
                        longSwipe = false,
                        shift = true
                    ),
                )
            }
            .toMap() +
                CircleDirection.entries.associate { "circle.${it.name}" to Circle(it) }
    }
}

enum class CircleDirection {
    Clockwise,
    CounterClockwise,
}
