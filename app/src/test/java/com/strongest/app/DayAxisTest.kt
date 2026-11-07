package com.strongest.app

import com.strongest.app.utils.DAY_MS
import com.strongest.app.utils.addDays
import com.strongest.app.utils.dailyEntries
import com.strongest.app.utils.daySlotCount
import com.strongest.app.utils.localDayStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

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

    @Test
    fun dailyEntries_snapsSlotKeysAcrossDstTransition() {
        // Regression: a range crossing a DST spring-forward (23h day) drifts fixed-24h slot keys
        // by one hour, so the last slot no longer equals the true local midnight and the chart
        // silently drops data for that day (reported as "6m shows no data").
        val tz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
        try {
            val lastDay = Calendar.getInstance().apply {
                timeZone = TimeZone.getTimeZone("Europe/Amsterdam")
                set(2026, Calendar.AUGUST, 16, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            // 181 days back from Aug 16 2026 crosses the Mar 29 2026 spring-forward exactly once.
            val startDay = addDays(lastDay, -181)

            assertEquals(182, daySlotCount(startDay, lastDay))
            val values = mapOf(lastDay to 42f)
            val entries = dailyEntries(startDay, lastDay) { values[it] }

            assertEquals(1, entries.size)
            assertEquals(181f, entries[0].x, 0f)
            assertEquals(42f, entries[0].y, 0f)
        } finally {
            TimeZone.setDefault(tz)
        }
    }

    @Test
    fun dailyEntries_keepsTodayAcrossDstFallBack() {
        // Regression: after a fall-back (25h day), midnight-before + n*24h lands at 23:00 the day
        // before, so every later slot snapped to the previous day and today's data never showed
        // (30d/90d/6m charts empty in November).
        val tz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
        try {
            val lastDay = Calendar.getInstance().apply {
                set(2026, Calendar.NOVEMBER, 7, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            // 30 days ending Nov 7 2026 crosses the Oct 25 2026 fall-back once.
            val startDay = addDays(lastDay, -29)

            assertEquals(Calendar.getInstance().apply {
                set(2026, Calendar.OCTOBER, 9, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis, startDay)
            assertEquals(30, daySlotCount(startDay, lastDay))
            val entries = dailyEntries(startDay, lastDay) { if (it == lastDay) 42f else null }

            assertEquals(1, entries.size)
            assertEquals(29f, entries[0].x, 0f)
        } finally {
            TimeZone.setDefault(tz)
        }
    }

    @Test
    fun addDays_landsOnMidnightAcrossBothDstTransitions() {
        val tz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
        try {
            val start = Calendar.getInstance().apply {
                set(2026, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val cal = Calendar.getInstance()
            for (n in 0..364) {
                val day = addDays(start, n)
                cal.timeInMillis = day
                assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
                assertEquals(n + 1, cal.get(Calendar.DAY_OF_YEAR))
                assertEquals(start, addDays(day, -n))
                assertEquals(n + 1, daySlotCount(start, day))
            }
        } finally {
            TimeZone.setDefault(tz)
        }
    }
}
