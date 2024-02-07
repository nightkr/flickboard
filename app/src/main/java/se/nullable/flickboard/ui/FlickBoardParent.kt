package se.nullable.flickboard.ui

import androidx.compose.runtime.Composable
import se.nullable.flickboard.ui.theme.FlickBoardTheme

@Composable
fun FlickBoardParent(content: @Composable () -> Unit) {
    AppSettingsProvider {
        FlickBoardTheme {
            content()
        }
    }
}