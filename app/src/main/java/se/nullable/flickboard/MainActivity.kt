package se.nullable.flickboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.nullable.flickboard.model.Action
import se.nullable.flickboard.ui.ConfiguredKeyboard
import se.nullable.flickboard.ui.FlickBoardParent
import se.nullable.flickboard.ui.Settings

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlickBoardParent {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        Column {
                            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
                            Settings(Modifier.weight(1F))
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                                Column {
                                    Text(
                                        text = "Preview keyboard",
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    ConfiguredKeyboard(onAction = { action ->
                                        val message = when {
                                            action is Action.Text -> action.character
                                            else -> action.toString()
                                        }
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    })
                                }
                            }
                        }
                        SnackbarHost(
                            snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}