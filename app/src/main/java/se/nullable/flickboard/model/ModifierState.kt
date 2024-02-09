package se.nullable.flickboard.model

data class ModifierState(
    val shift: ShiftState = ShiftState.Normal,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
) {
    fun next(): ModifierState = ModifierState(shift = shift.next())
}

enum class ShiftState {
    Normal,
    Shift,
    CapsLock;

    val isShift: Boolean
        get() = this == Shift
    val isCapsLock: Boolean
        get() = this == CapsLock

    val isShifted: Boolean
        get() = this != Normal

    fun next(): ShiftState = when (this) {
        Shift -> Normal
        else -> this
    }
}