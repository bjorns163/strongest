package com.strongest.app.utils

import com.github.mikephil.charting.data.Entry
import java.util.TimeZone

const val DAY_MS = 24L * 60 * 60 * 1000

/**
 * Start of the local calendar day containing [timestamp]. Uses the timezone offset at the
 * timestamp itself so DST transitions stay correct, and matches how the WorkoutDao buckets
 * workouts by day.
 */
fun localDayStart(timestamp: Long): Long {
    val offset = TimeZone.getDefault().getOffset(timestamp)
    return ((timestamp + offset) / DAY_MS) * DAY_MS - offset
}

/** Number of calendar days between two day-aligned timestamps, inclusive. */
fun daySlotCount(startDay: Long, lastDay: Long): Int =
    ((localDayStart(lastDay) - localDayStart(startDay)) / DAY_MS + 1).toInt()

/**
 * Builds line-chart entries for a continuous day axis: each day from [startDay] that has a value
 * becomes an entry at its day index, so the chart can span every calendar day through [lastDay]
 * while the line still connects data points across days that have no value.
 */
fun dailyEntries(startDay: Long, lastDay: Long, valueForDay: (Long) -> Float?): List<Entry> =
    List(daySlotCount(startDay, lastDay)) { idx ->
        val day = localDayStart(startDay) + idx * DAY_MS
        valueForDay(day)?.let { Entry(idx.toFloat(), it) }
    }.filterNotNull()
