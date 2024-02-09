package se.nullable.flickboard

import androidx.compose.ui.unit.Dp
import kotlin.math.PI

val PiF: Float = PI.toFloat()

fun sqrt(x: Dp): Dp = Dp(kotlin.math.sqrt(x.value))

inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()
