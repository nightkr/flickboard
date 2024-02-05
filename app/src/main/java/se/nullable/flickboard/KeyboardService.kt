package se.nullable.flickboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.layouts.SV_MESSAGEASE
import se.nullable.flickboard.ui.Keyboard
import se.nullable.flickboard.ui.theme.FlickBoardTheme

class KeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    private var cursor: CursorAnchorInfo? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentInputConnection.requestCursorUpdates(
            InputConnection.CURSOR_UPDATE_MONITOR,
            InputConnection.CURSOR_UPDATE_FILTER_EDITOR_BOUNDS
        )
    }

    override fun onCreateInputView(): View {
        window.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return ComposeView(this).also { view ->
            view.setContent {
                FlickBoardTheme {
                    Surface {
                        Keyboard(
                            layout = SV_MESSAGEASE, onAction = { action ->
                                when (action) {
                                    is Action.Text ->
                                        currentInputConnection.commitText(action.character, 1)

                                    is Action.Backspace ->
                                        currentInputConnection.deleteSurroundingText(1, 0)

                                    is Action.Enter -> {
                                        if (currentInputEditorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
                                            currentInputConnection.commitText("\n", 1)
                                        } else {
                                            currentInputConnection.performEditorAction(
                                                currentInputEditorInfo.actionId
                                            )
                                        }
                                    }

                                    is Action.Jump -> {
                                        val currentPos = cursor?.let {
                                            if (action.amount >= 0) {
                                                it.selectionStart
                                            } else {
                                                it.selectionEnd
                                            }
                                        } ?: 0
                                        currentInputConnection.setSelection(
                                            currentPos + action.amount,
                                            currentPos + action.amount
                                        )
                                    }
                                }
                            },
                            enterKeyLabel = currentInputEditorInfo.actionLabel?.toString()
                        )
                    }
                }
            }
        }
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        cursor = cursorAnchorInfo
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        super.onStartInputView(editorInfo, restarting)
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }
}