package se.nullable.flickboard.ui.voice

import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

@Composable
fun getAvailableVoiceInputMethod() : String? {
    val context = LocalContext.current
    val inputManager =
        remember(context) { context.getSystemService<InputMethodManager>() } ?: return null

    val candidateMethod = inputManager.enabledInputMethodList.find { meth -> inputMethodIsVoice(meth) } ?: return null
    return candidateMethod.id
}

@Composable
fun inputMethodIsVoice(inputMethod : InputMethodInfo) : Boolean {
    for (subtypeOffset in 0 until  inputMethod.subtypeCount) {
        if ("voice" ==  inputMethod.getSubtypeAt((subtypeOffset)).mode)
            return true
    }
    return false
}