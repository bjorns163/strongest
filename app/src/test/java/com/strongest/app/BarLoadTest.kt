package com.strongest.app

import com.strongest.app.ui.workout.BarLoad
import com.strongest.app.ui.workout.add
import com.strongest.app.ui.workout.barLoadForTarget
import com.strongest.app.ui.workout.canAdd
import com.strongest.app.ui.workout.cleared
import com.strongest.app.ui.workout.constrainedTo
import com.strongest.app.ui.workout.countOf
import com.strongest.app.ui.workout.maxPerSide
import com.strongest.app.ui.workout.remove
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarLoadTest {

    @Test
    fun `an empty bar weighs the bar`() {
        assertEquals(20f, BarLoad(bar = 20f).total)
        assertTrue(BarLoad(bar = 20f).isEmpty)
    }

    @Test
    fun `plates are mirrored onto both sides of a barbell`() {
        val load = BarLoad(bar = 20f)
            .add(20f, owned = 999)
            .add(20f, owned = 999)
            .add(2.5f, owned = 999)

        assertEquals(42.5f, load.plateWeight)
        assertEquals(105f, load.total)
    }

    @Test
    fun `single side counts each plate once`() {
        val load = BarLoad(bar = 20f, singleSide = true)
            .add(10f, owned = 999)
            .add(10f, owned = 999)

        assertEquals(40f, load.total)
    }

    @Test
    fun `a barbell cannot use more than half the owned plates per side`() {
        assertEquals(2, maxPerSide(owned = 5, singleSide = false))
        assertEquals(5, maxPerSide(owned = 5, singleSide = true))

        var load = BarLoad(bar = 20f)
        repeat(4) { load = load.add(20f, owned = 4) }

        assertEquals(2, load.countOf(20f))
        assertFalse(load.canAdd(20f, owned = 4))
        assertEquals(100f, load.total)
    }

    @Test
    fun `plates you do not own cannot be added`() {
        val load = BarLoad(bar = 20f).add(25f, owned = 0)

        assertTrue(load.isEmpty)
        assertFalse(BarLoad(bar = 20f).canAdd(25f, owned = 0))
    }

    @Test
    fun `removing stops at zero and never goes negative`() {
        val load = BarLoad(bar = 20f).add(10f, owned = 999).remove(10f).remove(10f)

        assertEquals(0, load.countOf(10f))
        assertTrue(load.isEmpty)
    }

    @Test
    fun `clearing keeps the bar and drops the plates`() {
        val load = BarLoad(bar = 20f).add(20f, owned = 999).cleared()

        assertEquals(20f, load.bar)
        assertEquals(20f, load.total)
    }

    @Test
    fun `loaded plates are listed heaviest first`() {
        val load = BarLoad(bar = 20f)
            .add(2.5f, owned = 999)
            .add(20f, owned = 999)
            .add(10f, owned = 999)

        assertEquals(listOf(20f to 1, 10f to 1, 2.5f to 1), load.loaded)
    }

    @Test
    fun `switching to a barbell re-caps a load that only fits a single side`() {
        val singleSide = BarLoad(bar = 0f, singleSide = true)
            .add(20f, owned = 3)
            .add(20f, owned = 3)
            .add(20f, owned = 3)
        assertEquals(3, singleSide.countOf(20f))

        val asBarbell = singleSide.copy(singleSide = false).constrainedTo(mapOf(20f to 3))
        assertEquals(1, asBarbell.countOf(20f))
    }

    @Test
    fun `fill to target loads the plates that reach the weight`() {
        val load = barLoadForTarget(target = 100f, bar = 20f, availablePlates = mapOf(20f to 999, 10f to 999))

        assertEquals(listOf(20f to 2), load.loaded)
        assertEquals(100f, load.total)
    }

    @Test
    fun `fill to target stops short when the plates run out`() {
        val load = barLoadForTarget(target = 140f, bar = 20f, availablePlates = mapOf(20f to 4))

        assertEquals(listOf(20f to 2), load.loaded)
        assertEquals(100f, load.total)
    }
}
