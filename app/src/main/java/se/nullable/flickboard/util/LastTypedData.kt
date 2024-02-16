package se.nullable.flickboard.util

import android.icu.text.Normalizer2

data class LastTypedData(val codePoint: Int, val position: Int, val combiner: Combiner?) {
    data class Combiner(val original: String, val combiningMark: String)

    fun tryCombineWith(nextChar: String): Combiner? {
        val normalizer = Normalizer2.getNFKDInstance()
        val nextCodePoint = nextChar.singleCodePointOrNull()
        if (nextCodePoint != null) {
            val combiningMark = when (nextCodePoint) {
                // Some standalone diacritics are not considered composed of their composing variants...
                '`'.code -> "\u0300"
                '^'.code -> "\u0302"
                '~'.code -> "\u0303"
                else -> normalizer.getRawDecomposition(nextCodePoint)?.dropInitialSpace()
            }
            val combiningMarkCodePoint =
                combiningMark?.singleCodePointOrNull()
            if (combiningMarkCodePoint != null && normalizer.composePair(
                    this.codePoint,
                    combiningMarkCodePoint
                ) >= 0
            ) {
                return Combiner(
                    original = nextChar,
                    combiningMark = combiningMark,
                )
            }
        }
        return null
    }
}
