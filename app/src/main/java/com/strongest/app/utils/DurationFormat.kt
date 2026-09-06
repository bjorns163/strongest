package com.strongest.app.utils

import java.util.Locale

/**
 * Clock formatting for the two durations the app stores as a plain second count: the rest timer on
 * every set, and the "reps" column of a cardio set, which is really a time on the machine.
 *
 * A bare number of seconds makes the user do the arithmetic ("is 150 two and a half minutes?"), so
 * everything on screen is written as a clock instead. Minutes are always zero-padded so the shape
 * of the value says what the format is; the hour segment only appears once it is non-zero, which
 * keeps the narrow table columns short for the rest timers that never reach it.
 */

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

/** Formats [totalSeconds] as `mm:ss`, widening to `h:mm:ss` from one hour up. */
fun formatDuration(totalSeconds: Int): String {
    val safe = if (totalSeconds < 0) 0 else totalSeconds
    val hours = safe / SECONDS_PER_HOUR
    val minutes = (safe % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = safe % SECONDS_PER_MINUTE
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/**
 * The digits behind [totalSeconds] as the duration fields hold them while being edited: `hhmmss`
 * with leading zeros stripped, so 150 seconds round-trips through "230".
 */
fun secondsToDurationDigits(totalSeconds: Int): String {
    val safe = if (totalSeconds < 0) 0 else totalSeconds
    if (safe == 0) return ""
    val hours = safe / SECONDS_PER_HOUR
    val minutes = (safe % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = safe % SECONDS_PER_MINUTE
    val padded = String.format(Locale.US, "%02d%02d%02d", hours, minutes, seconds)
    return padded.trimStart('0').ifEmpty { "0" }
}

/**
 * Reads the digits typed into a duration field, which fill from the right: "5" is five seconds,
 * "130" is a minute and a half, "13000" is an hour and a half.
 *
 * Over-large minute and second segments are carried rather than rejected, so typing "90" on the
 * way to "900" gives 90 seconds instead of nothing.
 */
fun durationDigitsToSeconds(digits: String): Int {
    val clean = digits.filter { it.isDigit() }.takeLast(6)
    if (clean.isEmpty()) return 0
    val padded = clean.padStart(6, '0')
    val hours = padded.substring(0, 2).toInt()
    val minutes = padded.substring(2, 4).toInt()
    val seconds = padded.substring(4, 6).toInt()
    return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds
}

/**
 * Lays the raw typed [digits] out as a clock without normalising them, so the value under the
 * cursor never jumps while a longer duration is still being typed.
 */
fun formatDurationDigits(digits: String): String {
    val raw = digits.filter { it.isDigit() }.takeLast(6)
    if (raw.isEmpty()) return ""
    // Leading zeros go before the layout, so the hour segment appears only once a digit has been
    // typed past the minutes rather than as a bare "0:" in front of them.
    val padded = raw.trimStart('0').padStart(4, '0')
    val seconds = padded.takeLast(2)
    val minutes = padded.dropLast(2).takeLast(2)
    val hours = padded.dropLast(4)
    return if (hours.isEmpty()) "$minutes:$seconds" else "$hours:$minutes:$seconds"
}

/**
 * Parses a duration a user typed freely, accepting both a clock ("2:30") and a bare second count
 * ("150"). Used where text arrives from outside the masked field. Returns null if unparseable.
 */
fun parseDurationText(text: String): Int? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    if (!trimmed.contains(':')) return trimmed.toIntOrNull()?.takeIf { it >= 0 }
    val parts = trimmed.split(':')
    if (parts.size > 3) return null
    val values = parts.map { part -> part.trim().toIntOrNull()?.takeIf { it >= 0 } ?: return null }
    return when (values.size) {
        2 -> values[0] * SECONDS_PER_MINUTE + values[1]
        else -> values[0] * SECONDS_PER_HOUR + values[1] * SECONDS_PER_MINUTE + values[2]
    }
}
