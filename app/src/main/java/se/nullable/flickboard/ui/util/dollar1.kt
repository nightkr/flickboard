package se.nullable.flickboard.ui.util

import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import se.nullable.flickboard.R
import se.nullable.flickboard.ui.GestureRecognizer
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.util.Boxed

// HACK: Boxing allows the variable to exist on platforms that do not provide the type
// (like the IDE previews)
val LocalDollar1GestureLibrary = compositionLocalOf<Boxed<GestureLibrary>?> {
    throw Exception("LocalDollar1GestureLibrary used outside of Dollar1GestureLibraryProvider")
}

@Composable
fun Dollar1GestureLibraryProvider(content: @Composable () -> Unit) {
    val gestureLibrary: Boxed<GestureLibrary>? = when {
        // android.gesture is not available in preview mode
        LocalView.current.isInEditMode -> null

        // No need to load the gesture library if nobody's using it
        LocalAppSettings.current.gestureRecognizer.state.value != GestureRecognizer.Dollar1 -> null

        else -> LocalContext.current.let { context ->
            remember(context) {
                GestureLibraries.fromRawResource(context, R.raw.gestures)
                    .also {
                        // GestureLibrary.ORIENTATION_STYLE_8
                        // required for recognizing 8 orientations
                        // of otherwise equivalent gestures
                        it.orientationStyle = 8
                        it.load()
                    }
                    .let(::Boxed)
            }
        }
    }
    CompositionLocalProvider(
        LocalDollar1GestureLibrary provides gestureLibrary,
        content = content,
    )
}