package com.strongest.app

import com.strongest.app.data.db.RoutineWithExercisesAndSets
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetType
import com.strongest.app.utils.parseSharedRoutine
import com.strongest.app.utils.toJson
import com.strongest.app.utils.toSharedRoutine
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineShareTest {

    private val benchPress = Exercise(
        id = 42,
        name = "Bench Press",
        muscleGroup = MuscleGroup.CHEST,
        equipment = Equipment.BARBELL
    )

    private val routineExercise = RoutineExercise(
        id = 7,
        routineId = 1,
        exerciseId = 42,
        orderIndex = 0,
        defaultSets = 4,
        defaultReps = 8,
        defaultWeight = 80f,
        restSeconds = 120
    )

    private val full = RoutineWithExercisesAndSets(
        routine = Routine(id = 1, name = "Monday Push", description = "Heavy push day"),
        exercises = listOf(routineExercise),
        sets = mapOf(
            7L to listOf(
                RoutineSet(id = 1, routineExerciseId = 7, setNumber = 1, weight = 40f, reps = 10, restSeconds = 60, setType = SetType.WARM_UP),
                RoutineSet(id = 2, routineExerciseId = 7, setNumber = 2, weight = 60f, reps = 5, restSeconds = 60, setType = SetType.WARM_UP),
                RoutineSet(id = 3, routineExerciseId = 7, setNumber = 3, weight = 80f, reps = 8, restSeconds = 120, setType = SetType.NORMAL),
                RoutineSet(id = 4, routineExerciseId = 7, setNumber = 4, weight = 80f, reps = 6, restSeconds = 120, setType = SetType.FAILURE)
            )
        )
    )

    private fun sharedJson(): String =
        full.toSharedRoutine(mapOf(42L to benchPress)).toJson()

    @Test
    fun `exported routine json contains the set type of every set`() {
        val sets = JSONObject(sharedJson())
            .getJSONArray("exercises")
            .getJSONObject(0)
            .getJSONArray("sets")

        assertEquals(4, sets.length())
        assertEquals("WARM_UP", sets.getJSONObject(0).getString("setType"))
        assertEquals("WARM_UP", sets.getJSONObject(1).getString("setType"))
        assertEquals("NORMAL", sets.getJSONObject(2).getString("setType"))
        assertEquals("FAILURE", sets.getJSONObject(3).getString("setType"))
    }

    @Test
    fun `round trip preserves warm-up sets`() {
        val parsed = parseOrFail(sharedJson())

        assertEquals("Monday Push", parsed.name)
        assertEquals("Heavy push day", parsed.description)
        assertEquals(1, parsed.exercises.size)

        with(parsed.exercises[0]) {
            assertEquals("Bench Press", name)
            assertEquals(MuscleGroup.CHEST, muscleGroup)
            assertEquals(Equipment.BARBELL, equipment)
            assertEquals(4, defaultSets)
            assertEquals(8, defaultReps)
            assertEquals(80f, defaultWeight)
            assertEquals(120, restSeconds)

            assertEquals(
                listOf(SetType.WARM_UP, SetType.WARM_UP, SetType.NORMAL, SetType.FAILURE),
                sets.map { it.setType }
            )
            with(sets[0]) {
                assertEquals(1, setNumber)
                assertEquals(40f, weight)
                assertEquals(10, reps)
                assertEquals(60, restSeconds)
            }
        }
    }

    @Test
    fun `sets without a set type fall back to normal`() {
        val legacyJson = """
            {
              "version": 1,
              "name": "Legacy",
              "description": "",
              "exercises": [
                {
                  "name": "Bench Press",
                  "muscleGroup": "CHEST",
                  "equipment": "BARBELL",
                  "sets": [{"setNumber": 1, "weight": 80.0, "reps": 8, "restSeconds": 90}],
                  "defaultSets": 1,
                  "defaultWeight": 80.0,
                  "defaultReps": 8,
                  "restSeconds": 90
                }
              ]
            }
        """.trimIndent()

        val parsed = parseOrFail(legacyJson)
        assertEquals(SetType.NORMAL, parsed.exercises.single().sets.single().setType)
    }

    @Test
    fun `unknown set type falls back to normal instead of failing the import`() {
        val json = sharedJson().replace("\"WARM_UP\"", "\"SUPERSET_FROM_THE_FUTURE\"")
        val parsed = parseOrFail(json)
        assertEquals(
            listOf(SetType.NORMAL, SetType.NORMAL, SetType.NORMAL, SetType.FAILURE),
            parsed.exercises.single().sets.map { it.setType }
        )
    }

    @Test
    fun `malformed and future-version payloads parse to null`() {
        assertNull("not json".parseSharedRoutine())
        assertNull(sharedJson().replace("\"version\": 1", "\"version\": 2").parseSharedRoutine())
    }

    private fun parseOrFail(json: String) =
        json.parseSharedRoutine() ?: throw AssertionError("Expected a parseable shared routine")
}
