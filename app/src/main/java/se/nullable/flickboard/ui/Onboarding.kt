package se.nullable.flickboard.ui

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import se.nullable.flickboard.KeyboardService

@Composable
fun OnboardingPrompt() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val inputManager = remember(context) { context.getSystemService<InputMethodManager>() }

    val keyboardsUpdated = remember { mutableStateOf(Unit, neverEqualPolicy()) }
    // ACTION_INPUT_METHOD_CHANGED is triggered when the user changes their active keyboard.
    BroadcastListener(IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED)) {
        keyboardsUpdated.value = Unit
    }
    // We have no actual event for when a keyboard is enabled or disabled, but we can assume that
    // enabling a keyboard requires the current activity to be paused. Thus, hopefully,
    // watching for RESUMED should give us a fighting chance of picking up the newly enabled keyboard.
    LaunchedEffect(true) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            keyboardsUpdated.value = Unit
        }
    }
    // Do a no-op read to update when the active keyboard is changed
    keyboardsUpdated.value

    val keyboardServiceId =
        remember(context) { ComponentName(context, KeyboardService::class.java) }
    if (inputManager != null &&
        LocalLifecycleOwner.current.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    ) {
        if (!inputManager.enabledInputMethodList.any { it.component == keyboardServiceId }) {
            OnboardingPromptEnable()
        } else if (
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ) != keyboardServiceId.flattenToString()
        ) {
            OnboardingPromptSelect(inputManager)
        }
    }
}

@Composable
fun OnboardingPromptEnable() {
    val context = LocalContext.current
    OnboardingPromptCard(title = "Keyboard not enabled", onClick = {
        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }) {
        Text("The keyboard must be enabled before it can be used. Tap here to enable it.")
        Text("You can still try it in the preview below.")
    }
}

@Composable
fun OnboardingPromptSelect(inputManager: InputMethodManager?) {
    OnboardingPromptCard(title = "Keyboard not selected", onClick = {
        inputManager?.showInputMethodPicker()
    }) {
        Text("The keyboard must be selected as you active input method before it can be used. Tap here to open the picker.")
    }
}

@Composable
@Preview
fun OnboardingPromptPreview() {
    FlickBoardParent {
        Column {
            OnboardingPromptEnable()
            OnboardingPromptSelect(null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPromptCard(title: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(Modifier.weight(1F)) {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                content()
            }
            Icon(
                Icons.Filled.ArrowForward, null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun BroadcastListener(intentFilter: IntentFilter, onBroadcast: (Intent) -> Unit) {
    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                onBroadcast(intent)
            }
        }
    }
    val context = LocalContext.current
    DisposableEffect(context, intentFilter) {
        val lastStickyIntent = context.registerReceiver(
            receiver, intentFilter,
            when {
                VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> Context.RECEIVER_NOT_EXPORTED
                else -> 0
            }
        )
        lastStickyIntent?.let(onBroadcast)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}