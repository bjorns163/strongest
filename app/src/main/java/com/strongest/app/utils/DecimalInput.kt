package com.strongest.app.utils

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
