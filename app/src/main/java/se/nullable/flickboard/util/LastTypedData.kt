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
        val combiningMark = nextChar.asCombiningMarkOrNull() ?: return null
        val combiningMarkCodePoint = combiningMark.singleCodePointOrNull() ?: return null
        val composed = normalizer.composePair(this.codePoint, combiningMarkCodePoint)
        return if (composed >= 0) {
            Combiner(
                original = UCharacter.toString(codePoint) + nextChar,
                combinedReplacement = UCharacter.toString(composed),
                baseCharLength = UCharacter.charCount(codePoint),
            )
        } else {
            null
        }
    }
}
