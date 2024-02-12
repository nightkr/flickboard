package se.nullable.flickboard.util

import java.text.BreakIterator
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.absoluteValue

/**
 * Searches a string for hard linebreaks
 *
 * As opposed to [BreakIterator.getLineInstance], which returns "soft" linebreaks, where breaks
 * would be allowed to be inserted.
 */
class HardLineBreakIterator : BreakIterator() {
    private var iter: CharacterIterator = StringCharacterIterator("")
    override fun getText(): CharacterIterator = iter
    override fun setText(newText: CharacterIterator) {
        newText.first()
        iter = newText
    }

    private val newlineChar = '\n'
    private var previousRequestWasReverse = false
    private var previousRequestPosition = Position.Inside

    private enum class Position(val isFinal: Boolean) {
        First(isFinal = true),
        Last(isFinal = true),
        Inside(isFinal = false);
    }

    override fun first(): Int {
        iter.index = 0
        previousRequestPosition = Position.First
        return iter.index
    }

    override fun last(): Int {
        iter.index = iter.endIndex
        previousRequestPosition = Position.Last
        return iter.index
    }

    override fun isBoundary(offset: Int): Boolean {
        this.iter.index = offset
        val boundary =
            offset == 0 || offset == this.iter.endIndex || this.iter.previous() == newlineChar
        if (offset != 0) {
            this.iter.next()
        }
        return boundary
    }

    override fun next(n: Int): Int {
        val isReverse = n < 0
        for (i in 0..<n.absoluteValue) {
            if (previousRequestWasReverse) {
                iter.previous()
                previousRequestWasReverse = false
            }
            if (isReverse) {
                iter.previous()
            }
            while (true) {
                val nextChar = when {
                    isReverse -> when (previousRequestPosition) {
                        Position.Last -> iter.current()
                        else -> iter.previous()
                    }

                    else -> {
                        val char = iter.current()
                        iter.next()
                        char
                    }
                }
                if (nextChar == CharacterIterator.DONE) {
                    // if the final character is a new line: return it, then return DONE after that
                    if (previousRequestPosition.isFinal) {
                        return DONE
                    } else {
                        previousRequestPosition = when {
                            isReverse -> Position.First
                            else -> Position.Last
                        }
                        break
                    }
                }
                previousRequestPosition = Position.Inside
                if (nextChar == newlineChar) {
                    if (isReverse) {
                        iter.next()
                        previousRequestWasReverse = true
                    }
                    break
                }
            }
        }
        return iter.index
    }

    override fun next(): Int = next(1)

    override fun previous(): Int = next(-1)

    override fun following(offset: Int): Int {
        iter.index = offset
        return next()
    }

    override fun preceding(offset: Int): Int {
        iter.index = offset
        return previous()
    }

    override fun current(): Int = iter.index
}