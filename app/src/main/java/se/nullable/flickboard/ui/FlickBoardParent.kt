package se.nullable.flickboard.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import se.nullable.flickboard.ui.theme.FlickBoardTheme
import se.nullable.flickboard.ui.util.Dollar1GestureLibraryProvider

@Composable
fun FlickBoardParent(prefs: SharedPreferences? = null, content: @Composable () -> Unit) {
    AppSettingsProvider(prefs) {
        Dollar1GestureLibraryProvider {
            FlickBoardTheme(content = content)
        }
    }
}