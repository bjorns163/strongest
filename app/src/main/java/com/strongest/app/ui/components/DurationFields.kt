package com.strongest.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.strongest.app.utils.durationDigitsToSeconds
import com.strongest.app.utils.formatDuration
import com.strongest.app.utils.formatDurationDigits
import com.strongest.app.utils.secondsToDurationDigits

/**
 * Folds one keystroke into the digits behind a duration field.
 *
 * The text on screen carries colons the user never typed, so an edit is read as a change to the
 * digits alone. Deleting a colon leaves the digits identical while the text shrinks; that is the
 * one case where a backspace has to be applied by hand, otherwise the caret would sit forever on
 * a separator that reappears as fast as it is removed.
 */
internal fun applyDurationEdit(previousText: String, newText: String): String {
    val previousDigits = previousText.filter { it.isDigit() }
    val newDigits = newText.filter { it.isDigit() }
    return if (newDigits == previousDigits && newText.length < previousText.length) {
        newDigits.dropLast(1)
    } else {
        newDigits.takeLast(6)
    }
}

/**
 * A compact `mm:ss` field for the set table, where a column is only wide enough for a clock.
 *
 * Digits fill from the right the way a stopwatch takes them — "5" is five seconds, "130" a minute
 * and a half — so there is never a unit to guess at.
 *
 * Taking focus selects what is already there, matching the weight and reps fields beside it: the
 * first digit typed replaces the old duration outright instead of being appended to it, which is
 * what "tap the field and type the new rest" has to mean.
 */
@Composable
fun DurationTextField(
    totalSeconds: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var digits by remember { mutableStateOf(secondsToDurationDigits(totalSeconds)) }

    LaunchedEffect(totalSeconds, isFocused) {
        if (!isFocused) {
            digits = secondsToDurationDigits(totalSeconds)
        }
    }

    val displayText = when {
        isFocused -> formatDurationDigits(digits)
        totalSeconds > 0 -> formatDuration(totalSeconds)
        else -> ""
    }
    var textState by remember { mutableStateOf(TextFieldValue(displayText)) }

    LaunchedEffect(displayText) {
        if (textState.text != displayText) {
            textState = TextFieldValue(displayText, TextRange(displayText.length))
        }
    }

    BasicTextField(
        value = textState,
        onValueChange = { newText ->
            digits = applyDurationEdit(textState.text, newText.text)
            onValueChange(durationDigitsToSeconds(digits))
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .reopenKeyboardOnTap { isFocused }
            .onFocusChanged { focusState ->
                val gainedFocus = !isFocused && focusState.isFocused
                isFocused = focusState.isFocused
                if (gainedFocus) {
                    textState = textState.copy(selection = TextRange(0, textState.text.length))
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            color = if (isFocused || textState.text.isNotEmpty()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp, vertical = 4.dp)
                    .clickable {
                        if (!isFocused) {
                            focusRequester.requestFocus()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (textState.text.isEmpty()) {
                    Text(
                        text = "00:00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * The same masked clock entry as [DurationTextField], laid out as a full-width labelled field for
 * the settings dialogs, and selecting its contents on focus for the same reason.
 *
 * It takes focus as soon as it appears. Opening one of these dialogs is already the decision to
 * change the value, so the field arrives ready to be typed over rather than needing a tap first.
 */
@Composable
fun DurationInputField(
    totalSeconds: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Duration (mm:ss)"
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var digits by remember { mutableStateOf(secondsToDurationDigits(totalSeconds)) }
    val displayText = formatDurationDigits(digits)
    var textState by remember { mutableStateOf(TextFieldValue(displayText)) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(displayText) {
        if (textState.text != displayText) {
            textState = TextFieldValue(displayText, TextRange(displayText.length))
        }
    }

    OutlinedTextField(
        value = textState,
        onValueChange = { newText ->
            digits = applyDurationEdit(textState.text, newText.text)
            onValueChange(durationDigitsToSeconds(digits))
        },
        label = { Text(label) },
        placeholder = { Text("00:00") },
        singleLine = true,
        textStyle = LocalTextStyle.current,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .reopenKeyboardOnTap { isFocused }
            .onFocusChanged { focusState ->
                val gainedFocus = !isFocused && focusState.isFocused
                isFocused = focusState.isFocused
                if (gainedFocus) {
                    textState = textState.copy(selection = TextRange(0, textState.text.length))
                }
            }
    )
}
