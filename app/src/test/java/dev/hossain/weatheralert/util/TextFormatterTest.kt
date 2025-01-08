package dev.hossain.weatheralert.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TextFormatterTest {
    @Test
    fun formatUnit_handlesFloatValue() {
        val value = 2.440923834343f
        val unit = "cm"
        val expected = "2.44 cm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesDoubleValue() {
        val value = 8.98237320
        val unit = "mm"
        val expected = "8.98 mm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesZeroFloatValue() {
        val value = 0.0f
        val unit = "cm"
        val expected = "0.00 cm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesZeroDoubleValue() {
        val value = 0.0
        val unit = "mm"
        val expected = "0.00 mm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesNegativeFloatValue() {
        val value = -2.440923834343f
        val unit = "cm"
        val expected = "-2.44 cm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesNegativeDoubleValue() {
        val value = -8.98237320
        val unit = "mm"
        val expected = "-8.98 mm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesLargeFloatValue() {
        val value = 123456.78923f
        val unit = "cm"
        val expected = "123456.79 cm"
        assertEquals(expected, value.formatUnit(unit))
    }

    @Test
    fun formatUnit_handlesLargeDoubleValue() {
        val value = 987654.321
        val unit = "mm"
        val expected = "987654.32 mm"
        assertEquals(expected, value.formatUnit(unit))
    }
}
