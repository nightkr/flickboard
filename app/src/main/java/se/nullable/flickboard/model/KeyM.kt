package se.nullable.flickboard.model

data class Layout(
    val mainLayer: Layer,
    val numericLayer: Layer? = null,
    val controlLayer: Layer? = null
)

data class Layer(val keyRows: List<List<KeyM>>) {
    fun merge(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow.zip(otherRow) { thisKey, otherKey ->
                thisKey + otherKey
            }
        })

    fun chain(other: Layer): Layer =
        copy(keyRows = keyRows.zip(other.keyRows) { thisRow, otherRow ->
            thisRow + otherRow
        })
}

data class KeyM(val actions: Map<Direction, Action>) {
    operator fun plus(other: KeyM): KeyM = copy(actions = actions + other.actions)
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