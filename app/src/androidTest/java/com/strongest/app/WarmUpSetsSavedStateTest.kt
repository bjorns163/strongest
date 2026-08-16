package com.strongest.app

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.strongest.app.ui.navigation.AddWarmUpSetsRequest
import com.strongest.app.ui.navigation.WarmUpSetSpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression for the production crash "Can't put value with type class
 * AddWarmUpSetsRequest into saved state": the nav-result payload must be
 * Bundle-compatible (Serializable) so SavedStateHandle.set() accepts it.
 */
@RunWith(AndroidJUnit4::class)
class WarmUpSetsSavedStateTest {

    @Test
    fun addWarmUpSetsRequestRoundTripsThroughSavedStateHandle() {
        val request = AddWarmUpSetsRequest(
            workoutExerciseId = 7L,
            sets = listOf(WarmUpSetSpec(weightKg = 50f, reps = 8), WarmUpSetSpec(weightKg = 70f, reps = 5))
        )
        val handle = SavedStateHandle()
        handle.set("warm_up_sets_request", request)
        assertEquals(request, handle.get<AddWarmUpSetsRequest>("warm_up_sets_request"))
    }
}
