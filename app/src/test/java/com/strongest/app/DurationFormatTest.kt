package com.strongest.app

import com.strongest.app.ui.components.applyDurationEdit
import com.strongest.app.utils.durationDigitsToSeconds
import com.strongest.app.utils.formatDuration
import com.strongest.app.utils.formatDurationDigits
import com.strongest.app.utils.parseDurationText
import com.strongest.app.utils.secondsToDurationDigits
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Rest timers and cardio set times are stored as a plain second count but written as a clock, so
 * the two directions have to agree: what a field shows must be what typing those digits produces.
 */
class DurationFormatTest {

    private lateinit var original: Locale

    @Before
    fun setUp() {
        original = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(original)
    }

    @Test
    fun `formats under an hour as zero-padded minutes and seconds`() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("00:05", formatDuration(5))
        assertEquals("01:30", formatDuration(90))
        assertEquals("02:30", formatDuration(150))
        assertEquals("59:59", formatDuration(3599))
    }

    @Test
    fun `widens to hours only once there are hours to show`() {
        assertEquals("1:00:00", formatDuration(3600))
        assertEquals("1:30:00", formatDuration(5400))
        assertEquals("2:05:09", formatDuration(7509))
    }

    @Test
    fun `formats the same in a comma-decimal locale`() {
        // The clock is punctuation, not a number: an nl-NL device must not see "01,30".
        Locale.setDefault(Locale.forLanguageTag("nl-NL"))
        assertEquals("01:30", formatDuration(90))
    }

    @Test
    fun `treats a negative duration as zero`() {
        assertEquals("00:00", formatDuration(-5))
        assertEquals("", secondsToDurationDigits(-5))
    }

    @Test
    fun `typed digits fill from the right`() {
        assertEquals(5, durationDigitsToSeconds("5"))
        assertEquals(45, durationDigitsToSeconds("45"))
        assertEquals(90, durationDigitsToSeconds("130"))
        assertEquals(150, durationDigitsToSeconds("230"))
        assertEquals(5400, durationDigitsToSeconds("13000"))
        assertEquals(0, durationDigitsToSeconds(""))
    }

    @Test
    fun `over-large segments carry instead of being rejected`() {
        // "90" is a stop on the way to "900"; it has to mean 90 seconds, not nothing.
        assertEquals(90, durationDigitsToSeconds("90"))
        assertEquals(5400, durationDigitsToSeconds("9000"))
    }

    @Test
    fun `digits past six are dropped from the left`() {
        assertEquals(durationDigitsToSeconds("123456"), durationDigitsToSeconds("9123456"))
    }

    @Test
    fun `partly typed digits keep their place without normalising`() {
        assertEquals("", formatDurationDigits(""))
        assertEquals("00:05", formatDurationDigits("5"))
        assertEquals("01:30", formatDurationDigits("130"))
        assertEquals("90:00", formatDurationDigits("9000"))
        assertEquals("1:30:00", formatDurationDigits("13000"))
    }

    @Test
    fun `a stored duration round-trips through its digits`() {
        for (seconds in listOf(0, 5, 45, 90, 150, 600, 3599, 3600, 5400, 7509)) {
            assertEquals(seconds, durationDigitsToSeconds(secondsToDurationDigits(seconds)))
            if (seconds > 0) {
                assertEquals(formatDuration(seconds), formatDurationDigits(secondsToDurationDigits(seconds)))
            }
        }
    }

    @Test
    fun `backspacing over a separator removes the digit behind it`() {
        // Deleting the colon of "01:30" leaves the digits untouched, so the edit has to be
        // applied by hand or the caret sticks on a separator that keeps reappearing.
        assertEquals("013", applyDurationEdit("01:30", "0130"))
        assertEquals("013", applyDurationEdit("01:30", "01:3"))
        assertEquals("01305", applyDurationEdit("01:30", "01:305"))
        assertEquals("", applyDurationEdit("", ""))
    }

    @Test
    fun `a backspace shifts the remaining digits right`() {
        // 01:30 backspaces to 00:13, the way a stopwatch entry does.
        val afterBackspace = applyDurationEdit("01:30", "01:3")
        assertEquals(13, durationDigitsToSeconds(afterBackspace))
        assertEquals("00:13", formatDurationDigits(afterBackspace))
    }

    @Test
    fun `a typed digit shifts the existing ones left`() {
        // 01:30 plus a "5" becomes 13:05.
        val afterDigit = applyDurationEdit("01:30", "01:305")
        assertEquals(785, durationDigitsToSeconds(afterDigit))
        assertEquals("13:05", formatDurationDigits(afterDigit))
    }

    @Test
    fun `typing over the selected contents replaces the duration`() {
        // Focus selects the whole field, so the first digit arrives as the entire new text and
        // has to start a fresh duration rather than extend the old one.
        val afterFirstDigit = applyDurationEdit("01:30", "2")
        assertEquals(2, durationDigitsToSeconds(afterFirstDigit))
        // The rest of "230" then fills in from the right, as it would in an empty field.
        val afterSecondDigit = applyDurationEdit("00:02", "00:023")
        assertEquals(23, durationDigitsToSeconds(afterSecondDigit))
        val afterThirdDigit = applyDurationEdit("00:23", "00:230")
        assertEquals(150, durationDigitsToSeconds(afterThirdDigit))
        assertEquals("02:30", formatDurationDigits(afterThirdDigit))
    }

    @Test
    fun `clearing the selected contents empties the field`() {
        assertEquals(0, durationDigitsToSeconds(applyDurationEdit("01:30", "")))
    }

    @Test
    fun `parses a duration typed as a clock or as a second count`() {
        assertEquals(150, parseDurationText("150"))
        assertEquals(150, parseDurationText("2:30"))
        assertEquals(5400, parseDurationText("1:30:00"))
        assertEquals(90, parseDurationText(" 1:30 "))
        assertNull(parseDurationText(""))
        assertNull(parseDurationText("abc"))
        assertNull(parseDurationText("1:2:3:4"))
    }
}
