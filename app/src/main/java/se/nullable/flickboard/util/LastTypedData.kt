package se.nullable.flickboard.util

import android.icu.lang.UCharacter
import android.icu.text.Normalizer2

data class LastTypedData(val codePoint: Int, val position: Int, val combiner: Combiner?) {
    data class Combiner(
        val original: String,
        val combinedReplacement: String,
        val baseCharLength: Int
    )

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
            if (combiningMarkCodePoint != null) {
                val composed = normalizer.composePair(this.codePoint, combiningMarkCodePoint)
                if (composed >= 0) {
                    return Combiner(
                        original = UCharacter.toString(codePoint) + nextChar,
                        combinedReplacement = UCharacter.toString(composed),
                        baseCharLength = UCharacter.charCount(codePoint),
                    )
                }
            }
        }
        return null
    }
}
