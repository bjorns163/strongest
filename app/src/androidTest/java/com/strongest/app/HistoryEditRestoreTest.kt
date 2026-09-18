package com.strongest.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.strongest.app.data.db.StrongestDatabase
import com.strongest.app.data.model.*
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Editing a finished workout writes straight to the database; Cancel relies on
 * `restoreWorkout` putting every edit back — changed, added and deleted rows alike.
 */
@RunWith(AndroidJUnit4::class)
class HistoryEditRestoreTest {

    private lateinit var db: StrongestDatabase
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrongestDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = WorkoutRepository(
            db.exerciseDao(),
            db.routineDao(),
            db.workoutDao(),
            SettingsRepository(ApplicationProvider.getApplicationContext())
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun exercise(id: Long, name: String) = Exercise(
        id = id,
        name = name,
        muscleGroup = MuscleGroup.CHEST,
        equipment = Equipment.BARBELL,
        description = "",
        instructions = "",
        secondaryMuscles = emptyList(),
        imageUrl = "",
        isCustom = true,
        type = ExerciseType.COMPOUND
    )

    @Test
    fun restoreUndoesEveryKindOfEdit() = runBlocking {
        val exerciseDao = db.exerciseDao()
        val workoutDao = db.workoutDao()
        exerciseDao.insertExercise(exercise(100, "Bench Press"))
        exerciseDao.insertExercise(exercise(101, "Incline Press"))
        exerciseDao.upsertNote(ExerciseNote(exerciseId = 100, noteText = "Elbows in", updatedAt = 1L))

        workoutDao.insertWorkout(
            Workout(id = 700, workoutName = "Push", startTime = 1_000L, endTime = 2_000L, isOngoing = false)
        )
        workoutDao.insertWorkoutExercise(WorkoutExercise(id = 710, workoutId = 700, exerciseId = 100, orderIndex = 0))
        workoutDao.insertSet(SetLog(id = 720, workoutExerciseId = 710, setNumber = 1, weightKg = 100f, reps = 5, completedAt = 1_500L))
        workoutDao.insertSet(SetLog(id = 721, workoutExerciseId = 710, setNumber = 2, weightKg = 100f, reps = 5, completedAt = 1_600L))

        val snapshot = repository.snapshotWorkout(700)!!

        // Edit everything an edit can touch.
        workoutDao.updateWorkout(snapshot.workout.copy(startTime = 5_000L))
        workoutDao.updateSet(snapshot.sets.first().copy(weightKg = 140f))
        workoutDao.deleteSet(snapshot.sets.last())
        workoutDao.insertSet(SetLog(workoutExerciseId = 710, setNumber = 3, weightKg = 60f, reps = 12))
        val added = workoutDao.insertWorkoutExercise(WorkoutExercise(workoutId = 700, exerciseId = 101, orderIndex = 1))
        workoutDao.insertSet(SetLog(workoutExerciseId = added, setNumber = 1, weightKg = 80f, reps = 8))
        exerciseDao.upsertNote(ExerciseNote(exerciseId = 100, noteText = "Changed", updatedAt = 2L))
        exerciseDao.upsertNote(ExerciseNote(exerciseId = 101, noteText = "New note", updatedAt = 2L))
        assertNotEquals(snapshot, repository.snapshotWorkout(700))

        repository.restoreWorkout(snapshot)

        assertEquals(snapshot, repository.snapshotWorkout(700))
        assertNull(exerciseDao.getNote(101))
        assertEquals("Elbows in", exerciseDao.getNote(100)?.noteText)
    }
}
