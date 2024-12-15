package se.nullable.flickboard.ui.theme

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import se.nullable.flickboard.neg

object Transition {
    // widthPx is unused, but leaving it to fit the signature and
    // enabling future experimenting with other values
    private fun widthTransform(widthPx: Int): Int = 100
    // private fun widthTransform(widthPx: Int): Int = widthPx + 20

    val pushEnter = slideInHorizontally(initialOffsetX = this::widthTransform) + fadeIn()
    val pushExit = slideOutHorizontally(targetOffsetX = neg(this::widthTransform)) + fadeOut()
    val push = pushEnter togetherWith pushExit

    val popEnter = slideInHorizontally(initialOffsetX = neg(this::widthTransform)) + fadeIn()
    val popExit = slideOutHorizontally(targetOffsetX = this::widthTransform) + fadeOut()
    val pop = popEnter togetherWith popExit
}