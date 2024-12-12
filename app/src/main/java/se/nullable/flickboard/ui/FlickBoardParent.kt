package se.nullable.flickboard.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import se.nullable.flickboard.ui.theme.FlickBoardTheme

@Composable
fun FlickBoardParent(prefs: SharedPreferences? = null, content: @Composable () -> Unit) {
    AppSettingsProvider(prefs) {
        FlickBoardTheme(content = content)
    }
}