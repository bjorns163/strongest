package com.strongest.app

import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.SetType
import com.strongest.app.utils.PrKind
import com.strongest.app.utils.computeWorkoutPrs
import com.strongest.app.utils.computeWorkoutVolume
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
        setType: SetType = SetType.NORMAL,
        muscleGroup: String = "CHEST",
        startTime: Long = workoutId * 1000
    ) = HistorySetRow(
        workoutId = workoutId,
        workoutExerciseId = workoutId * 10 + exerciseId,
        exerciseId = exerciseId,
        exerciseName = "Exercise $exerciseId",
        muscleGroup = muscleGroup,
        orderIndex = 0,
        setId = nextId++,
        setNumber = 1,
        weightKg = weightKg,
        reps = reps,
        setType = setType.name,
        workoutStartTime = startTime
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
            // Workout 1: 1500 kg, all working sets.
            row(workoutId = 1, exerciseId = 1, weightKg = 150f, reps = 10),
            // Workout 2: 1000 kg of working volume, plus 2000 kg of warm-up.
            row(workoutId = 2, exerciseId = 1, weightKg = 100f, reps = 10),
            row(workoutId = 2, exerciseId = 1, weightKg = 200f, reps = 10, setType = SetType.WARM_UP)
        )

        // Counting warm-ups, workout 2 would look like 3000 kg and beat workout 1.
        assertNull(computeWorkoutPrs(rows, workoutId = 2).firstOrNull { it.kind == PrKind.VOLUME })
    }

    @Test
    fun `working sets still produce a weight PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 120f, reps = 5)
        )

        val prs = computeWorkoutPrs(rows, workoutId = 2)

        val weightPr = prs.firstOrNull { it.kind == PrKind.WEIGHT }
        assertEquals(120f, weightPr!!.weightKg!!, 0.01f)
    }

    @Test
    fun `every record a lift beats is listed, most significant first`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            // Heavier weight, better 1RM, better set and exercise volume — all at once.
            row(workoutId = 2, exerciseId = 1, weightKg = 120f, reps = 5)
        )

        val exercisePrs = computeWorkoutPrs(rows, workoutId = 2).filter { it.exerciseId == 1L }

        assertEquals(
            listOf(PrKind.WEIGHT, PrKind.ONE_RM, PrKind.SET_VOLUME, PrKind.EXERCISE_VOLUME),
            exercisePrs.map { it.kind }
        )
    }

    @Test
    fun `a better 1RM at a lighter weight is a 1RM PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5), // e1RM 116.7
            row(workoutId = 2, exerciseId = 1, weightKg = 95f, reps = 10) // e1RM 126.7
        )

        val pr = computeWorkoutPrs(rows, workoutId = 2).first { it.exerciseId == 1L }

        assertEquals(PrKind.ONE_RM, pr.kind)
        assertEquals(126.67f, pr.oneRmKg!!, 0.01f)
    }

    @Test
    fun `sets above the rep cap don't count toward 1RM`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5), // e1RM 116.7
            // Epley would call this a 120 kg 1RM, which a 60 kg set of 30 doesn't show.
            row(workoutId = 2, exerciseId = 1, weightKg = 60f, reps = 30)
        )

        val prs = computeWorkoutPrs(rows, workoutId = 2)

        assertNull(prs.firstOrNull { it.kind == PrKind.ONE_RM })
        assertTrue(prs.any { it.kind == PrKind.SET_VOLUME })
    }

    @Test
    fun `more reps at a weight lifted before is a reps PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 110f, reps = 3),
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            // Not heavier than 110, and e1RM 120 < 121 — but one more rep at 100.
            row(workoutId = 2, exerciseId = 1, weightKg = 100f, reps = 6)
        )

        val pr = computeWorkoutPrs(rows, workoutId = 2).first { it.exerciseId == 1L }

        assertEquals(PrKind.REPS_AT_WEIGHT, pr.kind)
        assertEquals(100f, pr.weightKg!!, 0.01f)
        assertEquals(6, pr.reps)
    }

    @Test
    fun `the first time at a weight is not a reps PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 90f, reps = 5)
        )

        assertTrue(computeWorkoutPrs(rows, workoutId = 2).none { it.exerciseId == 1L })
    }

    @Test
    fun `weights entered in lbs match across workouts`() {
        // 225 lbs converted to kg; the stored float isn't exactly 102.06.
        val lbs225 = 225f * 0.45359237f
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 120f, reps = 1),
            row(workoutId = 1, exerciseId = 1, weightKg = lbs225, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = lbs225 + 0.001f, reps = 6)
        )

        val pr = computeWorkoutPrs(rows, workoutId = 2).first { it.exerciseId == 1L }

        assertEquals(PrKind.REPS_AT_WEIGHT, pr.kind)
    }

    @Test
    fun `bodyweight sets set max reps PRs`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 0f, reps = 8),
            row(workoutId = 2, exerciseId = 1, weightKg = 0f, reps = 8),
            row(workoutId = 3, exerciseId = 1, weightKg = 0f, reps = 10)
        )

        assertEquals(PrKind.MAX_REPS, computeWorkoutPrs(rows, workoutId = 1).single().kind)
        assertEquals(emptyList<Any>(), computeWorkoutPrs(rows, workoutId = 2))
        assertEquals(10, computeWorkoutPrs(rows, workoutId = 3).single().reps)
    }

    @Test
    fun `a better single set is a set volume PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5), // 500
            row(workoutId = 2, exerciseId = 1, weightKg = 80f, reps = 7) // 560, e1RM 98.7
        )

        val pr = computeWorkoutPrs(rows, workoutId = 2).first { it.exerciseId == 1L }

        assertEquals(PrKind.SET_VOLUME, pr.kind)
        assertEquals(560f, pr.volumeKg!!, 0.01f)
    }

    @Test
    fun `more total work on an exercise is an exercise volume PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5), // 500
            // Every set is smaller, but three of them add up to 1200.
            row(workoutId = 2, exerciseId = 1, weightKg = 80f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 80f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 80f, reps = 5)
        )

        val pr = computeWorkoutPrs(rows, workoutId = 2).first { it.exerciseId == 1L }

        assertEquals(PrKind.EXERCISE_VOLUME, pr.kind)
        assertEquals(1200f, pr.volumeKg!!, 0.01f)
    }

    @Test
    fun `cardio records duration first, then level`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 2, weightKg = 8f, reps = 1200, muscleGroup = "CARDIO"),
            row(workoutId = 2, exerciseId = 2, weightKg = 8f, reps = 1800, muscleGroup = "CARDIO"),
            row(workoutId = 3, exerciseId = 2, weightKg = 10f, reps = 900, muscleGroup = "CARDIO")
        )

        assertEquals(PrKind.CARDIO_DURATION, computeWorkoutPrs(rows, workoutId = 2).single().kind)
        val levelPr = computeWorkoutPrs(rows, workoutId = 3).single()
        assertEquals(PrKind.CARDIO_LEVEL, levelPr.kind)
        assertEquals(10f, levelPr.weightKg!!, 0.01f)
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

    @Test
    fun `cardio sets do not count toward workout volume`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 10),
            // Cardio stores distance × time in weight × reps — not a load.
            row(workoutId = 1, exerciseId = 2, weightKg = 5f, reps = 1800, muscleGroup = "CARDIO")
        )

        assertEquals(1000f, computeWorkoutVolume(rows), 0.01f)
    }

    @Test
    fun `cardio does not win the volume PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 10),
            row(workoutId = 2, exerciseId = 1, weightKg = 50f, reps = 10),
            row(workoutId = 2, exerciseId = 2, weightKg = 10f, reps = 3600, muscleGroup = "CARDIO")
        )

        assertNull(computeWorkoutPrs(rows, workoutId = 2).firstOrNull { it.kind == PrKind.VOLUME })
        assertTrue(computeWorkoutPrs(rows, workoutId = 1).any { it.kind == PrKind.VOLUME })
    }

    @Test
    fun `matching an earlier best is not a PR`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 100f, reps = 5)
        )

        assertTrue(computeWorkoutPrs(rows, workoutId = 1).map { it.kind }
            .containsAll(listOf(PrKind.WEIGHT, PrKind.VOLUME)))
        assertEquals(emptyList<Any>(), computeWorkoutPrs(rows, workoutId = 2))
    }

    @Test
    fun `a PR keeps its badge after a later workout beats it`() {
        val rows = listOf(
            row(workoutId = 1, exerciseId = 1, weightKg = 100f, reps = 5),
            row(workoutId = 2, exerciseId = 1, weightKg = 110f, reps = 5),
            row(workoutId = 3, exerciseId = 1, weightKg = 120f, reps = 5)
        )

        val prs = computeWorkoutPrs(rows, workoutId = 2)
        assertEquals(110f, prs.single { it.kind == PrKind.WEIGHT }.weightKg!!, 0.01f)
    }

    @Test
    fun `earlier means earlier start time, not a lower id`() {
        val rows = listOf(
            // Workout 5 was logged (e.g. imported) later but happened first.
            row(workoutId = 5, exerciseId = 1, weightKg = 100f, reps = 5, startTime = 1_000),
            row(workoutId = 2, exerciseId = 1, weightKg = 100f, reps = 5, startTime = 2_000)
        )

        assertTrue(computeWorkoutPrs(rows, workoutId = 5).any { it.kind == PrKind.WEIGHT })
        assertNull(computeWorkoutPrs(rows, workoutId = 2).firstOrNull { it.kind == PrKind.WEIGHT })
    }
}
