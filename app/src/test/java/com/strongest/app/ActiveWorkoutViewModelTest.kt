package com.strongest.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.strongest.app.data.db.RoutineWithExercisesAndSets
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.SetType
import com.strongest.app.data.model.Workout
import com.strongest.app.data.model.WorkoutExercise
import com.strongest.app.data.repository.AppSettings
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.data.repository.WorkoutWithDetails
import com.strongest.app.data.repository.WorkoutExerciseWithSets
import com.strongest.app.ui.navigation.WarmUpSetSpec
import com.strongest.app.ui.workout.ActiveWorkoutViewModel
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ActiveWorkoutViewModelTest {

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
        type = ExerciseType.COMPOUND
    )

    /**
     * Regression: deleting a set renumbered only the UI state, so the DB kept the pre-delete
     * numbers (1, 3, 4). Those resurfaced on reload, misaligned the previous-session hints
     * (looked up by setNumber) and carried the gaps into exports.
     */
    @Test
    fun `deleting a set persists the renumbering of the sets after it`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkoutWithThreeSets(vm)
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), vm.state.value.workoutExercises.single().sets.map { it.setNumber })

        // Drop the middle set.
        vm.deleteSet(workoutExerciseId = 10L, setIndex = 1)
        advanceUntilIdle()

        val sets = vm.state.value.workoutExercises.single().sets
        assertEquals(listOf(1000L, 1002L), sets.map { it.setId })
        assertEquals(listOf(1, 2), sets.map { it.setNumber })

        // The delete call itself is not argument-matched: deleteSet builds its SetLog without a
        // completedAt, so that field defaults to "now" and cannot be predicted. The renumbering
        // below is what this test is guarding anyway.
        // ...and the set that followed it is written back as set 2, not left as set 3.
        verify(repository).updateSet(
            SetLog(
                id = 1002L,
                workoutExerciseId = 10L,
                setNumber = 2,
                weightKg = 100f,
                reps = 4,
                rpe = null,
                setType = SetType.NORMAL,
                restSeconds = 150,
                completedAt = 0L
            )
        )
    }

    /** Stubs one ongoing exercise carrying three sets, so a middle delete has a tail to renumber. */
    private suspend fun seedWorkoutWithThreeSets(vm: ActiveWorkoutViewModel, workoutId: Long = 5L) {
        val workout = Workout(
            id = workoutId,
            routineId = null,
            routineName = null,
            workoutName = "Workout",
            startTime = 1000L,
            endTime = null,
            isOngoing = true
        )
        `when`(repository.getWorkoutById(workoutId)).thenReturn(workout)
        `when`(repository.getWorkoutWithDetails(workoutId)).thenReturn(
            WorkoutWithDetails(
                workout = workout,
                exercises = listOf(
                    WorkoutExerciseWithSets(
                        workoutExercise = WorkoutExercise(
                            id = 10L,
                            workoutId = workoutId,
                            exerciseId = 1L,
                            orderIndex = 0
                        ),
                        sets = listOf(
                            SetLog(1000L, 10L, 1, 80f, 8, null, SetType.NORMAL, 90, 0L),
                            SetLog(1001L, 10L, 2, 90f, 6, null, SetType.NORMAL, 90, 0L),
                            SetLog(1002L, 10L, 3, 100f, 4, null, SetType.NORMAL, 150, 0L)
                        )
                    )
                )
            )
        )
        `when`(repository.getExerciseById(1L)).thenReturn(exercise(1L, "Exercise 1"))
        `when`(repository.getPreviousSessionSets(1L)).thenReturn(emptyList())
        `when`(repository.getNote(1L)).thenReturn(null)

        vm.loadWorkout(workoutId)
    }

    /** Stubs an ongoing workout (pairs of exerciseId to workoutExerciseId) and loads it into the ViewModel. */
    private suspend fun seedWorkout(
        vm: ActiveWorkoutViewModel,
        workoutId: Long = 5L,
        routineId: Long? = null,
        pairs: List<Pair<Long, Long>>
    ) {
        val workout = Workout(
            id = workoutId,
            routineId = routineId,
            routineName = routineId?.let { "Routine $it" },
            workoutName = "Workout",
            startTime = 1000L,
            endTime = null,
            isOngoing = true
        )
        `when`(repository.getWorkoutById(workoutId)).thenReturn(workout)

        val details = WorkoutWithDetails(
            workout = workout,
            exercises = pairs.mapIndexed { idx, (exerciseId, workoutExerciseId) ->
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExercise(
                        id = workoutExerciseId,
                        workoutId = workoutId,
                        exerciseId = exerciseId,
                        orderIndex = idx
                    ),
                    sets = listOf(
                        SetLog(
                            id = workoutExerciseId * 100,
                            workoutExerciseId = workoutExerciseId,
                            setNumber = 1,
                            weightKg = 80f,
                            reps = 8,
                            setType = SetType.NORMAL,
                            restSeconds = 90,
                            completedAt = 0L
                        )
                    )
                )
            }
        )
        `when`(repository.getWorkoutWithDetails(workoutId)).thenReturn(details)

        pairs.forEach { (exerciseId, _) ->
            `when`(repository.getExerciseById(exerciseId))
                .thenReturn(exercise(exerciseId, "Exercise $exerciseId"))
            `when`(repository.getPreviousSessionSets(exerciseId)).thenReturn(emptyList())
            `when`(repository.getNote(exerciseId))
                .thenReturn(ExerciseNote(exerciseId = exerciseId, noteText = "Note for $exerciseId", updatedAt = 1000L))
        }
        `when`(repository.logSet(10L, 1, 0f, 0, null, SetType.NORMAL, 150, 0L)).thenReturn(42L)

        vm.loadWorkout(workoutId)
    }

    @Test
    fun `replacing an exercise loads the new exercise note`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, pairs = listOf(1L to 10L))
        advanceUntilIdle()
        assertEquals("Exercise 1", vm.state.value.workoutExercises.single().exerciseName)
        assertEquals("Note for 1", vm.state.value.workoutExercises.single().noteText)

        `when`(repository.getExerciseById(2L)).thenReturn(exercise(2L, "Bench Press"))
        `when`(repository.getPreviousSessionSets(2L)).thenReturn(emptyList())
        `when`(repository.getNote(2L))
            .thenReturn(ExerciseNote(exerciseId = 2L, noteText = "Keep elbows tucked", updatedAt = 2000L))

        vm.replaceExercise(10L, 2L)
        advanceUntilIdle()

        val replaced = vm.state.value.workoutExercises.single()
        assertEquals(2L, replaced.exerciseId)
        assertEquals("Bench Press", replaced.exerciseName)
        assertEquals("Keep elbows tucked", replaced.noteText)
    }

    @Test
    fun `replacing an exercise clears the note when the new exercise has none`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, pairs = listOf(1L to 10L))
        advanceUntilIdle()
        assertEquals("Exercise 1", vm.state.value.workoutExercises.single().exerciseName)
        assertEquals("Note for 1", vm.state.value.workoutExercises.single().noteText)

        `when`(repository.getExerciseById(2L)).thenReturn(exercise(2L, "Bench Press"))
        `when`(repository.getPreviousSessionSets(2L)).thenReturn(emptyList())
        `when`(repository.getNote(2L)).thenReturn(null)

        vm.replaceExercise(10L, 2L)
        advanceUntilIdle()

        val replaced = vm.state.value.workoutExercises.single()
        assertEquals(2L, replaced.exerciseId)
        assertEquals("", replaced.noteText)
    }

    @Test
    fun `update routine sets only when the workout still matches the routine order`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, routineId = 7L, pairs = listOf(1L to 10L, 2L to 11L))
        advanceUntilIdle()

        `when`(repository.getRoutineWithExercisesAndSets(7L)).thenReturn(
            RoutineWithExercisesAndSets(
                routine = Routine(id = 7L, name = "Routine 7"),
                exercises = listOf(
                    RoutineExercise(id = 70L, routineId = 7L, exerciseId = 1L, orderIndex = 0),
                    RoutineExercise(id = 71L, routineId = 7L, exerciseId = 2L, orderIndex = 1)
                ),
                sets = emptyMap()
            )
        )

        vm.updateRoutineSetsOnlyAndFinish()
        advanceUntilIdle()

        verify(repository).updateRoutineSetsOnly(eq(7L), anyList())
        verify(repository, never()).saveRoutineExercises(anyLong(), anyList(), anyMap())
    }

    @Test
    fun `toggling a set warm-up updates state and persists the set type`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, pairs = listOf(1L to 10L))
        advanceUntilIdle()
        assertEquals(SetType.NORMAL, vm.state.value.workoutExercises.single().sets.single().setType)

        vm.toggleWarmUp(10L, 0)
        advanceUntilIdle()

        assertEquals(SetType.WARM_UP, vm.state.value.workoutExercises.single().sets.single().setType)
        verify(repository).updateSet(
            SetLog(
                id = 1000L,
                workoutExerciseId = 10L,
                setNumber = 1,
                weightKg = 80f,
                reps = 8,
                rpe = null,
                setType = SetType.WARM_UP,
                restSeconds = 90,
                completedAt = 0L
            )
        )
    }

    @Test
    fun `toggling a set warm-up twice restores the normal type`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, pairs = listOf(1L to 10L))
        advanceUntilIdle()

        vm.toggleWarmUp(10L, 0)
        vm.toggleWarmUp(10L, 0)
        advanceUntilIdle()

        assertEquals(SetType.NORMAL, vm.state.value.workoutExercises.single().sets.single().setType)
    }

    @Test
    fun `starting a workout from a routine propagates warm-up set types`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        `when`(repository.startWorkout(eq(7L), eq("Routine 7"), anyString())).thenReturn(5L)
        `when`(repository.getRoutineWithExercisesAndSets(7L)).thenReturn(
            RoutineWithExercisesAndSets(
                routine = Routine(id = 7L, name = "Routine 7"),
                exercises = listOf(
                    RoutineExercise(id = 70L, routineId = 7L, exerciseId = 1L, orderIndex = 0)
                ),
                sets = mapOf(
                    70L to listOf(
                        RoutineSet(
                            id = 700L,
                            routineExerciseId = 70L,
                            setNumber = 1,
                            weight = 80f,
                            reps = 8,
                            restSeconds = 90,
                            setType = SetType.WARM_UP
                        )
                    )
                )
            )
        )
        `when`(repository.getExerciseById(1L)).thenReturn(exercise(1L, "Exercise 1"))
        `when`(repository.getPreviousSessionSets(1L)).thenReturn(emptyList())
        `when`(repository.addExerciseToWorkout(5L, 1L, 0)).thenReturn(10L)
        `when`(repository.getNote(1L)).thenReturn(null)
        `when`(repository.logSet(10L, 1, 80f, 8, null, SetType.WARM_UP, 150, 0L)).thenReturn(100L)

        vm.startWorkoutFromRoutine(7L)
        advanceUntilIdle()

        verify(repository).logSet(10L, 1, 80f, 8, null, SetType.WARM_UP, 150, 0L)
        assertEquals(SetType.WARM_UP, vm.state.value.workoutExercises.single().sets.single().setType)
    }

    @Test
    fun `reordered workout falls back to a full routine rebuild instead of position writes`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        // The workout order (2, 1) differs from the routine order (1, 2).
        seedWorkout(vm, routineId = 7L, pairs = listOf(2L to 11L, 1L to 10L))
        advanceUntilIdle()

        `when`(repository.getRoutineWithExercisesAndSets(7L)).thenReturn(
            RoutineWithExercisesAndSets(
                routine = Routine(id = 7L, name = "Routine 7"),
                exercises = listOf(
                    RoutineExercise(id = 70L, routineId = 7L, exerciseId = 1L, orderIndex = 0),
                    RoutineExercise(id = 71L, routineId = 7L, exerciseId = 2L, orderIndex = 1)
                ),
                sets = emptyMap()
            )
        )

        vm.updateRoutineSetsOnlyAndFinish()
        advanceUntilIdle()

        verify(repository, never()).updateRoutineSetsOnly(anyLong(), anyList())
        verify(repository).saveRoutineExercises(eq(7L), anyList(), anyMap())
    }

    @Test
    fun `adding warm-up sets prepends them and shifts existing set numbers`() = runTest(dispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = ActiveWorkoutViewModel(repository, settingsRepository, context)
        advanceUntilIdle()

        seedWorkout(vm, pairs = listOf(1L to 10L))
        advanceUntilIdle()
        val seeded = vm.state.value.workoutExercises.single().sets.single()
        assertEquals(SetType.NORMAL, seeded.setType)
        assertEquals(1, seeded.setNumber)

        `when`(repository.logSet(10L, 1, 40f, 8, null, SetType.WARM_UP, 90, 0L)).thenReturn(500L)
        `when`(repository.logSet(10L, 2, 56f, 5, null, SetType.WARM_UP, 90, 0L)).thenReturn(501L)

        vm.addWarmUpSets(
            10L,
            listOf(WarmUpSetSpec(40f, 8), WarmUpSetSpec(56f, 5))
        )
        advanceUntilIdle()

        val sets = vm.state.value.workoutExercises.single().sets
        assertEquals(3, sets.size)
        assertEquals(listOf(1, 2, 3), sets.map { it.setNumber })
        assertEquals(listOf(SetType.WARM_UP, SetType.WARM_UP, SetType.NORMAL), sets.map { it.setType })
        assertEquals(listOf(500L, 501L, 1000L), sets.map { it.setId })
        assertEquals(40f, sets[0].weight)
        assertEquals(8, sets[0].reps)
        assertEquals(56f, sets[1].weight)
        assertEquals(5, sets[1].reps)
        assertEquals(80f, sets[2].weight)
        assertEquals(8, sets[2].reps)

        verify(repository).updateSet(
            SetLog(
                id = 1000L,
                workoutExerciseId = 10L,
                setNumber = 3,
                weightKg = 80f,
                reps = 8,
                rpe = null,
                setType = SetType.NORMAL,
                restSeconds = 90,
                completedAt = 0L
            )
        )
    }
}
