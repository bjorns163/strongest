package com.strongest.app

import com.strongest.app.ui.exercise.ExercisePickerResultHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression: backing out of a Replace left replace mode set, so the next "Add Exercise" opened
 * the picker as "Replace Exercise" and replaced an exercise instead of adding one.
 */
class ExercisePickerResultHolderTest {

    @Test
    fun `starting an add clears a replace that was abandoned`() {
        val holder = ExercisePickerResultHolder()
        holder.startReplace(42L)
        // The user backs out of the picker: no result is published.

        holder.startAdd()

        assertFalse(holder.isReplaceMode)
        assertNull(holder.replacingExerciseId)
    }

    @Test
    fun `starting a replace targets that exercise and drops a stale result`() {
        val holder = ExercisePickerResultHolder()
        holder.publish(listOf(1L))

        holder.startReplace(7L)

        assertTrue(holder.isReplaceMode)
        assertEquals(7L, holder.replacingExerciseId)
        assertNull(holder.result.value)
    }
}
