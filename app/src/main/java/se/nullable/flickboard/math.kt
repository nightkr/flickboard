package se.nullable.flickboard

import kotlin.math.PI

val PiF: Float = PI.toFloat()

operator fun Int.times(cond: Boolean): Int = when {
    cond -> this
    else -> 0
}

inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()
