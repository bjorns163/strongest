package com.strongest.app

import org.junit.Assert.assertEquals
import org.junit.Test
import com.strongest.app.utils.OneRepMaxCalculator
import com.strongest.app.utils.PlateCalculator
import com.strongest.app.utils.WarmupCalculator

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

    @Test
    fun `Plate calculation for 100kg with 20kg bar`() {
        val plates = PlateCalculator.calculatePlates(100f, true)
        assertEquals(1, plates[20f])
        assertEquals(1, plates[5f])
    }

    @Test
    fun `Plate calculation for 60kg with 20kg bar`() {
        val plates = PlateCalculator.calculatePlates(60f, true)
        assertEquals(1, plates[10f])
    }

    @Test
    fun `Warmup sets calculation`() {
        val warmups = WarmupCalculator.calculateWarmupSets(100f, 5, true)
        assert(warmups.isNotEmpty())
        assert(warmups.last().isWorkingSet)
        assertEquals(100f, warmups.last().weight, 0.01f)
        assertEquals(5, warmups.last().reps)
    }
}
