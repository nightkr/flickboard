package se.nullable.flickboard

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import se.nullable.flickboard.model.Direction
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

const val PiF: Float = PI.toFloat()

operator fun Int.times(cond: Boolean): Int = when {
    cond -> this
    else -> 0
}

// Not an operator, because it seems like extension methods
// (including operators) can't be inlined over the extended object?
inline fun neg(crossinline f: (Int) -> Int): (Int) -> Int = { -f(it) }

operator fun Float.times(cond: Boolean): Float = when {
    cond -> this
    else -> 0F
}

operator fun Offset.div(size: IntSize): Offset = Offset(
    x = x / size.width,
    y = y / size.height,
)

fun Offset.angle(): Float = atan2(y, x)

fun Offset.direction(): Direction {
    val slice = (angle() * 4 / Math.PI)
        .roundToInt()
        .mod(8)
    return when (slice) {
        0 -> Direction.RIGHT
        1 -> Direction.BOTTOM_RIGHT
        2 -> Direction.BOTTOM
        3 -> Direction.BOTTOM_LEFT
        4 -> Direction.LEFT
        5 -> Direction.TOP_LEFT
        6 -> Direction.TOP
        7 -> Direction.TOP_RIGHT
        else -> throw RuntimeException("Offset has invalid direction slice=$slice (angle=${angle()}")
    }
}

inline fun <T> List<T>.averageOf(f: (T) -> Float): Float =
    (sumOf { f(it).toDouble() } / size).toFloat()
