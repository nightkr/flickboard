package se.nullable.flickboard.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import se.nullable.flickboard.ui.LocalAppSettings
import se.nullable.flickboard.util.toAccent
import se.nullable.flickboard.util.toAccentContainer
import se.nullable.flickboard.util.toOnAccentContainer
import se.nullable.flickboard.util.toTertiary

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

data class KeyboardTheme(
    val keySurfaceColour: Color,
    val keyIndicatorColour: Color,
    val activeKeyIndicatorColour: Color,
    val lastActionSurfaceColour: Color,
    val lastActionColour: Color,
)

val LocalKeyboardTheme = compositionLocalOf<KeyboardTheme> {
    error("Tried to use LocalKeyboardTheme without a FlickBoardTheme")
}

@Composable
fun FlickBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = when {
        // Dynamic colour is unavailable in previews, and makes them crash
        !view.isInEditMode && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        val settings = LocalAppSettings.current
        val keyColour = settings.keyColour.state
        val keyColourChroma = settings.keyColourChroma.state
        val toneMode = settings.keyColourTone.state
        val toneConfig = toneMode.value.config
        val visualFeedbackInvertColourScheme = settings.visualFeedbackInvertColourScheme.state
        val keySurfaceColour = keyColour.value?.toAccentContainer(
            chroma = keyColourChroma.value,
            toneConfig
        ) ?: colorScheme.primaryContainer
        val keyIndicatorColour = keyColour.value?.toOnAccentContainer(
            chroma = keyColourChroma.value,
            toneConfig
        ) ?: colorScheme.onPrimaryContainer
        CompositionLocalProvider(
            LocalKeyboardTheme provides (run {
                run {
                    KeyboardTheme(
                        keySurfaceColour = keySurfaceColour,
                        keyIndicatorColour = keyIndicatorColour,
                        activeKeyIndicatorColour = keyColour.value?.toAccent(
                            chroma = keyColourChroma.value,
                            toneConfig
                        ) ?: colorScheme.primary,
                        lastActionSurfaceColour = when {
                            visualFeedbackInvertColourScheme.value -> keyIndicatorColour
                            else -> keySurfaceColour
                        }.toTertiary(),
                        lastActionColour = when {
                            visualFeedbackInvertColourScheme.value -> keySurfaceColour
                            else -> keyIndicatorColour
                        }.toTertiary(),
                    )
                }
            }),
            content = content
        )
    }
}