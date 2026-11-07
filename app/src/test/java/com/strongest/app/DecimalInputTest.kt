package com.strongest.app

import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.formatDecimal
import com.strongest.app.utils.parseDecimalInput
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Regression: a Decimal keypad offers the device locale's separator, so weight entry on a
 * comma-decimal device (nl-NL, de-DE, fr-FR) fed `toFloatOrNull` a string it could not parse.
 * Every caller treated the resulting null as "no change", so the field kept its old value while
 * displaying the newly typed text.
 */
class DecimalInputTest {

    private lateinit var original: Locale

    @Before
    fun setUp() {
        original = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(original)
    }

    @Test
    fun `parses a dot separator`() {
        assertEquals(82.5f, parseDecimalInput("82.5")!!, 0.001f)
    }

    @Test
    fun `parses a comma separator`() {
        assertEquals(82.5f, parseDecimalInput("82,5")!!, 0.001f)
    }

    @Test
    fun `parses whole numbers and surrounding whitespace`() {
        assertEquals(100f, parseDecimalInput("100")!!, 0.001f)
        assertEquals(100f, parseDecimalInput("  100  ")!!, 0.001f)
    }

    @Test
    fun `parses a trailing separator mid-typing`() {
        // The user has typed "82," and has not reached the decimal yet.
        assertEquals(82f, parseDecimalInput("82,")!!, 0.001f)
    }

    @Test
    fun `returns null for input that is not a number`() {
        assertNull(parseDecimalInput(""))
        assertNull(parseDecimalInput("abc"))
        assertNull(parseDecimalInput("8,2,5"))
    }

    @Test
    fun `display format round-trips through the parser in a comma-decimal locale`() {
        Locale.setDefault(Locale.forLanguageTag("nl-NL"))
        val formatted = formatWeightForDisplay(82.5f, WeightUnit.KG)
        assertEquals("82,5", formatted)
        assertEquals(82.5f, parseDecimalInput(formatted)!!, 0.001f)
    }

    @Test
    fun `display format round-trips through the parser in a dot-decimal locale`() {
        Locale.setDefault(Locale.US)
        val formatted = formatWeightForDisplay(82.5f, WeightUnit.KG)
        assertEquals("82.5", formatted)
        assertEquals(82.5f, parseDecimalInput(formatted)!!, 0.001f)
    }

    @Test
    fun `weights show up to two decimals, only as many as needed`() {
        // Regression: everything was rounded to one decimal, so a 1.25 kg plate showed as 1.3.
        Locale.setDefault(Locale.US)
        assertEquals("1.25", formatWeightForDisplay(1.25f, WeightUnit.KG))
        assertEquals("2.5", formatWeightForDisplay(2.5f, WeightUnit.KG))
        assertEquals("100", formatWeightForDisplay(100f, WeightUnit.KG))
        assertEquals("61.25", formatWeightForDisplay(61.25f, WeightUnit.KG))
        assertEquals("60.3", formatWeightForDisplay(60.3f, WeightUnit.KG))
        assertEquals("1250", formatWeightForDisplay(1250f, WeightUnit.KG))
    }

    @Test
    fun `two-decimal weights round-trip in a comma-decimal locale`() {
        Locale.setDefault(Locale.forLanguageTag("nl-NL"))
        val formatted = formatWeightForDisplay(1.25f, WeightUnit.KG)
        assertEquals("1,25", formatted)
        assertEquals(1.25f, parseDecimalInput(formatted)!!, 0.0001f)
    }

    @Test
    fun `formatDecimal honours a smaller decimal cap for estimates`() {
        Locale.setDefault(Locale.US)
        assertEquals("117.3", formatDecimal(117.25f, maxDecimals = 1))
        assertEquals("117", formatDecimal(117.0f, maxDecimals = 1))
    }
}
