package com.strongest.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.strongest.app.data.db.StrongestDatabase
import com.strongest.app.data.model.*
import com.strongest.app.utils.ExportData
import com.strongest.app.utils.parseExportData
import com.strongest.app.utils.toJson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataExportImportIntegrationTest {

    private lateinit var sourceDb: StrongestDatabase
    private lateinit var targetDb: StrongestDatabase

    @Before
    fun setUp() {
        sourceDb = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrongestDatabase::class.java
        ).allowMainThreadQueries().build()

        targetDb = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrongestDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        sourceDb.close()
        targetDb.close()
    }

    @Test
    fun fullDatabaseRoundTrip() = runBlocking {
        val sourceDao = sourceDb.exerciseDao()
        val sourceRoutineDao = sourceDb.routineDao()
        val sourceWorkoutDao = sourceDb.workoutDao()
        val sourceMeasurementDao = sourceDb.measurementEntryDao()

        val exercise = Exercise(
            id = 100,
            name = "Squat",
            muscleGroup = MuscleGroup.QUADS,
            equipment = Equipment.BARBELL,
            description = "Full squat",
            instructions = "1. Rack bar\n2. Squat down\n3. Stand up",
            secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            imageUrl = "",
            isCustom = true,
            classification = ExerciseClassification.COMPOUND
        )
        sourceDao.insertExercise(exercise)

        val group = RoutineGroup(
            id = 200,
            name = "Leg Day",
            orderIndex = 1,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        sourceRoutineDao.insertRoutineGroup(group)

        val routine = Routine(
            id = 300,
            name = "Monday Legs",
            description = "Heavy leg workout",
            groupId = 200,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        sourceRoutineDao.insertRoutine(routine)

        val routineExercise = RoutineExercise(
            id = 400,
            routineId = 300,
            exerciseId = 100,
            orderIndex = 1,
            defaultSets = 3,
            defaultReps = 5,
            defaultWeight = 100f,
            restSeconds = 120
        )
        sourceRoutineDao.insertRoutineExercise(routineExercise)

        val routineSet = RoutineSet(
            id = 500,
            routineExerciseId = 400,
            setNumber = 1,
            weight = 100f,
            reps = 5,
            restSeconds = 120
        )
        sourceRoutineDao.insertRoutineSets(listOf(routineSet))

        val note = ExerciseNote(
            exerciseId = 100,
            noteText = "Keep chest up",
            updatedAt = 1000L
        )
        sourceDao.upsertNote(note)

        val workout = Workout(
            id = 600,
            routineId = 300,
            routineName = "Monday Legs",
            workoutName = "Leg day",
            startTime = 2000L,
            endTime = 3000L,
            notes = "Felt strong today",
            isOngoing = false
        )
        sourceWorkoutDao.insertWorkout(workout)

        val workoutExercise = WorkoutExercise(
            id = 700,
            workoutId = 600,
            exerciseId = 100,
            orderIndex = 1,
            notes = "Focus on depth"
        )
        sourceWorkoutDao.insertWorkoutExercise(workoutExercise)

        val setLog = SetLog(
            id = 800,
            workoutExerciseId = 700,
            setNumber = 1,
            weightKg = 100f,
            reps = 5,
            rpe = 8f,
            setType = SetType.NORMAL,
            restSeconds = 120,
            completedAt = 2500L
        )
        sourceWorkoutDao.insertSet(setLog)

        val measurement = MeasurementEntry(
            id = 900,
            metric = BodyMetric.WEIGHT,
            value = 90f,
            timestamp = 2000L,
            notes = "Pre-workout"
        )
        sourceMeasurementDao.insertEntry(measurement)

        val exportData = ExportData(
            exercises = sourceDao.getAllExercisesList(),
            routineGroups = sourceRoutineDao.getAllRoutineGroupsList(),
            routines = sourceRoutineDao.getAllRoutinesList(),
            routineExercises = sourceRoutineDao.getAllRoutineExercisesList(),
            routineSets = sourceRoutineDao.getAllRoutineSetsList(),
            exerciseNotes = sourceDao.getAllNotes(),
            workouts = sourceWorkoutDao.getAllWorkoutsList(),
            workoutExercises = sourceWorkoutDao.getAllWorkoutExercises(),
            sets = sourceWorkoutDao.getAllSets(),
            measurementEntries = sourceMeasurementDao.getAllEntriesList()
        )

        val json = exportData.toJson()
        assertNotNull(json)
        assertTrue(json.contains("Squat"))
        assertTrue(json.contains("Leg Day"))
        assertTrue(json.contains("Monday Legs"))

        val imported = json.parseExportData()
        assertNotNull(imported)

        val targetDao = targetDb.exerciseDao()
        val targetRoutineDao = targetDb.routineDao()
        val targetWorkoutDao = targetDb.workoutDao()
        val targetMeasurementDao = targetDb.measurementEntryDao()

        targetDb.query("PRAGMA foreign_keys = OFF", null)
        targetDb.clearAllTables()
        targetDb.query("PRAGMA foreign_keys = ON", null)

        imported!!.exercises.forEach { targetDao.insertExercise(it) }
        imported.routineGroups.forEach { targetRoutineDao.insertRoutineGroup(it) }
        imported.routines.forEach { targetRoutineDao.insertRoutine(it) }
        imported.routineExercises.forEach { targetRoutineDao.insertRoutineExercise(it) }
        imported.routineSets.forEach { targetRoutineDao.insertRoutineSets(listOf(it)) }
        imported.exerciseNotes.forEach { targetDao.upsertNote(it) }
        imported.workouts.forEach { targetWorkoutDao.insertWorkout(it) }
        imported.workoutExercises.forEach { targetWorkoutDao.insertWorkoutExercise(it) }
        imported.sets.forEach { targetWorkoutDao.insertSet(it) }
        imported.measurementEntries.forEach { targetMeasurementDao.insertEntry(it) }

        val targetExercises = targetDao.getAllExercisesList()
        assertEquals(1, targetExercises.size)
        with(targetExercises[0]) {
            assertEquals(100L, id)
            assertEquals("Squat", name)
            assertEquals(MuscleGroup.QUADS, muscleGroup)
            assertEquals(Equipment.BARBELL, equipment)
            assertEquals("Full squat", description)
            assertEquals("1. Rack bar\n2. Squat down\n3. Stand up", instructions)
            assertEquals(2, secondaryMuscles.size)
            assertTrue(isCustom)
            assertEquals(ExerciseClassification.COMPOUND, classification)
        }

        val targetGroups = targetRoutineDao.getAllRoutineGroupsList()
        assertEquals(1, targetGroups.size)
        assertEquals("Leg Day", targetGroups[0].name)
        assertEquals(200L, targetGroups[0].id)

        val targetRoutines = targetRoutineDao.getAllRoutinesList()
        assertEquals(1, targetRoutines.size)
        assertEquals("Monday Legs", targetRoutines[0].name)
        assertEquals(300L, targetRoutines[0].id)
        assertEquals(200L, targetRoutines[0].groupId)

        val targetRoutineExercises = targetRoutineDao.getAllRoutineExercisesList()
        assertEquals(1, targetRoutineExercises.size)
        assertEquals(400L, targetRoutineExercises[0].id)
        assertEquals(300L, targetRoutineExercises[0].routineId)
        assertEquals(100L, targetRoutineExercises[0].exerciseId)

        val targetRoutineSets = targetRoutineDao.getAllRoutineSetsList()
        assertEquals(1, targetRoutineSets.size)
        assertEquals(500L, targetRoutineSets[0].id)
        assertEquals(400L, targetRoutineSets[0].routineExerciseId)

        val targetNotes = targetDao.getAllNotes()
        assertEquals(1, targetNotes.size)
        assertEquals("Keep chest up", targetNotes[0].noteText)

        val targetWorkouts = targetWorkoutDao.getAllWorkoutsList()
        assertEquals(1, targetWorkouts.size)
        assertEquals("Leg day", targetWorkouts[0].workoutName)
        assertEquals(300L, targetWorkouts[0].routineId)

        val targetWorkoutExercises = targetWorkoutDao.getAllWorkoutExercises()
        assertEquals(1, targetWorkoutExercises.size)
        assertEquals(700L, targetWorkoutExercises[0].id)
        assertEquals(600L, targetWorkoutExercises[0].workoutId)

        val targetSets = targetWorkoutDao.getAllSets()
        assertEquals(1, targetSets.size)
        assertEquals(800L, targetSets[0].id)
        assertEquals(8f, targetSets[0].rpe!!, 0.01f)

        val targetMeasurements = targetMeasurementDao.getAllEntriesList()
        assertEquals(1, targetMeasurements.size)
        assertEquals(900L, targetMeasurements[0].id)
        assertEquals(BodyMetric.WEIGHT, targetMeasurements[0].metric)
        assertEquals(90f, targetMeasurements[0].value)
    }

    @Test
    fun exportWithEmptyDatabaseProducesValidJson() = runBlocking {
        val sourceDao = sourceDb.exerciseDao()
        val sourceRoutineDao = sourceDb.routineDao()
        val sourceWorkoutDao = sourceDb.workoutDao()
        val sourceMeasurementDao = sourceDb.measurementEntryDao()

        val exportData = ExportData(
            exercises = sourceDao.getAllExercisesList(),
            routineGroups = sourceRoutineDao.getAllRoutineGroupsList(),
            routines = sourceRoutineDao.getAllRoutinesList(),
            routineExercises = sourceRoutineDao.getAllRoutineExercisesList(),
            routineSets = sourceRoutineDao.getAllRoutineSetsList(),
            exerciseNotes = sourceDao.getAllNotes(),
            workouts = sourceWorkoutDao.getAllWorkoutsList(),
            workoutExercises = sourceWorkoutDao.getAllWorkoutExercises(),
            sets = sourceWorkoutDao.getAllSets(),
            measurementEntries = sourceMeasurementDao.getAllEntriesList()
        )

        val json = exportData.toJson()
        assertNotNull(json)
        assertTrue(json.isNotEmpty())

        val parsed = json.parseExportData()
        assertNotNull(parsed)
        assertTrue(parsed!!.exercises.isEmpty())
        assertTrue(parsed.routineGroups.isEmpty())
        assertTrue(parsed.routines.isEmpty())
        assertTrue(parsed.routineExercises.isEmpty())
        assertTrue(parsed.routineSets.isEmpty())
        assertTrue(parsed.exerciseNotes.isEmpty())
        assertTrue(parsed.workouts.isEmpty())
        assertTrue(parsed.workoutExercises.isEmpty())
        assertTrue(parsed.sets.isEmpty())
        assertTrue(parsed.measurementEntries.isEmpty())
    }
}
