package com.strongest.app

import com.strongest.app.data.db.MuscleLastTrained
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.AppSettings
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.ui.progress.ProgressViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Regression: the Progress tab loads on demand rather than observing the database, and its
 * ViewModel survives bottom-nav tab switches. `refresh()` existed but had no callers, so recovery
 * and personal records kept whatever they held when the tab was first opened — a workout finished
 * afterwards only appeared once the app was restarted. The charts hid the problem because
 * touching the range selector reloads them through `loadRanged()`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressRecoveryRefreshTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: WorkoutRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() = runTest {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = mock(WorkoutRepository::class.java)
        settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settingsFlow).thenReturn(flowOf(AppSettings()))
        `when`(repository.getAllPersonalRecords()).thenReturn(emptyList())
        `when`(repository.getVolumeByDate(anyLong())).thenReturn(emptyList())
        `when`(repository.getMuscleVolume(anyLong())).thenReturn(emptyList())
        `when`(repository.getWorkoutsPerDay(anyLong())).thenReturn(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh picks up a workout finished after the tab was first opened`() = runTest(dispatcher) {
        // Nothing trained yet when the tab is first opened.
        `when`(repository.getMuscleLastTrained()).thenReturn(emptyList())

        val vm = ProgressViewModel(repository, settingsRepository)
        advanceUntilIdle()
        assertTrue(
            "expected no recovering muscles before any workout",
            vm.state.value.recoveringMuscles.isEmpty()
        )

        // The user finishes a chest workout, then returns to the Progress tab.
        `when`(repository.getMuscleLastTrained()).thenReturn(
            listOf(MuscleLastTrained(MuscleGroup.CHEST.name, System.currentTimeMillis()))
        )

        vm.refresh()
        advanceUntilIdle()

        val recovering = vm.state.value.recoveringMuscles
        assertEquals(1, recovering.size)
        assertEquals(MuscleGroup.CHEST, recovering.single().muscleGroup)
        assertTrue(
            "a just-finished workout should have hours left on the clock",
            recovering.single().hoursRemaining > 0
        )
    }

    @Test
    fun `changing the range alone does not refresh recovery`() = runTest(dispatcher) {
        `when`(repository.getMuscleLastTrained()).thenReturn(emptyList())

        val vm = ProgressViewModel(repository, settingsRepository)
        advanceUntilIdle()

        `when`(repository.getMuscleLastTrained()).thenReturn(
            listOf(MuscleLastTrained(MuscleGroup.CHEST.name, System.currentTimeMillis()))
        )

        // Recovery is range-independent by design, so the range selector must not be what
        // refreshes it — this is why the bug looked like "only the charts update".
        vm.setRange(com.strongest.app.ui.progress.ProgressRange.DAYS_90)
        advanceUntilIdle()
        assertTrue(vm.state.value.recoveringMuscles.isEmpty())

        // Only a full refresh brings it up to date.
        vm.refresh()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.recoveringMuscles.size)
    }
}
