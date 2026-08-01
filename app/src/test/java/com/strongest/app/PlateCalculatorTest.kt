package com.strongest.app

import com.strongest.app.ui.workout.calculatePlates
import org.junit.Assert.assertEquals
import org.junit.Test

class PlateCalculatorTest {

    @Test
    fun `owned plates are capped at half per side for a barbell`() {
        val result = calculatePlates(target = 140f, bar = 20f, availablePlates = mapOf(20f to 4))

        assertEquals(listOf(20f to 2), result.perSide)
        assertEquals(100f, result.achievedTotal)
        assertEquals(40f, result.remainder)
    }

    @Test
    fun `single side uses the full owned quantity`() {
        val result = calculatePlates(target = 100f, bar = 20f, availablePlates = mapOf(20f to 4), singleSide = true)

        assertEquals(listOf(20f to 4), result.perSide)
        assertEquals(100f, result.achievedTotal)
        assertEquals(0f, result.remainder)
    }

    @Test
    fun `odd owned quantity leaves one plate unused`() {
        val result = calculatePlates(target = 140f, bar = 20f, availablePlates = mapOf(20f to 3))

        assertEquals(listOf(20f to 1), result.perSide)
        assertEquals(60f, result.achievedTotal)
        assertEquals(80f, result.remainder)
    }

    @Test
    fun `mixes plate sizes once the per-side cap is reached`() {
        val result = calculatePlates(target = 140f, bar = 20f, availablePlates = mapOf(20f to 4, 10f to 4))

        assertEquals(listOf(20f to 2, 10f to 2), result.perSide)
        assertEquals(140f, result.achievedTotal)
        assertEquals(0f, result.remainder)
    }

    @Test
    fun `unlimited plates are still unlimited per side`() {
        val result = calculatePlates(target = 140f, bar = 20f, availablePlates = mapOf(20f to 999))

        assertEquals(listOf(20f to 3), result.perSide)
        assertEquals(140f, result.achievedTotal)
        assertEquals(0f, result.remainder)
    }
}
