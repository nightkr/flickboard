package se.nullable.flickboard.model

data class Layout(
    val mainLayer: Layer,
    val numericLayer: Layer? = null,
    val controlLayer: Layer? = null
)

data class Layer(val keyRows: List<List<KeyM>>) {
    fun mergeFallback(fallback: Layer): Layer =
        copy(keyRows = keyRows.zip(fallback.keyRows) { thisRow, fallbackRow ->
            thisRow.zip(fallbackRow) { thisKey, fallbackKey ->
                thisKey.mergeFallback(fallbackKey)
            }
        })

    fun chain(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow + otherRow
        })
}

data class KeyM(val actions: Map<Direction, Action>, val colspan: Int = 1) {
    fun mergeFallback(fallback: KeyM): KeyM = copy(actions = fallback.actions + actions)
}

sealed class Action {
    abstract val label: String

    data class Text(val character: String, override val label: String = character) : Action()

    data object Backspace : Action() {
        override val label: String = "BKSPC"
    }

    data object Enter : Action() {
        override val label: String = "ENTER"
    }

    data class Jump(val amount: Int, override val label: String) : Action()
}

enum class Direction {
    TOP_LEFT, TOP, TOP_RIGHT,
    LEFT, CENTER, RIGHT,
    BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
}