package se.nullable.flickboard

import android.content.ComponentName
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.ShiftState
import se.nullable.flickboard.model.TextBoundary
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.EnabledLayers
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.ui.ProvideDisplayLimits
import se.nullable.flickboard.ui.emoji.EmojiKeyboard
import se.nullable.flickboard.util.LastTypedData
import se.nullable.flickboard.util.singleCodePointOrNull
import java.text.BreakIterator
import kotlin.math.max
import kotlin.math.min

class KeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    // Not available in all apps, use the helpers instead of accessing directly
    private var cursor: CursorAnchorInfo? = null

    private var activeModifiers = ModifierState()

    enum class SelectionSize {
        Empty,
        NonEmpty,
        BufferUnavailable,
    }

    private fun selectionSize(): SelectionSize {
        fun fromIsNonEmpty(isNonEmpty: Boolean) = when {
            isNonEmpty -> SelectionSize.NonEmpty
            else -> SelectionSize.Empty
        }
        return when (val currentCursor = cursor) {
            null -> {
                // Some apps (such as Firefox) don't always support requestCursorUpdates,
                // in those cases, fall back to making a synchronous getExtractedText request instead.
                val extractedText =
                    currentInputConnection.getExtractedText(ExtractedTextRequest().also {
                        it.hintMaxChars = 1
                        it.hintMaxLines = 1
                    }, 0)
                extractedText?.let { fromIsNonEmpty(it.selectionStart != it.selectionEnd) }
                    ?: SelectionSize.BufferUnavailable
            }

            else -> fromIsNonEmpty(currentCursor.selectionStart != currentCursor.selectionEnd)
        }
    }

    // Returns null if the input buffer is unavailable
    private fun currentCursorPosition(direction: SearchDirection): Int? =
        when (val currentCursor = cursor) {
            null -> {
                // Some apps (such as Firefox) don't always support requestCursorUpdates,
                // in those cases, fall back to making a synchronous getExtractedText request instead.
                val extractedText =
                    currentInputConnection.getExtractedText(ExtractedTextRequest().also {
                        it.hintMaxChars = 1
                        it.hintMaxLines = 1
                    }, 0)
                extractedText?.let {
                    it.startOffset + when (direction) {
                        SearchDirection.Backwards -> it.selectionStart
                        SearchDirection.Forwards -> it.selectionEnd
                    }
                }
            }

            else -> when (direction) {
                SearchDirection.Backwards -> currentCursor.selectionStart
                SearchDirection.Forwards -> currentCursor.selectionEnd
            }
        }

    private fun sendKeyPressEvents(keyCode: Int) {
        listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP).forEach { keyAction ->
            currentInputConnection.sendKeyEvent(
                KeyEvent(
                    0,
                    0,
                    keyAction,
                    keyCode,
                    0,
                    0,
                    KeyCharacterMap.VIRTUAL_KEYBOARD,
                    0,
                    KeyEvent.FLAG_SOFT_KEYBOARD,
                )
            )
        }
    }

    private var lastTyped: LastTypedData? = null
        // The lastTyped is no longer relevant if the position doesn't match,
        // also cleared in onUpdateCursorAnchorInfo but we still want to double-check
        // as a fallback.
        get() = field?.takeIf { currentCursorPosition(SearchDirection.Backwards) == it.position }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        cursor = null
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
                    ProvideDisplayLimits {
                        var emojiMode by remember { mutableStateOf(false) }
                        val appSettings = LocalAppSettings.current
                        val onAction: (Action) -> Unit = { action ->
                            when (action) {
                                is Action.Text -> {
                                    var char = action.character
                                    val combiner = lastTyped?.tryCombineWith(char)
                                    if (combiner != null) {
                                        char = combiner.combiningMark
                                    }
                                    val codePoint = char.singleCodePointOrNull()
                                    val positionOfChar =
                                        currentCursorPosition(SearchDirection.Backwards)
                                            ?.plus(char.length)
                                    lastTyped = when {
                                        codePoint != null && positionOfChar != null ->
                                            LastTypedData(
                                                codePoint = codePoint,
                                                position = positionOfChar,
                                                combiner = combiner
                                            )

                                        else -> null
                                    }
                                    currentInputConnection.commitText(
                                        char,
                                        1
                                    )
                                }

                                is Action.Delete -> {
                                    when (selectionSize()) {
                                        SelectionSize.NonEmpty -> {
                                            // if selection is non-empty, delete it regardless of the mode requested by the user
                                            currentInputConnection.commitText("", 0)
                                        }

                                        SelectionSize.Empty -> {
                                            val length = findBoundary(
                                                action.boundary,
                                                action.direction,
                                                coalesce = true
                                            )
                                            val lastTypedComposed = lastTyped?.combiner
                                            if (action.direction == SearchDirection.Backwards && lastTypedComposed != null) {
                                                currentInputConnection.beginBatchEdit()
                                                currentInputConnection.deleteSurroundingText(
                                                    lastTypedComposed.combiningMark.length,
                                                    0
                                                )
                                                currentInputConnection.commitText(
                                                    lastTypedComposed.original,
                                                    1
                                                )
                                                currentInputConnection.endBatchEdit()
                                                // Clear last-typed data even if we're in a context without
                                                // a working requestCursorUpdates
                                                lastTyped = null
                                            } else {
                                                currentInputConnection.deleteSurroundingText(
                                                    if (action.direction == SearchDirection.Backwards) length else 0,
                                                    if (action.direction == SearchDirection.Forwards) length else 0,
                                                )
                                            }
                                        }

                                        SelectionSize.BufferUnavailable -> {
                                            sendKeyPressEvents(
                                                keyCode = when (action.direction) {
                                                    SearchDirection.Backwards -> KeyEvent.KEYCODE_DEL
                                                    SearchDirection.Forwards -> KeyEvent.KEYCODE_FORWARD_DEL
                                                }
                                            )
                                        }
                                    }
                                }

                                is Action.Enter -> {
                                    if (currentInputEditorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
                                        currentInputConnection.commitText("\n", 1)
                                    } else {
                                        currentInputConnection.performEditorAction(
                                            when {
                                                currentInputEditorInfo.actionLabel != null ->
                                                    currentInputEditorInfo.actionId

                                                else -> currentInputEditorInfo.imeOptions and
                                                        (EditorInfo.IME_ACTION_DONE or
                                                                EditorInfo.IME_ACTION_GO or
                                                                EditorInfo.IME_ACTION_NEXT or
                                                                EditorInfo.IME_ACTION_SEARCH or
                                                                EditorInfo.IME_ACTION_SEND)
                                            }
                                        )
                                    }
                                }

                                is Action.Jump -> {
                                    when (val currentPos =
                                        currentCursorPosition(action.direction)) {
                                        null -> sendKeyPressEvents(
                                            keyCode = when (action.direction) {
                                                SearchDirection.Backwards -> KeyEvent.KEYCODE_DPAD_LEFT
                                                SearchDirection.Forwards -> KeyEvent.KEYCODE_DPAD_RIGHT
                                            }
                                        )

                                        else -> {
                                            val newPos = currentPos + findBoundary(
                                                action.boundary,
                                                action.direction,
                                                coalesce = true,
                                            ) * action.direction.factor
                                            currentInputConnection.setSelection(
                                                newPos,
                                                newPos
                                            )
                                        }
                                    }
                                }

                                is Action.JumpLineKeepPos -> {
                                    // Yes, this is a horror beyond comprehension.
                                    // Yes, this should really be the editor's responsibility...
                                    // How is this different from Action.Jump? Action.Jump jumps to *the boundary*,
                                    // for TextBoundary.Line this is equivalent to Home/End.
                                    when (val currentPos =
                                        currentCursorPosition(action.direction)) {
                                        null -> sendKeyPressEvents(
                                            keyCode = when (action.direction) {
                                                SearchDirection.Backwards -> KeyEvent.KEYCODE_DPAD_UP
                                                SearchDirection.Forwards -> KeyEvent.KEYCODE_DPAD_DOWN
                                            }
                                        )

                                        else -> {
                                            // Find our position on the current line
                                            val posOnLine = findBoundary(
                                                TextBoundary.Line,
                                                SearchDirection.Backwards,
                                            )
                                            val lineSearchSkip = when (action.direction) {
                                                // When going backwards, we need to find the linebreak before the current one
                                                SearchDirection.Backwards -> 1
                                                SearchDirection.Forwards -> 0
                                            }
                                            // Find the offset to the target line
                                            val targetLineOffset = (findBoundary(
                                                TextBoundary.Line,
                                                action.direction,
                                                skipBoundaries = lineSearchSkip,
                                            )) * action.direction.factor
                                            // To reconstruct the position on the line, we also need to clamp
                                            // to the length of the new line, if it is shorter than the current one
                                            val targetLineLength = when (action.direction) {
                                                // When jumping backwards, we already know the length of the
                                                // line since we're jumping into it
                                                SearchDirection.Backwards -> -targetLineOffset - posOnLine - 1
                                                // When jumping forwards.. search for the next newline after the current one
                                                SearchDirection.Forwards -> findBoundary(
                                                    TextBoundary.Line,
                                                    action.direction,
                                                    skipBoundaries = 1,
                                                    endOfBufferOffset = 1,
                                                ) - targetLineOffset - 1
                                            }
                                            val newPos = (currentPos + targetLineOffset +
                                                    posOnLine.coerceAtMost(targetLineLength))
                                                .coerceAtLeast(0)
                                            currentInputConnection.setSelection(
                                                newPos,
                                                newPos
                                            )
                                        }
                                    }
                                }

                                is Action.ToggleShift, Action.ToggleCtrl, Action.ToggleAlt -> {
                                    // handled internally in Keyboard
                                }

                                Action.Copy ->
                                    currentInputConnection.performContextMenuAction(
                                        android.R.id.copy
                                    )

                                Action.Cut ->
                                    currentInputConnection.performContextMenuAction(
                                        android.R.id.cut
                                    )

                                Action.Paste ->
                                    currentInputConnection.performContextMenuAction(
                                        android.R.id.paste
                                    )

                                Action.SelectAll ->
                                    currentInputConnection.performContextMenuAction(
                                        android.R.id.selectAll
                                    )

                                Action.Settings -> startActivity(
                                    Intent.makeMainActivity(
                                        ComponentName(
                                            this,
                                            MainActivity::class.java
                                        )
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )

                                is Action.SwitchLetterLayer -> {
                                    appSettings.activeLetterLayerIndex.currentValue =
                                        (appSettings.activeLetterLayerIndex.currentValue
                                                + action.direction.factor)
                                            .mod(appSettings.letterLayers.currentValue.size)
                                }

                                Action.ToggleLayerOrder -> {
                                    when (appSettings.enabledLayers.currentValue) {
                                        EnabledLayers.Letters ->
                                            appSettings.enabledLayers.currentValue =
                                                EnabledLayers.Numbers

                                        EnabledLayers.Numbers ->
                                            appSettings.enabledLayers.currentValue =
                                                EnabledLayers.Letters

                                        // Both layers are enabled, so switch their sides by toggling handedness
                                        EnabledLayers.All, EnabledLayers.DoubleLetters,
                                        EnabledLayers.AllMiniNumbers, EnabledLayers.AllMiniNumbersMiddle ->
                                            appSettings.handedness.currentValue =
                                                !appSettings.handedness.currentValue
                                    }
                                }

                                is Action.AdjustCellHeight ->
                                    appSettings.keyHeight.currentValue += action.amount

                                Action.ToggleEmojiMode ->
                                    emojiMode = !emojiMode
                            }
                        }
                        Box {
                            when {
                                emojiMode -> EmojiKeyboard(onAction = onAction)
                                else -> {
                                    ConfiguredKeyboard(
                                        onAction = onAction,
                                        onModifierStateUpdated = { newModifiers ->
                                            if (newModifiers != activeModifiers) {
                                                var modifierMask = when (newModifiers.shift) {
                                                    ShiftState.Normal -> 0
                                                    ShiftState.Shift -> KeyEvent.META_SHIFT_LEFT_ON
                                                    ShiftState.CapsLock -> KeyEvent.META_CAPS_LOCK_ON
                                                }
                                                if (newModifiers.ctrl) {
                                                    modifierMask =
                                                        modifierMask or KeyEvent.META_CTRL_LEFT_ON
                                                }
                                                if (newModifiers.alt) {
                                                    modifierMask =
                                                        modifierMask or KeyEvent.META_ALT_LEFT_ON
                                                }
                                                fun newKeyEvent(
                                                    isDown: Boolean,
                                                    code: Int
                                                ): KeyEvent =
                                                    KeyEvent(
                                                        0,
                                                        0,
                                                        when {
                                                            isDown -> KeyEvent.ACTION_DOWN
                                                            else -> KeyEvent.ACTION_UP
                                                        },
                                                        code,
                                                        0,
//                                                0,
                                                        KeyEvent.normalizeMetaState(modifierMask),
                                                        KeyCharacterMap.VIRTUAL_KEYBOARD,
                                                        0,
                                                        KeyEvent.FLAG_SOFT_KEYBOARD,
                                                    )
                                                if (newModifiers.shift.isShift != activeModifiers.shift.isShift) {
                                                    currentInputConnection.sendKeyEvent(
                                                        newKeyEvent(
                                                            newModifiers.shift.isShift,
                                                            KeyEvent.KEYCODE_SHIFT_LEFT
                                                        )
                                                    )
                                                }
                                                if (newModifiers.shift.isCapsLock != activeModifiers.shift.isCapsLock) {
                                                    currentInputConnection.sendKeyEvent(
                                                        newKeyEvent(
                                                            newModifiers.shift.isCapsLock,
                                                            KeyEvent.KEYCODE_CAPS_LOCK
                                                        )
                                                    )
                                                }
                                                if (newModifiers.ctrl != activeModifiers.ctrl) {
                                                    currentInputConnection.sendKeyEvent(
                                                        newKeyEvent(
                                                            newModifiers.ctrl,
                                                            KeyEvent.KEYCODE_CTRL_LEFT
                                                        )
                                                    )
                                                }
                                                if (newModifiers.alt != activeModifiers.alt) {
                                                    currentInputConnection.sendKeyEvent(
                                                        newKeyEvent(
                                                            newModifiers.alt,
                                                            KeyEvent.KEYCODE_ALT_LEFT
                                                        )
                                                    )
                                                }
                                                activeModifiers = newModifiers
                                            }
                                        },
                                        enterKeyLabel = currentInputEditorInfo.actionLabel?.toString(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findBoundary(
        boundary: TextBoundary,
        direction: SearchDirection,
        skipBoundaries: Int = 0,
        coalesce: Boolean = false,
        endOfBufferOffset: Int = 0,
    ): Int {
        val searchBufferSize = when (boundary) {
            TextBoundary.Character -> 40
            else -> 1000
        }
        val searchBuffer = when (direction) {
            SearchDirection.Backwards -> currentInputConnection.getTextBeforeCursor(
                searchBufferSize,
                0
            )

            SearchDirection.Forwards -> currentInputConnection.getTextAfterCursor(
                searchBufferSize,
                0
            )
        } ?: ""
        val breakIterator = boundary.breakIterator()
        breakIterator.setText(searchBuffer.toString())
        if (direction == SearchDirection.Backwards) {
            breakIterator.last()
        }
        val steps = skipBoundaries + 1
        var pos = breakIterator.next(direction.factor * steps)
        if (pos == BreakIterator.DONE) {
            return searchBuffer.length + endOfBufferOffset
        }
        if (coalesce && boundary != TextBoundary.Character) {
            val next = breakIterator.next(direction.factor)
            if (next != BreakIterator.DONE &&
                searchBuffer.subSequence(min(pos, next), max(pos, next)).isBlank()
            ) {
                pos = next
            }
        }
        val posInDirection = when (direction) {
            SearchDirection.Backwards -> searchBuffer.length - pos
            SearchDirection.Forwards -> pos
        }
        return posInDirection
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        cursor = cursorAnchorInfo
        // Best-effort: drop lastTyped when the user moves the selection,
        // such that moving the cursor away and then back into position
        // will still clear it.
        if (cursorAnchorInfo?.selectionStart != lastTyped?.position) {
            lastTyped = null
        }
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