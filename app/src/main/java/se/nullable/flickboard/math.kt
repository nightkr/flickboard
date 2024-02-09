package se.nullable.flickboard

import androidx.compose.ui.unit.Dp

fun sqrt(x: Dp): Dp = Dp(kotlin.math.sqrt(x.value))

inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()
