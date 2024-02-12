package se.nullable.flickboard.util

import org.junit.Assert
import org.junit.Test
import java.text.BreakIterator

class HardLineBreakIteratorTest {
    @Test
    fun shouldBreakCorrectly() {
        val testString = "aaaa\nbbbb\ncccc"
        val lineIterator = HardLineBreakIterator()
        lineIterator.setText(testString)

        // Random access
        Assert.assertEquals(0, lineIterator.preceding(2))
        Assert.assertEquals(5, lineIterator.following(2))
        Assert.assertEquals(true, lineIterator.isBoundary(0))
        Assert.assertEquals(false, lineIterator.isBoundary(1))
        Assert.assertEquals(false, lineIterator.isBoundary(2))
        Assert.assertEquals(false, lineIterator.isBoundary(3))
        Assert.assertEquals(false, lineIterator.isBoundary(4))
        Assert.assertEquals(true, lineIterator.isBoundary(5))
        Assert.assertEquals(true, lineIterator.isBoundary(14))

        // Walk ascending
        Assert.assertEquals(0, lineIterator.first())
        Assert.assertEquals(0, lineIterator.current())
        for (breakPos in listOf(5, 10, 14)) {
            Assert.assertEquals(breakPos, lineIterator.next())
            Assert.assertEquals(breakPos, lineIterator.current())
        }
        Assert.assertEquals(BreakIterator.DONE, lineIterator.next())
        Assert.assertEquals(testString.length, lineIterator.current())

        // Walk descending
        Assert.assertEquals(testString.length, lineIterator.last())
        Assert.assertEquals(testString.length, lineIterator.current())
        for (breakPos in listOf(10, 5, 0)) {
            Assert.assertEquals(breakPos, lineIterator.previous())
            Assert.assertEquals(breakPos, lineIterator.current())
        }
        Assert.assertEquals(BreakIterator.DONE, lineIterator.previous())
        Assert.assertEquals(0, lineIterator.current())
    }

    @Test
    fun shouldCountInitialNewline() {
        val lineIterator = HardLineBreakIterator()
        lineIterator.setText("\nasdf")

        Assert.assertEquals(1, lineIterator.next())
        Assert.assertEquals(5, lineIterator.next())
    }

    @Test
    fun shouldCountFinalNewline() {
        val lineIterator = HardLineBreakIterator()
        lineIterator.setText("asdf\n")

        Assert.assertEquals(5, lineIterator.next())
        Assert.assertEquals(5, lineIterator.next())
        Assert.assertEquals(BreakIterator.DONE, lineIterator.next())

        Assert.assertEquals(5, lineIterator.last())
        Assert.assertEquals(5, lineIterator.previous())
        Assert.assertEquals(0, lineIterator.previous())
    }
}