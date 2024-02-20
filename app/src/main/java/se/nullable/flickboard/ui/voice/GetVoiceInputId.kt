package se.nullable.flickboard.ui.voice

import android.view.inputmethod.InputMethodManager

fun getVoiceInputId(inputManager : InputMethodManager) : String? {
    val candidateMethod = inputManager.enabledInputMethodList.find { inputMethodInfo ->
        (0..<inputMethodInfo.subtypeCount).any {
            "voice" == inputMethodInfo.getSubtypeAt(
                it
            ).mode
        }
    } ?: return null
    return candidateMethod.id
}
