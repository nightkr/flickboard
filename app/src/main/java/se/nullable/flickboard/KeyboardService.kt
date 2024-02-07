package se.nullable.flickboard

import android.content.ComponentName
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
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
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.TextBoundary
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Keyboard
import se.nullable.flickboard.ui.LocalAppSettings

class KeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    private var cursor: CursorAnchorInfo? = null

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        var cursorUpdatesRequested = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                currentInputConnection.requestCursorUpdates(
                    InputConnection.CURSOR_UPDATE_MONITOR,
                    InputConnection.CURSOR_UPDATE_FILTER_EDITOR_BOUNDS,
                )
        // Even on modern android, some apps only support unfiltered requestCursorUpdates,
        // so fall back to trying that.
        cursorUpdatesRequested = cursorUpdatesRequested ||
                currentInputConnection.requestCursorUpdates(
                    InputConnection.CURSOR_UPDATE_MONITOR,
                )
        if (!cursorUpdatesRequested) {
            println("no cursor data :(")
        }
    }

    override fun onCreateInputView(): View {
        window.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return ComposeView(this).also { view ->
            view.setContent {
                FlickBoardParent {
                    val appSettings = LocalAppSettings.current
                    Surface {
                        Keyboard(
                            layout = LocalAppSettings.current.layout, onAction = { action ->
                                when (action) {
                                    is Action.Text ->
                                        currentInputConnection.commitText(action.character, 1)

                                    is Action.Delete -> {
                                        if (cursor?.selectionStart != cursor?.selectionEnd) {
                                            // if selection is non-empty, delete it regardless of the mode requested by the user
                                            currentInputConnection.commitText("", 0)
                                        } else {
                                            val length =
                                                findBoundary(action.boundary, action.direction)
                                            currentInputConnection.deleteSurroundingText(
                                                if (action.direction == SearchDirection.Backwards) length else 0,
                                                if (action.direction == SearchDirection.Forwards) length else 0,
                                            )
                                        }
                                    }

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
                                            when (action.direction) {
                                                SearchDirection.Backwards -> it.selectionStart
                                                SearchDirection.Forwards -> it.selectionEnd
                                            }
                                        } ?: 0
                                        val newPos = currentPos + findBoundary(
                                            action.boundary,
                                            action.direction
                                        ) * when (action.direction) {
                                            SearchDirection.Backwards -> -1
                                            SearchDirection.Forwards -> 1
                                        }
                                        currentInputConnection.setSelection(newPos, newPos)
                                    }

                                    is Action.Shift -> {
                                        // handled internally in Keyboard
                                    }

                                    Action.Copy -> currentInputConnection.performContextMenuAction(
                                        android.R.id.copy
                                    )

                                    Action.Cut -> currentInputConnection.performContextMenuAction(
                                        android.R.id.cut
                                    )

                                    Action.Paste -> currentInputConnection.performContextMenuAction(
                                        android.R.id.paste
                                    )

                                    Action.Settings -> startActivity(
                                        Intent.makeMainActivity(
                                            ComponentName(this, MainActivity::class.java)
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )

                                    Action.ToggleLayerOrder -> {
                                        val handedness = appSettings.handedness
                                        handedness.currentValue = !handedness.currentValue
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

    private fun findBoundary(boundary: TextBoundary, direction: SearchDirection) = when (boundary) {
        TextBoundary.Letter -> 1
        TextBoundary.Word -> {
            val searchBufferSize = 1000
            val searchBuffer = when (direction) {
                SearchDirection.Backwards -> currentInputConnection.getTextBeforeCursor(
                    searchBufferSize,
                    0
                )?.reversed()

                SearchDirection.Forwards -> currentInputConnection.getTextAfterCursor(
                    searchBufferSize,
                    0
                )
            } ?: ""
            val initialSpaces =
                searchBuffer.takeWhile { it == ' ' }.length
            val wordBoundaryIndex =
                searchBuffer.indexOf(' ', initialSpaces)
                    .takeUnless { it == -1 }
            wordBoundaryIndex ?: searchBuffer.length
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