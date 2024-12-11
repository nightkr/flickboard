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

    abstract val title: String
    abstract val description: String
    open val showAsRelatedInHelp: Boolean = true

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

        override fun withHidden(hidden: Boolean): Action = copy(hidden = hidden)

        private val characterName = when (character) {
            " " -> "space"
            "\t" -> "tab"
            "\n" -> "newline"
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
        override val description: String =
            "Deletes the ${direction.nextOrPrevInSentence} ${boundary.nameInSentence}."

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data class FastDelete(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Character
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_backspace_24)

        override val title: String = "Fast Delete"
        override val description: String =
            "Marks a cursor movement during a Fast Delete. This should not be visible."

        override val fastActionType: FastActionType = FastActionType.Delete

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data object BeginFastAction : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = ActionVisual.None
        override val isHiddenAction: Boolean = true

        override val title: String = "Begin Fast Action"
        override val description: String =
            "Fake event that marks the start of a fast action. This should not be visible."
    }

    data class FastActionDone(val type: FastActionType) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = ActionVisual.None
        override val isHiddenAction: Boolean = true

        override val title: String = "Fast Action Done"
        override val description: String =
            "Fake event that marks that a gesture containing fast actions is done, and that any previewed actions should be executed. This should not be visible."
    }

    data class Enter(val shift: Boolean = false) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_return_24)

        override val title: String = "Enter"
        override val description: String =
            "Sends an enter event, either executing the action or inserting a newline."

        override fun shift(locale: Locale): Action = copy(shift = true)
    }

    data object Escape : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual {
            return ActionVisual.Label("esc")
        }

        override val title: String = "Escape"
        override val description: String = "May close open pop-up windows, if the app supports it."
    }

    data class Jump(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Character,
    ) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_left_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_right_24)
        }

        override val title: String =
            "Jump To ${direction.nextOrPrevInTitle} ${boundary.nameInTitle}"
        override val description: String =
            "Moves the cursor to the ${direction.nextOrPrevInSentence} ${boundary.nameInSentence}."

        override fun shift(locale: Locale): Action = copy(boundary = TextBoundary.Word)
    }

    data class JumpLineKeepPos(val direction: SearchDirection, val rawEvent: Boolean = false) :
        Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            SearchDirection.Backwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_up_24)
            SearchDirection.Forwards -> ActionVisual.Icon(R.drawable.baseline_keyboard_arrow_down_24)
        }

        override val title: String = "Jump To ${direction.nextOrPrevInTitle} Line"
        override val description: String =
            "Moves the cursor to the ${direction.nextOrPrevInSentence} line."
        override val showAsRelatedInHelp: Boolean = !rawEvent

        override fun shift(locale: Locale): Action = copy(rawEvent = true)
    }

    data class ToggleShift(val state: ShiftState) : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when (state) {
            ShiftState.Normal -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_down_24)
            ShiftState.Shift -> ActionVisual.Icon(R.drawable.baseline_arrow_drop_up_24)
            ShiftState.CapsLock -> ActionVisual.Icon(R.drawable.baseline_keyboard_capslock_24)
        }

        override val title: String = when (state) {
            ShiftState.Normal -> "Disable Shift"
            ShiftState.Shift -> "Enable Shift"
            ShiftState.CapsLock -> "Enable Caps Lock"
        }
        override val description: String = when (state) {
            ShiftState.Normal -> "Disables any active shift modifier."
            ShiftState.Shift -> "Replaces the next action with a shifted variant (such as an upper-case letter or alternate symbol)." +
                    " This can also be done by drawing a circle (for tap gestures) or U (for edge gestures) on the key."

            ShiftState.CapsLock -> "Turns all letters upper-case until shift is disabled manually."
        }

        override fun isActive(modifier: ModifierState?): Boolean = state == modifier?.shift

        override fun shift(locale: Locale): Action {
            return when (state) {
                ShiftState.Shift -> copy(state = ShiftState.CapsLock)
                else -> return this
            }
        }
    }

    // Not an actual usable icon, but a pseudo-icon that shows up in help texts
    data object TransientShift : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_rotate_right_24)

        override val title: String = "Transient Shift"
        override val description: String =
            "A gesture can be made shifted by drawing a circle (for tap gestures) or U (for edge gestures) on the key.\n" +
                    "Gestures that require transient shift can only be made this way, not by using shift mode."
    }

    /**
     * Shift the case for the current word up/down one step (lower <-> Title <-> UPPER )
     */
    data class ToggleWordCase(val direction: CaseChangeDirection, val locale: Locale) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when (direction) {
            CaseChangeDirection.Up -> ActionVisual.Icon(R.drawable.baseline_keyboard_double_arrow_up_24)
            CaseChangeDirection.Down -> ActionVisual.Icon(R.drawable.baseline_keyboard_double_arrow_down_24)
        }

        override val title: String = "Shift Word ${direction.nameInTitle}"
        override val description: String = when (direction) {
            CaseChangeDirection.Up -> "Replaces the last typed word with an 'upperer-case' variant. Cycles lower-case -> Title-case, UPPER-CASE."
            CaseChangeDirection.Down -> "Replaces the last typed word with an 'lowerer-case' variant. Cycles UPPER-CASE -> Title-case, lower-case."
        }
    }

    data object ToggleCtrl : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when {
            modifier?.ctrl ?: true -> ActionVisual.Label("ctrl")
            else -> ActionVisual.None
        }

        override val title: String = "Toggle Control Modifier"
        override val description: String =
            "If enabled, the control modifier will be applied to the next typed character. This can be used to activate keyboard shortcuts intended for physical keyboards."
    }

    data object ToggleAlt : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual = when {
            modifier?.alt ?: true -> ActionVisual.Label("alt")
            else -> ActionVisual.None
        }

        override val title: String = "Toggle Alt Modifier"
        override val description: String =
            "If enabled, the alt modifier will be applied to the next typed character. This can be used to activate keyboard shortcuts intended for physical keyboards."
    }

    data object ToggleZalgo : Action() {
        override val isModifier: Boolean = true

        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.outline_cell_merge_24)

        override val title: String = "Toggle Zalgo Modifier"
        override val description: String =
            "If enabled, the next typed character will be be combined into the previous (if supported).\n" +
                    "For example, c-zalgo-¨ becomes c̈, and a-zalgo-e becomes æ."

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

        override val title: String = "Toggle Select Mode"
        override val description: String =
            "If enabled, text movement (such as swiping on the space bar) will instead select text."

        override fun isActive(modifier: ModifierState?): Boolean = modifier?.select ?: false
    }

    data object Cut : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_cut_24)

        override val title: String = "Cut"
        override val description: String =
            "Moves all selected text into the clipboard, and deletes it from the source field."
    }

    data object Copy : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_copy_24)

        override val title: String = "Copy"
        override val description: String =
            "Copies all selected text into the clipboard. If nothing is selected, everything is selected and copied."
    }

    data object Paste : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_content_paste_24)

        override val title: String = "Paste"
        override val description: String = "Pastes whatever is in the clipboard."
    }

    data object Settings : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_settings_24)

        override val title: String = "FlickBoard Settings"
        override val description: String = "Opens FlickBoard's settings app."
    }

    data class SwitchLetterLayer(val direction: SearchDirection) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_24)

        override val title: String = "${direction.nextOrPrevInTitle} Letter Layout"
        override val description: String =
            "Switches to the ${direction.nextOrPrevInSentence} letter layout, if more than one is enabled."
    }

    data object SwitchSystemKeyboard : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_keyboard_24)

        override val title: String = "Switch Keyboard App"
        override val description: String =
            "Opens the system keyboard picker, letting you switch to another keyboard app."
    }

    /**
     * Toggles which layer is "active"
     *
     * Intent: "I want easy access to the opposite layer with my current hand"
     */
    data object ToggleActiveLayer : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_flip_camera_android_24)

        override val title: String = "Toggle Layers"
        override val description: String =
            "Toggles between the letter and number modes. Numbers can also be typed by holding the matching letter.\n" +
                    "When using a single-sided layout, it toggles between the letter and number layers.\n" +
                    "When using a double-sided layout, it flips the sides."

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

        override val title: String = "Toggle Handedness"
        override val description: String = "Toggles between right-handed and left-handed mode."
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

        override val title: String = "Toggle Numbers Layer"
        override val description: String = "Toggles between showing and hiding the numbers layer."
    }

    data class AdjustCellHeight(val amount: Float) : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual = when {
            amount >= 0 -> ActionVisual.Icon(R.drawable.baseline_zoom_in_24)
            else -> ActionVisual.Icon(R.drawable.baseline_zoom_out_24)
        }

        override val title: String = run {
            val direction = when {
                amount >= 0 -> "Increase"
                else -> "Decrease"
            }
            "$direction Keyboard Size"
        }
        override val description: String = run {
            val direction = when {
                amount >= 0 -> "bigger"
                else -> "smaller"
            }
            "Makes the keyboard slightly $direction."
        }
    }

    data object SelectAll : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_select_all_24)

        override val title: String = "Select All"
        override val description: String = "Selects all text in the field."
    }

    data object ToggleEmojiMode : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_emoji_emotions_24)

        override val title: String = "Emoji"
        override val description: String = "Opens the emoji picker."
    }

    data object ToggleShowSymbols : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.None

        override val title: String = "Toggle Show Symbols"
        override val description: String = "Toggles whether or not symbols are shown."

        override fun shift(locale: Locale): Action = ToggleShowLetters
    }

    data object ToggleShowLetters : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.None

        override val title: String = "Toggle Show Letters"
        override val description: String = "Toggles whether or not letters are shown."
    }

    data object EnableVoiceMode : Action() {
        override fun visual(modifier: ModifierState?): ActionVisual =
            ActionVisual.Icon(R.drawable.baseline_mic_24)

        override val title: String = "Voice"
        override val description: String = "Switches to the voice keyboard, if any is installed."
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

enum class TextBoundary(val nameInTitle: String, val nameInSentence: String) {
    Character(nameInTitle = "Character", nameInSentence = "character"),
    Word(nameInTitle = "Word", nameInSentence = "word"),
    Line(nameInTitle = "Line", nameInSentence = "line");

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

enum class SearchDirection(
    val factor: Int,
    val nextOrPrevInTitle: String,
    val nextOrPrevInSentence: String
) {
    Backwards(factor = -1, nextOrPrevInTitle = "Previous", nextOrPrevInSentence = "previous"),
    Forwards(factor = 1, nextOrPrevInTitle = "Next", nextOrPrevInSentence = "next"),
}

enum class CaseChangeDirection(val nameInTitle: String, val nameInSentence: String) {
    Up(nameInTitle = "Up", nameInSentence = "up"),
    Down(nameInTitle = "Down", nameInSentence = "down");
}

enum class Direction {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

    fun title(): String = when (this) {
        TOP_LEFT -> "Northwest"
        TOP -> "North"
        TOP_RIGHT -> "Northeast"
        LEFT -> "West"
        CENTER -> "Tap"
        RIGHT -> "East"
        BOTTOM_LEFT -> "Southwest"
        BOTTOM -> "South"
        BOTTOM_RIGHT -> "Southeast"
    }

    fun angleFromTop(): Int = when (this) {
        TOP -> 0
        TOP_RIGHT -> 45
        RIGHT -> 90
        BOTTOM_RIGHT -> 135
        BOTTOM -> 180
        BOTTOM_LEFT -> 225
        LEFT -> 270
        TOP_LEFT -> 315
        CENTER -> 0
    }

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
        val Tap =
            Flick(
                direction = Direction.CENTER,
                longHold = false, longSwipe = false, shift = false
            )
    }
}

enum class CircleDirection {
    Clockwise,
    CounterClockwise,
}
