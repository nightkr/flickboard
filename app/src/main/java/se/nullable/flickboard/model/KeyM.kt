package se.nullable.flickboard.model

data class Layout(
    val mainLayer: Layer,
    val shiftLayer: Layer = mainLayer.autoShift(),
    val numericLayer: Layer? = null,
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
        zipKeys(shift) { thisKey, shiftKey -> thisKey.copy(shift = shiftKey) }

    fun chain(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow + otherRow
        })

    fun autoShift(): Layer =
        copy(keyRows = keyRows.map { row -> row.map { key -> key.autoShift() } })
}

data class KeyM(
    val actions: Map<Direction, Action>,
    val colspan: Int = 1,
    val fallback: KeyM? = null,
    val shift: KeyM? = null
) {
    fun mergeFallback(fallback: KeyM): KeyM = copy(
        actions = fallback.actions + actions,
        fallback = this.fallback ?: fallback
    )

    fun autoShift(): KeyM = copy(actions = actions.mapValues { it.value.shift() })
}

sealed class Action {
    abstract val label: String
    open fun shift(): Action = this

    data class Text(val character: String, override val label: String = character) : Action() {
        override fun shift(): Action {
            return copy(character = character.uppercase(), label = label.uppercase())
        }
    }

    data class Delete(
        val direction: SearchDirection = SearchDirection.Backwards,
        val boundary: TextBoundary = TextBoundary.Letter,
    ) : Action() {
        override val label: String = "BKSPC"

        override fun shift(): Action = copy(boundary = TextBoundary.Word)
    }

    data object Enter : Action() {
        override val label: String = "ENTER"
    }

    data class Jump(
        val direction: SearchDirection,
        val boundary: TextBoundary = TextBoundary.Letter,
        override val label: String
    ) : Action() {
        override fun shift(): Action = copy(boundary = TextBoundary.Word)
    }

    data class Shift(val state: ShiftState) : Action() {
        override val label: String = "SHIFT"

        override fun shift(): Action {
            return when (state) {
                ShiftState.Shift -> copy(state = ShiftState.CapsLock)
                else -> return this
            }
        }
    }

    data object Cut : Action() {
        override val label: String = "CUT"
    }

    data object Copy : Action() {
        override val label: String = "COPY"
    }

    data object Paste : Action() {
        override val label: String = "PASTE"
    }
}

enum class TextBoundary {
    Letter,
    Word,
}

enum class SearchDirection {
    Backwards,
    Forwards,
}

enum class Direction {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
}

data class Gesture(val direction: Direction, val forceFallback: Boolean, val shift: Boolean)