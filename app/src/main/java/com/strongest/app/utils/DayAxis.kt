package com.strongest.app.utils

import com.github.mikephil.charting.data.Entry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

const val DAY_MS = 24L * 60 * 60 * 1000

private fun localDate(timestamp: Long): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.startMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Start of the local calendar day containing [timestamp]. Every chart key and data bucket goes
 * through this (or [addDays]) so they always agree, including on DST transition days.
 */
fun localDayStart(timestamp: Long): Long = localDate(timestamp).startMillis()

/** Local midnight [days] calendar days after (or before, if negative) the day containing [day]. */
fun addDays(day: Long, days: Int): Long = localDate(day).plusDays(days.toLong()).startMillis()

/** Number of calendar days between two timestamps' days, inclusive. */
fun daySlotCount(startDay: Long, lastDay: Long): Int =
    (ChronoUnit.DAYS.between(localDate(startDay), localDate(lastDay)) + 1).toInt()

/**
 * Builds line-chart entries for a continuous day axis: each day from [startDay] that has a value
 * becomes an entry at its day index, so the chart can span every calendar day through [lastDay]
 * while the line still connects data points across days that have no value.
 */
fun dailyEntries(startDay: Long, lastDay: Long, valueForDay: (Long) -> Float?): List<Entry> =
    List(daySlotCount(startDay, lastDay)) { idx ->
        // Slot keys must equal the localDayStart() keys used for data lookup, or the chart
        // silently empties; addDays keeps them aligned across DST transitions.
        val day = addDays(startDay, idx)
        valueForDay(day)?.let { Entry(idx.toFloat(), it) }
    }.filterNotNull()
