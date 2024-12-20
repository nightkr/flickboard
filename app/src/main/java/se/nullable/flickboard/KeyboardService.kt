package se.nullable.flickboard

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.EnabledLayers
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.ProvideDisplayLimits
import se.nullable.flickboard.ui.emoji.EmojiKeyboard
import se.nullable.flickboard.ui.theme.LocalKeyboardTheme
import se.nullable.flickboard.ui.util.sharePointerInput

class KeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

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

    private val currentInputSession = mutableStateOf<InputSession?>(null)

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentInputSession.value = InputSession(currentInputConnection, attribute)
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
                BoxWithConstraints(
                    Modifier.onSizeChanged { size ->
                        viewHeight = size.height
                    },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    // Android clips ongoing touch events at the border between the keyboard and app.
                    // As a workaround, we claim a transparent whole-screen box, and then
                    // use an inset to limit the content region (for touch input and app resizing)
                    // to the region we actually occupy.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(maxHeight),
                    )
                    Box(
                        Modifier.onSizeChanged { size ->
                            keyboardHeight = size.height
                        },
                    ) {
                        FlickBoardParent {
                            ProvideDisplayLimits {
                                val warningSnackbarHostState = remember { SnackbarHostState() }
                                val keyboardTheme = LocalKeyboardTheme.current

                                SideEffect {
                                    window.window?.let { window ->
                                        val insets = WindowCompat.getInsetsController(
                                            window,
                                            window.decorView,
                                        )
                                        insets.isAppearanceLightNavigationBars =
                                            !keyboardTheme.isDark
                                    }
                                }

                                val inputSession = currentInputSession.value!!
                                val onAction = inputSession.actionHandler(
                                    warningSnackbarHostState = warningSnackbarHostState,
                                    switchInputMethod = ::switchInputMethod,
                                )
                                Box {
                                    when {
                                        inputSession.emojiMode.value -> EmojiKeyboard(onAction = onAction)
                                        else -> {
                                            ConfiguredKeyboard(
                                                modifier = Modifier.fillMaxWidth(),
                                                onAction = onAction,
                                                onModifierStateUpdated = inputSession::updateModifierState,
                                                enterKeyLabel = inputSession.editorInfo?.actionLabel?.toString(),
                                                overrideEnabledLayers = when (inputSession.editorInfo?.let { it.inputType and InputType.TYPE_MASK_CLASS }) {
                                                    InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE -> EnabledLayers.Numbers
                                                    else -> null
                                                },
                                            )
                                            SnackbarHost(
                                                warningSnackbarHostState,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .sharePointerInput(),
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


    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        currentInputSession.value?.updateCursorAnchorInfo(cursorAnchorInfo)
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