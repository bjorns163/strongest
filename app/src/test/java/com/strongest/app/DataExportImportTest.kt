package com.strongest.app

import com.strongest.app.data.model.*
import com.strongest.app.data.repository.*
import com.strongest.app.utils.ExportData
import com.strongest.app.utils.parseExportData
import com.strongest.app.utils.toJson
import org.junit.Assert.*
import org.junit.Test

class DataExportImportTest {

    private val sampleExercise = Exercise(
        id = 42,
        name = "Bench Press",
        muscleGroup = MuscleGroup.CHEST,
        equipment = Equipment.BARBELL,
        description = "Lie on a flat bench",
        instructions = "1. Setup\n2. Lift\n3. Lower",
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
        imageUrl = "https://example.com/bench.png",
        isCustom = true,
        type = ExerciseType.COMPOUND
    )

    private val sampleRoutineGroup = RoutineGroup(
        id = 10,
        name = "Push Day",
        orderIndex = 1,
        createdAt = 1000L,
        updatedAt = 2000L
    )

    private val sampleRoutine = Routine(
        id = 20,
        name = "Monday Push",
        description = "Heavy push day",
        groupId = 10,
        createdAt = 1000L,
        updatedAt = 2000L
    )

    private val sampleRoutineExercise = RoutineExercise(
        id = 30,
        routineId = 20,
        exerciseId = 42,
        orderIndex = 1,
        defaultSets = 3,
        defaultReps = 10,
        defaultWeight = 80f,
        restSeconds = 90
    )

    private val sampleRoutineSet = RoutineSet(
        id = 40,
        routineExerciseId = 30,
        setNumber = 1,
        weight = 80f,
        reps = 10,
        restSeconds = 90,
        setType = SetType.WARM_UP
    )

    private val sampleExerciseNote = ExerciseNote(
        exerciseId = 42,
        noteText = "Focus on form",
        updatedAt = 3000L
    )

    private val sampleWorkout = Workout(
        id = 50,
        routineId = 20,
        routineName = "Monday Push",
        workoutName = "Push session",
        startTime = 4000L,
        endTime = 5000L,
        notes = "Felt strong",
        isOngoing = false
    )

    private val sampleWorkoutExercise = WorkoutExercise(
        id = 60,
        workoutId = 50,
        exerciseId = 42,
        orderIndex = 1,
        notes = "Focused on slow negatives"
    )

    private val sampleSet = SetLog(
        id = 70,
        workoutExerciseId = 60,
        setNumber = 1,
        weightKg = 80f,
        reps = 10,
        rpe = 8f,
        setType = SetType.NORMAL,
        restSeconds = 90,
        completedAt = 4500L
    )

    private val sampleMeasurement = MeasurementEntry(
        id = 80,
        metric = BodyMetric.WEIGHT,
        value = 85f,
        timestamp = 4000L,
        notes = "Morning weight"
    )

    private val sampleExerciseSettings = ExerciseSettings(
        exerciseId = 42,
        warmUpSetCount = 2,
        barWeightKg = 0f,
        plateSingleSide = true
    )

    private val sampleSettings = AppSettings(
        themeMode = com.strongest.app.ThemeMode.DARK,
        weightUnit = WeightUnit.KG,
        defaultRestSeconds = 120,
        timerAdjustmentSeconds = 30,
        lastSetRestSeconds = 180,
        keepScreenOn = true,
        notificationSoundUri = "content://settings/ringtone",
        rpeTrackingEnabled = true,
        availableKgPlates = mapOf(1.25f to 999, 2.5f to 999, 5f to 999, 10f to 999, 20f to 999),
        availableLbsPlates = mapOf(2.5f to 999, 5f to 999, 10f to 999),
        oneRmFormula = OneRmFormula.EPLEY,
        recoveryHoursByMuscle = mapOf<MuscleGroup, Int>(
            MuscleGroup.CHEST to 48,
            MuscleGroup.QUADS to 72
        ),
        userSex = Sex.MALE,
        birthYear = 1990,
        caliperMode = CaliperMode.SEVEN_SITE
    )

    @Test
    fun `full round-trip preserves all data`() {
        val original = ExportData(
            version = 1,
            exportedAt = 12345L,
            source = "com.strongest.app",
            exercises = listOf(sampleExercise),
            routineGroups = listOf(sampleRoutineGroup),
            routines = listOf(sampleRoutine),
            routineExercises = listOf(sampleRoutineExercise),
            routineSets = listOf(sampleRoutineSet),
            exerciseNotes = listOf(sampleExerciseNote),
            exerciseSettings = listOf(sampleExerciseSettings),
            workouts = listOf(sampleWorkout),
            workoutExercises = listOf(sampleWorkoutExercise),
            sets = listOf(sampleSet),
            measurementEntries = listOf(sampleMeasurement),
            settings = sampleSettings
        )

        val json = original.toJson()
        assertNotNull(json)
        assertTrue(json.isNotEmpty())
        assertTrue(json.contains("\"Bench Press\""))
        assertTrue(json.contains("com.strongest.app"))

        val parsed = json.parseExportData()
        assertNotNull("Parsed ExportData should not be null", parsed)
        assertEquals(1, parsed!!.version)
        assertEquals(12345L, parsed.exportedAt)
        assertEquals("com.strongest.app", parsed.source)

        assertEquals(1, parsed.exercises.size)
        with(parsed.exercises[0]) {
            assertEquals(42L, id)
            assertEquals("Bench Press", name)
            assertEquals(MuscleGroup.CHEST, muscleGroup)
            assertEquals(Equipment.BARBELL, equipment)
            assertEquals("Lie on a flat bench", description)
            assertEquals("1. Setup\n2. Lift\n3. Lower", instructions)
            assertEquals(2, secondaryMuscles.size)
            assertEquals(MuscleGroup.SHOULDERS, secondaryMuscles[0])
            assertEquals(MuscleGroup.TRICEPS, secondaryMuscles[1])
            assertEquals("https://example.com/bench.png", imageUrl)
            assertTrue(isCustom)
            assertEquals(ExerciseType.COMPOUND, type)
        }

        assertEquals(1, parsed.routineGroups.size)
        with(parsed.routineGroups[0]) {
            assertEquals(10L, id)
            assertEquals("Push Day", name)
            assertEquals(1, orderIndex)
            assertEquals(1000L, createdAt)
            assertEquals(2000L, updatedAt)
        }

        assertEquals(1, parsed.routines.size)
        with(parsed.routines[0]) {
            assertEquals(20L, id)
            assertEquals("Monday Push", name)
            assertEquals("Heavy push day", description)
            assertEquals(10L, groupId)
            assertEquals(1000L, createdAt)
            assertEquals(2000L, updatedAt)
        }

        assertEquals(1, parsed.routineExercises.size)
        with(parsed.routineExercises[0]) {
            assertEquals(30L, id)
            assertEquals(20L, routineId)
            assertEquals(42L, exerciseId)
            assertEquals(1, orderIndex)
            assertEquals(3, defaultSets)
            assertEquals(10, defaultReps)
            assertEquals(80f, defaultWeight)
            assertEquals(90, restSeconds)
        }

        assertEquals(1, parsed.routineSets.size)
        with(parsed.routineSets[0]) {
            assertEquals(40L, id)
            assertEquals(30L, routineExerciseId)
            assertEquals(1, setNumber)
            assertEquals(80f, weight)
            assertEquals(10, reps)
            assertEquals(90, restSeconds)
            assertEquals(SetType.WARM_UP, setType)
        }

        assertEquals(1, parsed.exerciseNotes.size)
        with(parsed.exerciseNotes[0]) {
            assertEquals(42L, exerciseId)
            assertEquals("Focus on form", noteText)
            assertEquals(3000L, updatedAt)
        }

        assertEquals(1, parsed.exerciseSettings.size)
        with(parsed.exerciseSettings[0]) {
            assertEquals(42L, exerciseId)
            assertEquals(2, warmUpSetCount)
            assertEquals(0f, barWeightKg)
            assertTrue(plateSingleSide)
        }

        assertEquals(1, parsed.workouts.size)
        with(parsed.workouts[0]) {
            assertEquals(50L, id)
            assertEquals(20L, routineId)
            assertEquals("Monday Push", routineName)
            assertEquals("Push session", workoutName)
            assertEquals(4000L, startTime)
            assertEquals(5000L, endTime)
            assertEquals("Felt strong", notes)
            assertFalse(isOngoing)
        }

        assertEquals(1, parsed.workoutExercises.size)
        with(parsed.workoutExercises[0]) {
            assertEquals(60L, id)
            assertEquals(50L, workoutId)
            assertEquals(42L, exerciseId)
            assertEquals(1, orderIndex)
            assertEquals("Focused on slow negatives", notes)
        }

        assertEquals(1, parsed.sets.size)
        with(parsed.sets[0]) {
            assertEquals(70L, id)
            assertEquals(60L, workoutExerciseId)
            assertEquals(1, setNumber)
            assertEquals(80f, weightKg)
            assertEquals(10, reps)
            assertEquals(8f, rpe!!, 0.01f)
            assertEquals(SetType.NORMAL, setType)
            assertEquals(90, restSeconds)
            assertEquals(4500L, completedAt)
        }

        assertEquals(1, parsed.measurementEntries.size)
        with(parsed.measurementEntries[0]) {
            assertEquals(80L, id)
            assertEquals(BodyMetric.WEIGHT, metric)
            assertEquals(85f, value)
            assertEquals(4000L, timestamp)
            assertEquals("Morning weight", notes)
        }

        with(parsed.settings) {
            assertEquals(com.strongest.app.ThemeMode.DARK, themeMode)
            assertEquals(WeightUnit.KG, weightUnit)
            assertEquals(120, defaultRestSeconds)
            assertEquals(30, timerAdjustmentSeconds)
            assertEquals(180, lastSetRestSeconds)
            assertEquals(true, keepScreenOn)
            assertEquals("content://settings/ringtone", notificationSoundUri)
            assertEquals(true, rpeTrackingEnabled)
            assertEquals(setOf(1.25f, 2.5f, 5f, 10f, 20f), availableKgPlates.keys)
            assertEquals(setOf(2.5f, 5f, 10f), availableLbsPlates.keys)
            assertEquals(OneRmFormula.EPLEY, oneRmFormula)
            assertEquals(2, recoveryHoursByMuscle.size)
            assertEquals(48, recoveryHoursByMuscle[MuscleGroup.CHEST])
            assertEquals(72, recoveryHoursByMuscle[MuscleGroup.QUADS])
            assertEquals(Sex.MALE, userSex)
            assertEquals(1990, birthYear)
            assertEquals(CaliperMode.SEVEN_SITE, caliperMode)
        }
    }

    @Test
    fun `empty lists round-trip correctly`() {
        val original = ExportData(
            exercises = emptyList(),
            routineGroups = emptyList(),
            routines = emptyList(),
            routineExercises = emptyList(),
            routineSets = emptyList(),
            exerciseNotes = emptyList(),
            workouts = emptyList(),
            workoutExercises = emptyList(),
            sets = emptyList(),
            measurementEntries = emptyList()
        )

        val json = original.toJson()
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
        assertNotNull(parsed.settings)
    }

    @Test
    fun `nullable fields round-trip correctly`() {
        val workoutNoOptional = Workout(
            id = 1,
            routineId = null,
            routineName = null,
            workoutName = null,
            startTime = 1000L,
            endTime = null,
            notes = "",
            isOngoing = true
        )

        val setNoRpe = SetLog(
            id = 2,
            workoutExerciseId = 3,
            setNumber = 1,
            weightKg = 100f,
            reps = 5,
            rpe = null,
            setType = SetType.NORMAL,
            restSeconds = 90,
            completedAt = 2000L
        )

        val original = ExportData(
            workouts = listOf(workoutNoOptional),
            sets = listOf(setNoRpe),
            settings = AppSettings(notificationSoundUri = null)
        )

        val json = original.toJson()
        val parsed = json.parseExportData()

        assertNotNull(parsed)
        assertNull(parsed!!.workouts[0].routineId)
        assertNull(parsed.workouts[0].routineName)
        assertNull(parsed.workouts[0].workoutName)
        assertNull(parsed.workouts[0].endTime)
        assertTrue(parsed.workouts[0].isOngoing)
        assertNull(parsed.sets[0].rpe)
        assertNull(parsed.settings.notificationSoundUri)
    }

    @Test
    fun `special characters in text fields`() {
        val exercise = Exercise(
            id = 1,
            name = "Bench Press (Barbell) -- King of Chest?",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BARBELL,
            description = "Special chars: n, u, o, a, E, L, heart, newline, tab",
            instructions = "<script>alert('xss')</script> & \"quotes\"",
            secondaryMuscles = emptyList(),
            imageUrl = "",
            isCustom = false,
            type = ExerciseType.ISOLATION
        )

        val note = ExerciseNote(
            exerciseId = 1,
            noteText = "Unicode: hello and privet",
            updatedAt = 1000L
        )

        val original = ExportData(
            exercises = listOf(exercise),
            exerciseNotes = listOf(note)
        )

        val json = original.toJson()
        val parsed = json.parseExportData()

        assertNotNull(parsed)
        assertEquals("Bench Press (Barbell) -- King of Chest?", parsed!!.exercises[0].name)
        assertEquals("Special chars: n, u, o, a, E, L, heart, newline, tab", parsed.exercises[0].description)
        assertEquals("<script>alert('xss')</script> & \"quotes\"", parsed.exercises[0].instructions)
        assertEquals("Unicode: hello and privet", parsed.exerciseNotes[0].noteText)
    }

    @Test
    fun `corrupt JSON returns null`() {
        assertNull("this is not valid json".parseExportData())
        assertNull("{\"version\": 1".parseExportData())
        assertNull("".parseExportData())
    }

    @Test
    fun `missing optional fields use defaults`() {
        val json = """{
            "version": 1,
            "exportedAt": 1000,
            "source": "test",
            "exercises": [
                {
                    "id": 1,
                    "name": "Test",
                    "muscleGroup": "CHEST",
                    "equipment": "BARBELL",
                    "secondaryMuscles": [],
                    "imageUrl": "",
                    "isCustom": false
                }
            ],
            "measurementEntries": [
                {
                    "id": 1,
                    "metric": "WEIGHT",
                    "value": 80.0,
                    "timestamp": 1000
                }
            ]
        }"""

        val parsed = json.parseExportData()
        assertNotNull(parsed)
        assertEquals("Test", parsed!!.exercises[0].name)
        assertEquals("", parsed.exercises[0].description)
        assertEquals("", parsed.exercises[0].instructions)
        assertEquals(ExerciseType.ISOLATION, parsed.exercises[0].type)
        assertEquals("", parsed.measurementEntries[0].notes)
        assertEquals(90, parsed.settings.defaultRestSeconds)
    }

    @Test
    fun `all enum values round-trip correctly`() {
        val exercises = MuscleGroup.entries.mapIndexed { i, mg ->
            Exercise(
                id = i.toLong(),
                name = mg.name,
                muscleGroup = mg,
                equipment = Equipment.BARBELL,
                description = "",
                instructions = "",
                secondaryMuscles = emptyList(),
                imageUrl = "",
                isCustom = false,
                type = ExerciseType.ISOLATION
            )
        }

        val original = ExportData(exercises = exercises)
        val json = original.toJson()
        val parsed = json.parseExportData()

        assertNotNull(parsed)
        assertEquals(MuscleGroup.entries.size, parsed!!.exercises.size)
        for (mg in MuscleGroup.entries) {
            assertTrue(
                "MuscleGroup $mg should be present",
                parsed.exercises.any { it.muscleGroup == mg }
            )
        }
    }

    @Test
    fun `measurement with null notes field in JSON uses default`() {
        val json = """{
            "version": 1,
            "measurementEntries": [
                {
                    "id": 1,
                    "metric": "WEIGHT",
                    "value": 80.0,
                    "timestamp": 1000
                }
            ]
        }"""
        val parsed = json.parseExportData()
        assertNotNull(parsed)
        assertEquals("", parsed!!.measurementEntries[0].notes)
    }
}
