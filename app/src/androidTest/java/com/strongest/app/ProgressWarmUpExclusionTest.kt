package com.strongest.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.strongest.app.data.db.StrongestDatabase
import com.strongest.app.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression: warm-up sets must NOT count toward Progress tab stats.
 *
 * Seeded data deliberately uses a HEAVY warm-up set (120kg) so that if the
 * warm-up leaked into any aggregation it would dominate the expected values.
 */
@RunWith(AndroidJUnit4::class)
class ProgressWarmUpExclusionTest {

    private lateinit var db: StrongestDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrongestDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun progressQueriesExcludeWarmUpSets() = runBlocking {
        val exerciseDao = db.exerciseDao()
        val workoutDao = db.workoutDao()

        val squat = Exercise(
            id = 100,
            name = "Squat",
            muscleGroup = MuscleGroup.QUADS,
            equipment = Equipment.BARBELL,
            description = "Full squat",
            instructions = "",
            secondaryMuscles = listOf(MuscleGroup.GLUTES),
            imageUrl = "",
            isCustom = true,
            classification = ExerciseClassification.COMPOUND
        )
        exerciseDao.insertExercise(squat)

        val bench = Exercise(
            id = 101,
            name = "Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BARBELL,
            description = "Bench press",
            instructions = "",
            secondaryMuscles = emptyList(),
            imageUrl = "",
            isCustom = true,
            classification = ExerciseClassification.COMPOUND
        )
        exerciseDao.insertExercise(bench)

        workoutDao.insertWorkout(
            Workout(
                id = 600,
                workoutName = "Leg day",
                startTime = 1000L,
                endTime = 3000L,
                isOngoing = false
            )
        )
        workoutDao.insertWorkoutExercise(
            WorkoutExercise(id = 700, workoutId = 600, exerciseId = 100, orderIndex = 1)
        )
        workoutDao.insertSet(
            SetLog(
                id = 800,
                workoutExerciseId = 700,
                setNumber = 1,
                weightKg = 120f,
                reps = 8,
                setType = SetType.WARM_UP,
                completedAt = 2500L
            )
        )
        workoutDao.insertSet(
            SetLog(
                id = 801,
                workoutExerciseId = 700,
                setNumber = 2,
                weightKg = 100f,
                reps = 5,
                setType = SetType.NORMAL,
                completedAt = 2600L
            )
        )
        workoutDao.insertSet(
            SetLog(
                id = 802,
                workoutExerciseId = 700,
                setNumber = 3,
                weightKg = 102.5f,
                reps = 3,
                setType = SetType.NORMAL,
                completedAt = 2700L
            )
        )

        workoutDao.insertWorkout(
            Workout(
                id = 601,
                workoutName = "Chest day",
                startTime = 2000L,
                endTime = 4000L,
                isOngoing = false
            )
        )
        workoutDao.insertWorkoutExercise(
            WorkoutExercise(id = 701, workoutId = 601, exerciseId = 101, orderIndex = 1)
        )
        workoutDao.insertSet(
            SetLog(
                id = 803,
                workoutExerciseId = 701,
                setNumber = 1,
                weightKg = 40f,
                reps = 8,
                setType = SetType.WARM_UP,
                completedAt = 3500L
            )
        )

        // Volume by date: only the 2 working sets count (100*5 + 102.5*3 = 807.5).
        val volume = workoutDao.getVolumeByDate(0L)
        assertEquals(1, volume.size)
        assertEquals(807.5f, volume[0].totalVolumeKg, 0.001f)
        assertEquals(2, volume[0].totalSets)

        val perExercise = workoutDao.getExerciseWorkoutVolume(0L)
        assertEquals(1, perExercise.size)
        assertEquals(807.5f, perExercise[0].volumeKg, 0.001f)
        assertEquals(2, perExercise[0].sets)

        val prs = workoutDao.getAllPersonalRecords()
        assertEquals(1, prs.size)
        assertEquals(100L, prs[0].exerciseId)
        assertEquals(102.5f, prs[0].maxWeightKg, 0.001f)

        val lastTrained = workoutDao.getMuscleLastTrained()
        val muscles = lastTrained.map { it.muscleGroup }
        assertTrue(muscles.contains(MuscleGroup.QUADS.name))
        assertFalse(muscles.contains(MuscleGroup.CHEST.name))
    }
}
