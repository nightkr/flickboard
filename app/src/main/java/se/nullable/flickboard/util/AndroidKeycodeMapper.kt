package se.nullable.flickboard.util

import android.view.KeyCharacterMap
import android.view.KeyEvent

class AndroidKeycodeMapper {
    private val keycodesByChar =
        KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).let { charMap ->
            (0..KeyEvent.getMaxKeyCode())
                .associateBy { keyCode ->
                    (charMap.get(keyCode, 0) and KeyCharacterMap.COMBINING_ACCENT.inv())
                        .toChar().lowercaseChar()
                }
                .filterKeys { it != (0).toChar() }
        }

    fun keycodeForChar(char: Char): Int? = keycodesByChar[char.lowercaseChar()]
}