package se.nullable.flickboard

import android.content.ComponentName
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.model.CaseChangeDirection
import se.nullable.flickboard.model.FastActionType
import se.nullable.flickboard.model.ModifierState
import se.nullable.flickboard.model.SearchDirection
import se.nullable.flickboard.model.TextBoundary
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.EnabledLayers
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.ui.LocalDisplayLimits
import se.nullable.flickboard.ui.ProvideDisplayLimits
import se.nullable.flickboard.ui.emoji.EmojiKeyboard
import se.nullable.flickboard.ui.voice.getVoiceInputId
import se.nullable.flickboard.util.AndroidKeycodeMapper
import se.nullable.flickboard.util.Boxed
import se.nullable.flickboard.util.LastTypedData
import se.nullable.flickboard.util.asCombiningMarkOrNull
import se.nullable.flickboard.util.singleCodePointOrNull
import sharePointerInput
import java.text.BreakIterator
import kotlin.math.max

class KeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    val keycodeMapper = AndroidKeycodeMapper()

    // Not available in all apps, use selection (or, preferably, the helpers)
    // instead of accessing directly
    private var cursor: CursorAnchorInfo? = null

    var gestureBeginPosition: Int = 0
    private fun selection(): IntRange? = when (val currentCursor = cursor) {
        null -> {
            // Some apps (such as Firefox) don't always support requestCursorUpdates,
            // in those cases, fall back to making a synchronous getExtractedText request instead.
            // getExtractedText can still fail for non-standard text editors that don't use a typical
            // "selection" at all (like terminals).
            currentInputConnection.getExtractedText(ExtractedTextRequest().also {
                it.hintMaxChars = 1
                it.hintMaxLines = 1
            }, 0)?.let { it.selectionStart..it.selectionEnd }
        }

        else -> currentCursor.selectionStart..currentCursor.selectionEnd
    }

    private var activeModifiers = ModifierState()

    enum class SelectionSize {
        Empty,
        NonEmpty,
        BufferUnavailable,
    }

    private fun selectionSize(): SelectionSize =
        selection().let { selection ->
            when {
                selection == null -> SelectionSize.BufferUnavailable
                selection.first == selection.last -> SelectionSize.Empty
                else -> SelectionSize.NonEmpty
            }
        }

    // Returns null if the input buffer is unavailable
    private fun currentCursorPosition(direction: SearchDirection): Int? =
        selection().let { selection ->
            when (direction) {
                SearchDirection.Backwards -> selection?.first
                SearchDirection.Forwards -> selection?.last
            }
        }

    private fun sendKeyPressEvents(keyCode: Int, extraModifiers: Int = 0) {
        listOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP).forEach { keyAction ->
            currentInputConnection.sendKeyEvent(
                KeyEvent(
                    0,
                    0,
                    keyAction,
                    keyCode,
                    0,
                    activeModifiers.androidMetaState or extraModifiers,
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

    // Tracked variant of currentInputEditorInfo
    private val editorInfo = mutableStateOf<EditorInfo?>(null)

    private var viewHeight: Int = 0 // Set once view is measured
    private var keyboardHeight: Int = 0 // Set once view is measured
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        // Invisible area "occupied by" the view
        outInsets.contentTopInsets = viewHeight - keyboardHeight
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT
    }

    // The inset hack breaks fullscreen, and trying to adjust based on it causes glitches
    // in some apps depending on circumstances (flickering in YouTube comments,
    // Discord force-hiding the keyboard, etc).
    // For now, we just accept the tradeoff and disable fullscreen entirely.
    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        editorInfo.value = attribute
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
                BoxWithConstraints(Modifier.onSizeChanged { size ->
                    viewHeight = size.height
                }, contentAlignment = Alignment.BottomCenter) {
                    // Android clips ongoing touch events at the border between the keyboard and app.
                    // As a workaround, we claim a transparent whole-screen box, and then
                    // use an inset to limit the content region (for touch input and app resizing)
                    // to the region we actually occupy.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(maxHeight)
                    )
                    Box(Modifier.onSizeChanged { size ->
                        keyboardHeight = size.height
                    }) {
                        FlickBoardParent {
                            ProvideDisplayLimits {
                                var emojiMode by remember { mutableStateOf(false) }
                                val warningMessageScope = rememberCoroutineScope()
                                val warningSnackbarHostState = remember { SnackbarHostState() }
                                val appSettings = LocalAppSettings.current
                                val periodOnDoubleSpace = appSettings.periodOnDoubleSpace.state
                                val disabledDeadkeys = appSettings.disabledDeadkeys.state
                                val displayLimits = LocalDisplayLimits.current
                                val onAction: (Action) -> Boolean = { action ->
                                    warningMessageScope.launch {
                                        warningSnackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                    fun typeText(
                                        text: String,
                                        forceRawKeyEvent: Boolean = false,
                                        forceShift: Boolean = false
                                    ) {
                                        var char = text
                                        val rawKeyCode = when {
                                            forceRawKeyEvent || activeModifiers.useRawKeyEvent -> char.singleOrNull()
                                                ?.let(keycodeMapper::keycodeForChar)
                                                ?: KeyEvent.KEYCODE_UNKNOWN

                                            else -> KeyEvent.KEYCODE_UNKNOWN
                                        }
                                        when {
                                            rawKeyCode != KeyEvent.KEYCODE_UNKNOWN ->
                                                sendKeyPressEvents(
                                                    rawKeyCode, extraModifiers = when {
                                                        forceShift -> KeyEvent.normalizeMetaState(
                                                            KeyEvent.META_SHIFT_LEFT_ON
                                                        )

                                                        else -> 0
                                                    }
                                                )

                                            else -> {
                                                val combiner = when {
                                                    activeModifiers.zalgo ->
                                                        lastTyped?.tryCombineWith(
                                                            char,
                                                            periodOnDoubleSpace = false,
                                                            tryHarder = true
                                                        ) ?: char.asCombiningMarkOrNull()
                                                            ?.let {
                                                                LastTypedData.Combiner(
                                                                    original = char,
                                                                    combinedReplacement = it,
                                                                    baseCharLength = 0
                                                                )
                                                            }

                                                    disabledDeadkeys.value.contains(char) -> null

                                                    else -> lastTyped?.tryCombineWith(
                                                        char,
                                                        periodOnDoubleSpace = periodOnDoubleSpace.value,
                                                    )
                                                }
                                                if (combiner != null) {
                                                    char = combiner.combinedReplacement
                                                }
                                                val codePoint = char.singleCodePointOrNull()
                                                val positionOfChar =
                                                    currentCursorPosition(SearchDirection.Backwards)
                                                        ?.plus(char.length)
                                                        ?.minus(combiner?.baseCharLength ?: 0)
                                                lastTyped = when {
                                                    positionOfChar != null -> LastTypedData(
                                                        codePoint = codePoint,
                                                        position = positionOfChar,
                                                        combiner = combiner
                                                    )

                                                    else -> null
                                                }
                                                currentInputConnection.beginBatchEdit()
                                                if (combiner != null) {
                                                    currentInputConnection.deleteSurroundingText(
                                                        combiner.baseCharLength,
                                                        0
                                                    )
                                                }
                                                currentInputConnection.commitText(
                                                    char,
                                                    1
                                                )
                                                currentInputConnection.endBatchEdit()
                                            }
                                        }
                                    }

                                    fun moveSelection(
                                        direction: SearchDirection,
                                        boundary: TextBoundary
                                    ): Boolean {
                                        val range = selection() ?: return false
                                        val movingSide = when {
                                            range.first == range.last -> {
                                                gestureBeginPosition = range.first
                                                direction
                                            }

                                            range.last == gestureBeginPosition -> SearchDirection.Backwards
                                            else -> SearchDirection.Forwards
                                        }
                                        val distance = findBoundary(
                                            boundary,
                                            direction,
                                            coalesce = true,
                                            includeSelection = movingSide != direction,
                                        ) * direction.factor
                                        currentInputConnection.setSelection(
                                            (range.first + distance * (movingSide == SearchDirection.Backwards))
                                                .coerceAtMost(range.last),
                                            (range.last + distance * (movingSide == SearchDirection.Forwards))
                                                .coerceAtLeast(range.first),
                                        )
                                        return true
                                    }

                                    var actionSuccessful = true
                                    when (action) {
                                        is Action.Text -> typeText(
                                            action.character,
                                            forceRawKeyEvent = action.forceRawKeyEvent
                                        )

                                        is Action.BeginFastAction -> {
                                            if (!activeModifiers.select) {
                                                gestureBeginPosition =
                                                    currentCursorPosition(direction = SearchDirection.Backwards)
                                                        ?: 0
                                            }
                                        }

                                        is Action.FastActionDone -> when (action.type) {
                                            FastActionType.Delete -> {
                                                currentInputConnection.commitText("", 0)
                                            }
                                        }

                                        is Action.FastDelete -> {
                                            // Let user select text, which is then deleted when FastActionDone is fired
                                            actionSuccessful =
                                                moveSelection(action.direction, action.boundary)
                                        }

                                        is Action.Delete -> {
                                            when {
                                                activeModifiers.useRawKeyEvent -> sendKeyPressEvents(
                                                    when (action.direction) {
                                                        SearchDirection.Backwards -> KeyEvent.KEYCODE_DEL
                                                        SearchDirection.Forwards -> KeyEvent.KEYCODE_FORWARD_DEL
                                                    }
                                                )

                                                else -> when (selectionSize()) {
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
                                                                lastTypedComposed.combinedReplacement.length,
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
                                        }

                                        is Action.Enter -> {
                                            val editorInfo = editorInfo.value
                                            val imeOptions = editorInfo?.imeOptions ?: 0
                                            if (activeModifiers.useRawKeyEvent ||
                                                activeModifiers.shift.isShift ||
                                                action.shift ||
                                                imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0
                                            ) {
                                                typeText(
                                                    "\n",
                                                    forceRawKeyEvent = activeModifiers.shift.isShift || action.shift,
                                                    forceShift = action.shift
                                                )
                                            } else {
                                                currentInputConnection.performEditorAction(
                                                    when {
                                                        editorInfo?.actionLabel != null -> editorInfo.actionId
                                                        else -> imeOptions and
                                                                (EditorInfo.IME_ACTION_DONE or
                                                                        EditorInfo.IME_ACTION_GO or
                                                                        EditorInfo.IME_ACTION_NEXT or
                                                                        EditorInfo.IME_ACTION_SEARCH or
                                                                        EditorInfo.IME_ACTION_SEND)
                                                    }
                                                )
                                            }
                                        }

                                        Action.Escape -> sendKeyPressEvents(KeyEvent.KEYCODE_ESCAPE)

                                        is Action.Jump -> {
                                            when {
                                                activeModifiers.select -> actionSuccessful =
                                                    moveSelection(action.direction, action.boundary)

                                                else -> {
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
                                            }
                                        }

                                        is Action.JumpLineKeepPos -> {
                                            // Yes, this is a horror beyond comprehension.
                                            // Yes, this should really be the editor's responsibility...
                                            // How is this different from Action.Jump? Action.Jump jumps to *the boundary*,
                                            // for TextBoundary.Line this is equivalent to Home/End.
                                            val currentPos = when {
                                                action.rawEvent -> null
                                                else -> currentCursorPosition(action.direction)
                                            }
                                            when (currentPos) {
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

                                        is Action.ToggleWordCase -> {
                                            selection()?.let { currentSelection ->
                                                currentInputConnection.beginBatchEdit()

                                                var prefixLength = 0
                                                var suffixLength = 0
                                                val word = when {
                                                    currentSelection.first == currentSelection.last -> {
                                                        val posInWord = findBoundary(
                                                            TextBoundary.Word,
                                                            SearchDirection.Backwards
                                                        )
                                                        val textAfterInWord = findBoundary(
                                                            TextBoundary.Word,
                                                            SearchDirection.Forwards
                                                        )

                                                        val prefix =
                                                            currentInputConnection.getTextBeforeCursor(
                                                                posInWord,
                                                                0
                                                            )?.takeUnless { it.isBlank() }
                                                                ?.toString()
                                                                ?: ""
                                                        val suffix =
                                                            currentInputConnection.getTextAfterCursor(
                                                                textAfterInWord,
                                                                0
                                                            )?.takeUnless { it.isBlank() }
                                                                ?.toString()
                                                                ?: ""
                                                        prefixLength = prefix.length
                                                        suffixLength = suffix.length
                                                        prefix + suffix
                                                    }

                                                    else -> currentInputConnection.getSelectedText(0)
                                                        .toString()
                                                }

                                                fun replaceWord(newWord: String) {
                                                    currentInputConnection.deleteSurroundingText(
                                                        prefixLength,
                                                        suffixLength
                                                    )
                                                    currentInputConnection.commitText(newWord, 1)
                                                }

                                                val lowercase = word.lowercase(action.locale)
                                                val titlecase =
                                                    lowercase.replaceFirstChar { it.titlecase(action.locale) }
                                                val uppercase = word.uppercase(action.locale)
                                                when (action.direction) {
                                                    CaseChangeDirection.Up -> when (word) {
                                                        uppercase -> {}
                                                        lowercase -> replaceWord(titlecase)
                                                        else -> replaceWord(uppercase)
                                                    }

                                                    CaseChangeDirection.Down -> when (word) {
                                                        lowercase -> {}
                                                        titlecase -> replaceWord(lowercase)
                                                        else -> replaceWord(titlecase)
                                                    }
                                                }

                                                currentInputConnection.setSelection(
                                                    currentSelection.first,
                                                    currentSelection.last
                                                )
                                                currentInputConnection.endBatchEdit()

                                            }
                                        }

                                        is Action.ToggleShift, Action.ToggleCtrl, Action.ToggleAlt, Action.ToggleZalgo -> {
                                            // handled internally in Keyboard
                                        }

                                        Action.ToggleSelect -> gestureBeginPosition =
                                            currentCursorPosition(direction = SearchDirection.Backwards)
                                                ?: 0

                                        Action.Copy -> {
                                            if (selectionSize() == SelectionSize.Empty) {
                                                currentInputConnection.performContextMenuAction(
                                                    android.R.id.selectAll
                                                )
                                            }
                                            currentInputConnection.performContextMenuAction(
                                                android.R.id.copy
                                            )
                                        }

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
                                                    this@KeyboardService,
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

                                        is Action.SwitchSystemKeyboard -> {
                                            getSystemService<InputMethodManager>()?.showInputMethodPicker()
                                        }

                                        Action.ToggleActiveLayer -> {
                                            var hasToggled = false

                                            appSettings.enabledLayersProjectionForOrientation(
                                                displayLimits
                                            ).tryModify { old ->
                                                if (old?.isSingleSided == true) {
                                                    old.toggleNumbers?.let {
                                                        hasToggled = true
                                                        Boxed(it)
                                                    }
                                                } else {
                                                    null
                                                }
                                            }

                                            if (!hasToggled) {
                                                appSettings.handedness.currentValue =
                                                    !appSettings.handedness.currentValue
                                            }
                                        }

                                        Action.ToggleHandedness -> {
                                            appSettings.handedness.currentValue =
                                                !appSettings.handedness.currentValue
                                            appSettings.portraitLocation.currentValue =
                                                -appSettings.portraitLocation.currentValue
                                            appSettings.landscapeLocation.currentValue =
                                                -appSettings.landscapeLocation.currentValue
                                        }

                                        Action.ToggleNumbersLayer -> {
                                            appSettings.enabledLayersProjectionForOrientation(
                                                displayLimits
                                            ).modify { old ->
                                                old?.toggleNumbers
                                            }
                                        }

                                        is Action.AdjustCellHeight ->
                                            appSettings.keyHeight.currentValue += action.amount

                                        Action.ToggleEmojiMode ->
                                            emojiMode = !emojiMode

                                        Action.ToggleShowSymbols ->
                                            appSettings.showSymbols.currentValue =
                                                !appSettings.showSymbols.currentValue

                                        Action.ToggleShowLetters ->
                                            appSettings.showLetters.currentValue =
                                                !appSettings.showLetters.currentValue

                                        Action.EnableVoiceMode -> {
                                            val inputManager =
                                                getSystemService<InputMethodManager>()
                                            val voiceMethodId = inputManager?.let(::getVoiceInputId)
                                            if (voiceMethodId != null) {
                                                // Only works on Android 9+ and when a compatible
                                                // voice keyboard is installed
                                                switchInputMethod(voiceMethodId)
                                            } else {
                                                warningMessageScope.launch {
                                                    warningSnackbarHostState
                                                        .showSnackbar("No voice input method found")
                                                }
                                            }
                                        }
                                    }
                                    actionSuccessful
                                }
                                Box {
                                    when {
                                        emojiMode -> EmojiKeyboard(onAction = onAction)
                                        else -> {
                                            ConfiguredKeyboard(
                                                modifier = Modifier.fillMaxWidth(),
                                                onAction = onAction,
                                                onModifierStateUpdated = { newModifiers ->
                                                    if (newModifiers != activeModifiers) {
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
                                                                newModifiers.androidMetaState,
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
                                                enterKeyLabel = editorInfo.value?.actionLabel?.toString(),
                                                overrideEnabledLayers = when (editorInfo.value?.let { it.inputType and InputType.TYPE_MASK_CLASS }) {
                                                    InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE -> EnabledLayers.Numbers
                                                    else -> null
                                                }
                                            )
                                            SnackbarHost(
                                                warningSnackbarHostState,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .sharePointerInput()
                                            )
                                        }
                                    }
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
        includeSelection: Boolean = false,
        endOfBufferOffset: Int = 0,
    ): Int {
        val searchBufferSize = when (boundary) {
            TextBoundary.Character -> 40
            else -> 1000
        }
        val selectedText = when {
            includeSelection -> currentInputConnection.getSelectedText(0) ?: ""
            else -> ""
        }
        val searchBuffer = when (direction) {
            SearchDirection.Backwards -> (currentInputConnection.getTextBeforeCursor(
                searchBufferSize,
                0
            ) ?: "").toString() + selectedText

            SearchDirection.Forwards -> selectedText.toString() + (currentInputConnection.getTextAfterCursor(
                searchBufferSize,
                0
            ) ?: "")
        }
        val breakIterator = boundary.breakIterator()
        breakIterator.setText(searchBuffer)
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
            if (next != BreakIterator.DONE
                && searchBuffer.getOrNull(max(pos, next))?.isWhitespace() != false
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