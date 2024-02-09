package se.nullable.flickboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

val LocalDisplayLimits = staticCompositionLocalOf<DisplayLimits?> { null }

@Composable
fun ProvideDisplayLimits(limits: DisplayLimits? = null, content: @Composable () -> Unit) {
    val actualLimits = limits ?: run {
        val displayMetrics = LocalContext.current.resources.displayMetrics
        DisplayLimits(
            portraitWidth = (min(
                displayMetrics.widthPixels,
                displayMetrics.heightPixels
            ) / displayMetrics.density).dp
        )
    }
    CompositionLocalProvider(LocalDisplayLimits provides actualLimits) {
        content()
    }
}

data class DisplayLimits(val portraitWidth: Dp)