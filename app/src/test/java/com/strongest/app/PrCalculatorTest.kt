package com.strongest.app

import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.SetType
import com.strongest.app.utils.PrKind
import com.strongest.app.utils.computeWorkoutPrs
import com.strongest.app.utils.excludingWarmUps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression: every Progress query filters `setType != 'WARM_UP'` in SQL, but the rows feeding
 * PR detection come from `getAllCompletedHistoryRows`, which deliberately returns everything.
 * `computeWorkoutPrs` selected `setType` and then ignored it, so warm-ups inflated volume PRs
 * and a heavy set tagged as a warm-up could register as a weight PR.
 */
class PrCalculatorTest {

    private var nextId = 1L

    private fun row(
        workoutId: Long,
        exerciseId: Long,
        weightKg: Float,
        reps: Int,
        setType: SetType = SetType.NORMAL
    ) = HistorySetRow(
        workoutId = workoutId,
        workoutExerciseId = workoutId * 10 + exerciseId,
        exerciseId = exerciseId,
        exerciseName = "Exercise $exerciseId",
        muscleGroup = "CHEST",
        orderIndex = 0,
        setId = nextId++,
        setNumber = 1,
        weightKg = weightKg,
        reps = reps,
        setType = setType.name
    )

    @Test
    fun `a heavy warm-up set does not register as a weight PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            // The user tagged a heavy top single as a warm-up; it must not count.
            row(workoutId = 2, exerciseId = 1, weightKg = 200f, reps = 1, setType = SetType.WARM_UP),
            row(workoutId = 2, exerciseId = 1, weightKg = 90f, reps = 5)
        )

        val prs = computeWorkoutPrs(rows, workoutId = 2)

        assertNull(prs.firstOrNull { it.kind == PrKind.WEIGHT })
    }

    @Test
    fun `volume PR ignores warm-up volume`() {
        val rows = listOf(
            // Workout 1: 1000 kg of working volume, plus 2000 kg of warm-up.
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 10),
            row(workoutId = 1, exerciseId = 1, weightKg = 200f, reps = 10, setType = SetType.WARM_UP),
            // Workout 2: 1500 kg, all working sets — the real volume PR.
            row(workoutId = 2, exerciseId = 1, weightKg = 150f, reps = 10)
        )

        // Counting warm-ups, workout 1 would look like 3000 kg and win.
        assertNull(computeWorkoutPrs(rows, workoutId = 1).firstOrNull { it.kind == PrKind.VOLUME })

        val volumePr = computeWorkoutPrs(rows, workoutId = 2).firstOrNull { it.kind == PrKind.VOLUME }
        assertEquals(1500f, volumePr!!.volumeKg!!, 0.01f)
    }

    @Test
    fun `working sets still produce weight and 1RM PRs`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 120f, reps = 5)
        )

        val prs = computeWorkoutPrs(rows, workoutId = 2)

        val weightPr = prs.firstOrNull { it.kind == PrKind.WEIGHT }
        assertEquals(120f, weightPr!!.weightKg!!, 0.01f)
        assertTrue(prs.any { it.kind == PrKind.ONE_RM })
    }

    @Test
    fun `a workout of nothing but warm-ups produces no PRs`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 200f, reps = 10, setType = SetType.WARM_UP)
        )

        assertEquals(emptyList<Any>(), computeWorkoutPrs(rows, workoutId = 1))
    }

    @Test
    fun `only warm-ups are excluded, not failure or drop sets`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5, setType = SetType.NORMAL),
            row(workoutId = 1, exerciseId = 1, weightKg = 90f, reps = 3, setType = SetType.FAILURE),
            row(workoutId = 1, exerciseId = 1, weightKg = 80f, reps = 8, setType = SetType.DROP_SET),
            row(workoutId = 1, exerciseId = 1, weightKg = 50f, reps = 5, setType = SetType.WARM_UP)
        )

        val kept = rows.excludingWarmUps()

        assertEquals(3, kept.size)
        assertEquals(
            listOf(SetType.NORMAL.name, SetType.FAILURE.name, SetType.DROP_SET.name),
            kept.map { it.setType }
        )
    }
}
