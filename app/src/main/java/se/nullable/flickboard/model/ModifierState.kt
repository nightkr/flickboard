package se.nullable.flickboard.model

import android.view.KeyEvent
import se.nullable.flickboard.times

data class ModifierState(
    val shift: ShiftState = ShiftState.Normal,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val zalgo: Boolean = false,
    val select: Boolean = false,
) {
    fun next(): ModifierState = ModifierState(shift = shift.next())

    val useRawKeyEvent = ctrl || alt

    val androidMetaState = KeyEvent.normalizeMetaState(
        when (shift) {
            ShiftState.Normal -> 0
            ShiftState.Shift -> KeyEvent.META_SHIFT_LEFT_ON
            ShiftState.CapsLock -> KeyEvent.META_CAPS_LOCK_ON
        }
                or (KeyEvent.META_CTRL_LEFT_ON * ctrl)
                or (KeyEvent.META_ALT_LEFT_ON * alt),
    )
}

enum class ShiftState {
    Normal,
    Shift,
    CapsLock;

    val isShift: Boolean
        get() = this == Shift
    val isCapsLock: Boolean
        get() = this == CapsLock

    fun next(): ShiftState = when (this) {
        Shift -> Normal
        else -> this
    }
}

