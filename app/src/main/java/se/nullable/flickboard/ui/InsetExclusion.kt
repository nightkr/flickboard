package se.nullable.flickboard.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.modifier.modifierLocalProvider
import androidx.compose.ui.platform.LocalDensity

private val LocalImeBottomInsetOffsetPx = modifierLocalOf {
    // Always overridden by consumeExcludedInsets when actually used, but
    // sometimes it seems to flicker out of existence...
    // Excluders should be mindful of this, and make sure to move their exclusion zone when
    // the MutableIntState changes.
    mutableIntStateOf(0)
}

/**
 * Allows layout children to consume insets from this node.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.consumeExcludedInsets(): Modifier {
    val bottomInsetOffsetPx = remember { mutableIntStateOf(0) }
    LaunchedEffect(bottomInsetOffsetPx.intValue) {
        println("ime bottom inset: ${bottomInsetOffsetPx.intValue}")
    }
    return this
        .modifierLocalProvider(LocalImeBottomInsetOffsetPx) { bottomInsetOffsetPx }
        .consumeWindowInsets(with(LocalDensity.current) {
            PaddingValues(
                bottom = bottomInsetOffsetPx.intValue.toDp()
            )
        })
}

/**
 * Exclude the height of this node from the closest ancestor [consumeExcludedInsets].
 *
 * Silently ignored if there is no ancestor [consumeExcludedInsets].
 *
 * Should only be called from nodes at the bottom of the display.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.excludeFromBottomInset(): Modifier {
    val globalInsetOffset: MutableState<MutableIntState?> = remember { mutableStateOf(null) }
    // Track the latest height, so that we only add the delta the next time the height changes.
    // Also allows us to "move" our reservation if the globalInsetOffset changes to a new MutableState.
    val selfHeight = remember { mutableIntStateOf(0) }
    DisposableEffect(globalInsetOffset) {
        val currentGlobalInsetOffset = globalInsetOffset.value
        currentGlobalInsetOffset?.let { it.intValue += selfHeight.intValue }
        onDispose {
            currentGlobalInsetOffset?.let { it.intValue -= selfHeight.intValue }
        }
    }
    return this
        .modifierLocalConsumer {
            globalInsetOffset.value = LocalImeBottomInsetOffsetPx.current
        }
        .onSizeChanged { newSize ->
            globalInsetOffset.value!!.intValue += newSize.height - selfHeight.intValue
            selfHeight.intValue = newSize.height
        }
}