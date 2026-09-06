package com.strongest.app

import com.strongest.app.utils.WarmUpCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class WarmUpCalculatorTest {

    @Test
    fun `a heavy working weight supports the full scheme`() {
        assertEquals(4, WarmUpCalculator.maxAchievable(100f, 5, 2.5f))
        assertEquals(4, WarmUpCalculator.suggest(100f, 5, 2.5f, 4).size)
    }

    @Test
    fun `a light working weight supports fewer sets than requested`() {
        // The complaint: ask for 4, get 2, because the steps round onto each other.
        val suggested = WarmUpCalculator.suggest(10f, 5, 2.5f, 4)
        assertEquals(2, suggested.size)
        assertEquals(2, WarmUpCalculator.maxAchievable(10f, 5, 2.5f))
    }

    @Test
    fun `the lightest weights support no warm-up at all`() {
        assertEquals(0, WarmUpCalculator.maxAchievable(2.5f, 5, 2.5f))
    }

    @Test
    fun `steps climb and stay below the working weight`() {
        val suggested = WarmUpCalculator.suggest(100f, 5, 2.5f, 4)
        val weights = suggested.map { it.weight }
        assertEquals(weights.sortedBy { it }, weights)
        assertEquals(emptyList<Float>(), weights.filter { it >= 100f })
    }

    @Test
    fun `warm-up reps never exceed the working reps`() {
        val suggested = WarmUpCalculator.suggest(100f, 2, 2.5f, 4)
        assertEquals(emptyList<Int>(), suggested.map { it.reps }.filter { it > 2 })
    }

    @Test
    fun `asking for fewer sets takes the lightest steps`() {
        val two = WarmUpCalculator.suggest(100f, 5, 2.5f, 2)
        val four = WarmUpCalculator.suggest(100f, 5, 2.5f, 4)
        assertEquals(2, two.size)
        assertEquals(four.take(2), two)
    }

    @Test
    fun `invalid input yields nothing`() {
        assertEquals(0, WarmUpCalculator.suggest(0f, 5, 2.5f, 4).size)
        assertEquals(0, WarmUpCalculator.suggest(100f, 0, 2.5f, 4).size)
        assertEquals(0, WarmUpCalculator.suggest(100f, 5, 2.5f, 0).size)
    }
}
