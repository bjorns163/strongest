package com.strongest.app

import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.exerciseSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the type tagging of the built-in exercise catalogue. */
class ExerciseSeedDataTest {

    @Test
    fun `ids are unique`() {
        val ids = exerciseSeedData.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `names are unique within a muscle group`() {
        val duplicates = exerciseSeedData
            .groupBy { it.name.lowercase() to it.muscleGroup }
            .filterValues { it.size > 1 }
        assertTrue("Duplicate entries: ${duplicates.keys}", duplicates.isEmpty())
    }

    @Test
    fun `multi-joint lifts are compound`() {
        val compounds = exerciseSeedData.filter {
            it.name.contains("Bench Press") || it.name.contains("Deadlift") ||
                it.name.contains("Pull-Up") || it.name.contains("Barbell Back Squat")
        }
        assertTrue(compounds.isNotEmpty())
        assertTrue(
            "Not tagged compound: ${compounds.filter { it.type != ExerciseType.COMPOUND }.map { it.name }}",
            compounds.all { it.type == ExerciseType.COMPOUND }
        )
    }

    @Test
    fun `single-joint lifts are isolation`() {
        val isolations = exerciseSeedData.filter {
            it.name.contains("Lateral Raise") || it.name.contains("Leg Extension") ||
                it.name.contains("Calf Raise") || it.name.contains("Preacher Curl")
        }
        assertTrue(isolations.isNotEmpty())
        assertTrue(
            "Not tagged isolation: ${isolations.filter { it.type != ExerciseType.ISOLATION }.map { it.name }}",
            isolations.all { it.type == ExerciseType.ISOLATION }
        )
    }

    @Test
    fun `static holds are isometric`() {
        val holds = exerciseSeedData.filter {
            it.name == "Plank" || it.name == "Side Plank" || it.name == "Wall Sit" ||
                it.name == "Hollow Body Hold" || it.name == "Dead Hang"
        }
        assertTrue(holds.isNotEmpty())
        assertTrue(
            "Not tagged isometric: ${holds.filter { it.type != ExerciseType.ISOMETRIC }.map { it.name }}",
            holds.all { it.type == ExerciseType.ISOMETRIC }
        )
    }

    @Test
    fun `cardio is never isolation work`() {
        val cardio = exerciseSeedData.filter { it.muscleGroup == MuscleGroup.CARDIO }
        assertTrue(cardio.isNotEmpty())
        assertTrue(cardio.none { it.type == ExerciseType.ISOLATION })
    }
}
