package se.nullable.flickboard.ui.voice

import android.view.inputmethod.InputMethodManager

fun getVoiceInputId(inputManager: InputMethodManager): String? =
    inputManager.enabledInputMethodList
        .find { inputMethodInfo ->
            (0..<inputMethodInfo.subtypeCount).any {
                inputMethodInfo.getSubtypeAt(it).mode == "voice"
            }
        }
        ?.id
