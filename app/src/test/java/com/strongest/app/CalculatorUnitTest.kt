package com.strongest.app

import org.junit.Assert.assertEquals
import org.junit.Test
import com.strongest.app.utils.OneRepMaxCalculator

class CalculatorUnitTest {

    @Test
    fun `1RM calculation for single rep returns same weight`() {
        assertEquals(100f, OneRepMaxCalculator.epley(100f, 1), 0.01f)
        assertEquals(100f, OneRepMaxCalculator.brzycki(100f, 1), 0.01f)
    }

    @Test
    fun `1RM Epley formula calculation`() {
        assertEquals(133.33f, OneRepMaxCalculator.epley(100f, 10), 0.5f)
    }

    @Test
    fun `1RM Brzycki formula calculation`() {
        assertEquals(133.33f, OneRepMaxCalculator.brzycki(100f, 10), 0.5f)
    }
}
