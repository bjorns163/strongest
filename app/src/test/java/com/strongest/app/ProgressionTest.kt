package com.strongest.app

import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.ProgressionConfig
import com.strongest.app.utils.ProgressionMethod
import com.strongest.app.utils.ProgressionSession
import com.strongest.app.utils.ProgressionSet
import com.strongest.app.utils.ProgressionTarget
import com.strongest.app.utils.SuggestionKind
import com.strongest.app.utils.defaultWeightStepKg
import com.strongest.app.utils.implicitProgressionTarget
import com.strongest.app.utils.selectProgressionTrack
import com.strongest.app.utils.suggestProgression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressionTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 100 * day
    private val config = ProgressionConfig()
    private val range = ProgressionTarget(repMin = 8, repMax = 12, targetRpe = 8)

    private fun set(w: Float, r: Int, rpe: Float? = null, type: SetType = SetType.NORMAL) =
        ProgressionSet(w, r, rpe, type)

    private fun session(
        daysAgo: Int,
        vararg sets: ProgressionSet,
        routineId: Long? = null,
        id: Long = daysAgo.toLong()
    ) = ProgressionSession(id, routineId, now - daysAgo * day, sets.toList())

    private fun suggest(
        vararg sessions: ProgressionSession,
        method: ProgressionMethod = ProgressionMethod.DOUBLE,
        step: Float = 2.5f,
        cfg: ProgressionConfig = config,
        target: ProgressionTarget = range
    ) = suggestProgression(sessions.toList(), method, target, step, cfg, now)

    @Test
    fun `no history gives no suggestion`() {
        assertNull(suggest())
    }

    @Test
    fun `turned off gives no suggestion`() {
        val s = session(3, set(60f, 12), set(60f, 12))
        assertNull(suggest(s, method = ProgressionMethod.OFF))
        assertNull(suggest(s, cfg = config.copy(enabled = false)))
    }

    @Test
    fun `hit top of range at or under RPE 9 adds a weight step and drops to bottom of range`() {
        val s = session(3, set(60f, 12, 9f), set(60f, 12, 8f), set(60f, 12, 9f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.INCREASE_WEIGHT, result.kind)
        assertEquals(62.5f, result.weightKg, 0.001f)
        assertEquals(8, result.reps)
    }

    @Test
    fun `hit top of range at RPE 10 repeats`() {
        val s = session(3, set(60f, 12, 10f), set(60f, 12, 10f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.REPEAT, result.kind)
        assertEquals(60f, result.weightKg, 0.001f)
        assertEquals(12, result.reps)
    }

    @Test
    fun `within range at RPE 7 adds two reps`() {
        val s = session(3, set(60f, 9, 7f), set(60f, 9, 7f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
        assertEquals(11, result.reps)
    }

    @Test
    fun `within range on target adds one rep`() {
        val s = session(3, set(60f, 9, 8f), set(60f, 10, 9f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
        // Lowest set decides: 9 → 10
        assertEquals(10, result.reps)
    }

    @Test
    fun `within range at RPE 10 repeats`() {
        val s = session(3, set(60f, 10, 10f), set(60f, 10, 10f))
        assertEquals(SuggestionKind.REPEAT, suggest(s)!!.kind)
    }

    @Test
    fun `extra reps never go past the top of the range`() {
        val s = session(3, set(60f, 11, 7f), set(60f, 11, 7f))
        assertEquals(12, suggest(s)!!.reps)
    }

    @Test
    fun `below range at RPE 10 drops weight by whole steps`() {
        val s = session(3, set(100f, 6, 10f), set(100f, 5, 10f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.DECREASE, result.kind)
        // 5% of 100 = 5 kg = two 2.5 kg steps
        assertEquals(95f, result.weightKg, 0.001f)
        assertEquals(8, result.reps)
    }

    @Test
    fun `below range without RPE also drops`() {
        val s = session(3, set(100f, 6), set(100f, 6))
        val cfg = config.copy(useRpe = false)
        assertEquals(SuggestionKind.DECREASE, suggest(s, cfg = cfg)!!.kind)
    }

    @Test
    fun `below range but easy aims for bottom of range at same weight`() {
        val s = session(3, set(100f, 7, 8f), set(100f, 7, 8f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
        assertEquals(100f, result.weightKg, 0.001f)
        assertEquals(8, result.reps)
    }

    @Test
    fun `RPE is ignored when turned off`() {
        val s = session(3, set(60f, 12, 10f), set(60f, 12, 10f))
        val result = suggest(s, cfg = config.copy(useRpe = false))!!
        assertEquals(SuggestionKind.INCREASE_WEIGHT, result.kind)
    }

    @Test
    fun `warm-ups and drop sets are left out`() {
        val s = session(
            3,
            set(20f, 12, type = SetType.WARM_UP),
            set(60f, 12, 8f),
            set(60f, 12, 8f),
            set(40f, 5, 10f, SetType.DROP_SET)
        )
        val result = suggest(s)!!
        assertEquals(SuggestionKind.INCREASE_WEIGHT, result.kind)
        assertEquals(2, result.basis.sets)
    }

    @Test
    fun `back-off sets at a lower weight don't block progress`() {
        val s = session(3, set(100f, 12, 8f), set(100f, 12, 9f), set(80f, 9, 8f))
        assertEquals(SuggestionKind.INCREASE_WEIGHT, suggest(s)!!.kind)
    }

    @Test
    fun `long break eases back in instead of increasing`() {
        val s = session(30, set(60f, 12, 8f), set(60f, 12, 8f))
        val result = suggest(s)!!
        assertEquals(SuggestionKind.LONG_BREAK, result.kind)
        assertEquals(57.5f, result.weightKg, 0.001f)
    }

    @Test
    fun `alternating every other week is not a long break`() {
        val s = session(14, set(60f, 12, 8f), set(60f, 12, 8f))
        assertEquals(SuggestionKind.INCREASE_WEIGHT, suggest(s)!!.kind)
    }

    @Test
    fun `long break threshold comes from settings`() {
        val s = session(14, set(60f, 12, 8f))
        val result = suggest(s, cfg = config.copy(longBreakDays = 10))!!
        assertEquals(SuggestionKind.LONG_BREAK, result.kind)
    }

    @Test
    fun `stuck at the same weight suggests a deload`() {
        val result = suggest(
            session(2, set(100f, 9, 9f), set(100f, 9, 9f)),
            session(5, set(100f, 9, 9f), set(100f, 9, 9f)),
            session(9, set(100f, 9, 9f), set(100f, 9, 9f))
        )!!
        assertEquals(SuggestionKind.DELOAD, result.kind)
        assertEquals(90f, result.weightKg, 0.001f)
        assertEquals(3, result.basis.stalledSessions)
    }

    @Test
    fun `reps going up at the same weight is progress, not a plateau`() {
        val result = suggest(
            session(2, set(100f, 10, 8f)),
            session(5, set(100f, 9, 8f)),
            session(9, set(100f, 8, 8f))
        )!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
    }

    @Test
    fun `plateau detection can be turned off`() {
        val result = suggest(
            session(2, set(100f, 9, 9f)),
            session(5, set(100f, 9, 9f)),
            session(9, set(100f, 9, 9f)),
            cfg = config.copy(plateauSessions = 0)
        )!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
    }

    @Test
    fun `after a deload the plateau count starts over`() {
        val result = suggest(
            session(2, set(90f, 9, 8f)),
            session(5, set(100f, 9, 9f)),
            session(9, set(100f, 9, 9f)),
            session(12, set(100f, 9, 9f))
        )!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
    }

    @Test
    fun `bodyweight exercises progress by reps past the top of the range`() {
        val s = session(3, set(0f, 12, 8f), set(0f, 12, 8f))
        val result = suggest(s, step = 0f)!!
        assertEquals(SuggestionKind.ADD_REPS, result.kind)
        assertEquals(13, result.reps)
        assertEquals(0f, result.weightKg, 0.001f)
    }

    @Test
    fun `linear keeps the reps and adds weight`() {
        val s = session(3, set(100f, 5, 8f), set(100f, 5, 8f))
        val target = ProgressionTarget(5, 5, 8)
        val result = suggest(s, method = ProgressionMethod.LINEAR, target = target)!!
        assertEquals(SuggestionKind.INCREASE_WEIGHT, result.kind)
        assertEquals(102.5f, result.weightKg, 0.001f)
        assertEquals(5, result.reps)
    }

    @Test
    fun `linear repeats a session that fell short`() {
        val s = session(3, set(100f, 5, 9f), set(100f, 4, 9f))
        val target = ProgressionTarget(5, 5, 8)
        val result = suggest(s, method = ProgressionMethod.LINEAR, target = target)!!
        assertEquals(SuggestionKind.REPEAT, result.kind)
        assertEquals(5, result.reps)
    }

    @Test
    fun `same routine is followed first so heavy and light days keep their own track`() {
        val heavy = session(7, set(100f, 5, 8f), routineId = 1, id = 1)
        val light = session(3, set(70f, 12, 8f), routineId = 2, id = 2)
        val track = selectProgressionTrack(listOf(light, heavy), routineId = 1)
        assertEquals(listOf(heavy), track)
    }

    @Test
    fun `without a matching routine the latest session overall is used`() {
        val a = session(7, set(100f, 5), routineId = 1, id = 1)
        val b = session(3, set(70f, 12), routineId = 2, id = 2)
        assertEquals(listOf(b, a), selectProgressionTrack(listOf(a, b), routineId = 9))
        assertEquals(listOf(b, a), selectProgressionTrack(listOf(a, b), routineId = null))
    }

    @Test
    fun `sessions with only warm-ups are skipped`() {
        val warmOnly = session(1, set(20f, 10, type = SetType.WARM_UP), id = 1)
        val real = session(5, set(60f, 10), id = 2)
        assertEquals(listOf(real), selectProgressionTrack(listOf(warmOnly, real), null))
    }

    @Test
    fun `implicit target keeps a 5 rep lifter at 5 reps`() {
        val target = implicitProgressionTarget(5, config)
        assertEquals(5, target.repMin)
        assertEquals(7, target.repMax)
        assertEquals(ProgressionTarget(8, 12, 8), implicitProgressionTarget(10, config))
        assertEquals(ProgressionTarget(8, 12, 8), implicitProgressionTarget(null, config))
    }

    @Test
    fun `weight steps follow the equipment`() {
        assertEquals(2.5f, defaultWeightStepKg(Equipment.BARBELL, WeightUnit.KG, 60f), 0.001f)
        assertEquals(2f, defaultWeightStepKg(Equipment.DUMBBELL, WeightUnit.KG, 20f), 0.001f)
        assertEquals(5f, defaultWeightStepKg(Equipment.MACHINE, WeightUnit.KG, 50f), 0.001f)
        assertEquals(0f, defaultWeightStepKg(Equipment.BODYWEIGHT, WeightUnit.KG, 0f), 0.001f)
        assertEquals(2.5f, defaultWeightStepKg(Equipment.BODYWEIGHT, WeightUnit.KG, 10f), 0.001f)
        // 5 lb in kg
        assertEquals(2.268f, defaultWeightStepKg(Equipment.BARBELL, WeightUnit.LBS, 60f), 0.001f)
    }
}
