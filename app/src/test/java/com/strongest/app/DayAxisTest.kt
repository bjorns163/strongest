package com.strongest.app

import com.strongest.app.utils.DAY_MS
import com.strongest.app.utils.dailyEntries
import com.strongest.app.utils.daySlotCount
import com.strongest.app.utils.localDayStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DayAxisTest {

    @Test
    fun localDayStart_isIdempotentAndWithinSameDay() {
        val ts = 1_700_000_000_000L // Nov 14 2023, between DST transitions
        val day = localDayStart(ts)
        assertTrue(day <= ts)
        assertTrue(ts - day < DAY_MS)
        assertEquals(day, localDayStart(day))
    }

    @Test
    fun localDayStart_nextDayIsStrictlyLater() {
        val day = localDayStart(1_700_000_000_000L)
        val next = localDayStart(day + DAY_MS)
        assertTrue(next > day)
        assertTrue(next - day in DAY_MS - 3_600_000..DAY_MS + 3_600_000)
    }

    @Test
    fun daySlotCount_countsCalendarDaysInclusive() {
        val start = localDayStart(1_700_000_000_000L)
        assertEquals(1, daySlotCount(start, start))
        assertEquals(3, daySlotCount(start, localDayStart(start + 2 * DAY_MS)))
    }

    @Test
    fun dailyEntries_skipsMissingDaysButKeepsTrueDayPositions() {
        val start = localDayStart(1_700_000_000_000L)
        val last = localDayStart(start + 4 * DAY_MS)
        val values = mapOf(start to 5f, last to 9f)

        val entries = dailyEntries(start, last) { values[it] }

        assertEquals(2, entries.size)
        assertEquals(0f, entries[0].x, 0f)
        assertEquals(5f, entries[0].y, 0f)
        assertEquals(4f, entries[1].x, 0f)
        assertEquals(9f, entries[1].y, 0f)
    }

    @Test
    fun dailyEntries_mapsEverySlotToItsDay() {
        val start = localDayStart(1_700_000_000_000L)
        val last = localDayStart(start + 2 * DAY_MS)

        val entries = dailyEntries(start, last) { day -> (day - start).toFloat() }

        assertEquals(3, entries.size)
        entries.forEachIndexed { idx, e ->
            assertEquals(idx.toFloat(), e.x, 0f)
            assertTrue(e.y.isFinite())
        }
    }
}
