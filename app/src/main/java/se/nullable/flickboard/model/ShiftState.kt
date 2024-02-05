package se.nullable.flickboard.model

enum class ShiftState {
    Normal,
    Shift,
    CapsLock;

    val isShifted: Boolean
        get() = this != Normal

    fun next(): ShiftState = when (this) {
        Shift -> Normal
        else -> this
    }
}