package com.strongest.app.utils

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Parses a number the user typed into a text field, accepting either decimal separator.
 *
 * Kotlin's [String.toFloatOrNull] only understands a dot, but a decimal keypad offers whatever
 * separator the device locale uses — a comma across most of Europe. Parsing such input with
 * [String.toFloatOrNull] alone returns null, which every caller here treats as "no change", so
 * the field silently keeps its old value while displaying the newly typed text.
 */
fun parseDecimalInput(text: String): Float? =
    text.trim().replace(',', '.').toFloatOrNull()

/**
 * Strips everything but digits and decimal separators from text typed into a decimal field.
 * Keeps the comma too: fields are prefilled with locale-formatted numbers ("72,5"), and dropping
 * the comma on the next keystroke would silently turn that into 725.
 */
fun filterDecimalInput(text: String): String =
    text.filter { it.isDigit() || it == '.' || it == ',' }

/**
 * Formats a number for display or for prefilling a decimal field: at most [maxDecimals]
 * decimals, trailing zeros dropped (2.5 not 2.50, 1.25 stays 1.25), locale decimal separator.
 */
fun formatDecimal(value: Float, maxDecimals: Int = 2): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = maxDecimals
        roundingMode = RoundingMode.HALF_UP
        isGroupingUsed = false
    }.format(value.toDouble())
