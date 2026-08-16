package com.strongest.app

import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseClassification
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.AppSettings
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.ui.routines.RoutineBuilderViewModel
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
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoutineBuilderViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var repository: WorkoutRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        repository = mock(WorkoutRepository::class.java)
        settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settingsFlow).thenReturn(
            flowOf(AppSettings(workoutNotificationEnabled = false))
        )
        `when`(repository.getAllExercises()).thenReturn(flowOf(listOf(exercise(1L, "Exercise 1"))))
        `when`(repository.getAllRoutineGroups()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun exercise(id: Long, name: String) = Exercise(
        id = id,
        name = name,
        muscleGroup = MuscleGroup.CHEST,
        equipment = Equipment.BARBELL,
        classification = ExerciseClassification.COMPOUND
    )

    @Test
    fun `toggling warm-up updates the set type in state`() = runTest(dispatcher) {
        `when`(repository.getPreviousSessionSets(1L)).thenReturn(emptyList())
        `when`(repository.getNote(1L)).thenReturn(null)
        val vm = RoutineBuilderViewModel(repository, settingsRepository)
        advanceUntilIdle()

        vm.addExercise(1L)
        advanceUntilIdle()
        val routineExerciseId = vm.state.value.exercises.single().routineExerciseId
        assertEquals(SetType.NORMAL, vm.state.value.exercises.single().sets.first().setType)

        vm.toggleWarmUp(routineExerciseId, 0)
        assertEquals(SetType.WARM_UP, vm.state.value.exercises.single().sets.first().setType)

        vm.toggleWarmUp(routineExerciseId, 0)
        assertEquals(SetType.NORMAL, vm.state.value.exercises.single().sets.first().setType)
    }

    @Test
    fun `saving a routine persists the warm-up set type`() = runTest(dispatcher) {
        `when`(repository.getPreviousSessionSets(1L)).thenReturn(emptyList())
        `when`(repository.getNote(1L)).thenReturn(null)
        val vm = RoutineBuilderViewModel(repository, settingsRepository)
        advanceUntilIdle()

        vm.updateName("Leg Day")
        vm.addExercise(1L)
        advanceUntilIdle()
        vm.toggleWarmUp(vm.state.value.exercises.single().routineExerciseId, 0)

        vm.saveRoutine()
        advanceUntilIdle()

        @Suppress("UNCHECKED_CAST")
        var savedRoutineSets: Map<Long, List<RoutineSet>>? = null
        `when`(repository.saveRoutine(anyString(), anyString(), anyList(), anyMap(), isNull()))
            .thenAnswer { invocation ->
                savedRoutineSets = invocation.getArgument(3) as Map<Long, List<RoutineSet>>
                7L
            }

        vm.saveRoutine()
        advanceUntilIdle()

        assertTrue(
            "Saved routine sets should include the warm-up set type",
            savedRoutineSets!!.values.flatten().any { it.setType == SetType.WARM_UP }
        )
    }
}
