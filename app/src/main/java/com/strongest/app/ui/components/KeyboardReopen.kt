package com.strongest.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Brings the soft keyboard back when an already-focused field is tapped.
 *
 * Dismissing the keyboard with back or the gesture bar does not clear focus, so the next tap on
 * that same field changes nothing: it is already focused, so there is no focus event to open the
 * keyboard on, and the field sits there uneditable until you tap a different one and come back.
 *
 * The tap is observed on the initial pass and never consumed, so the field's own handling — moving
 * the caret, selecting on focus — is untouched; this only asks for the keyboard alongside it.
 */
@Composable
fun Modifier.reopenKeyboardOnTap(isFocused: () -> Boolean): Modifier {
    val keyboardController = LocalSoftwareKeyboardController.current
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (isFocused()) {
                keyboardController?.show()
            }
        }
    }
}
