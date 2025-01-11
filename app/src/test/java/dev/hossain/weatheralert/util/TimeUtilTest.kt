package dev.hossain.weatheralert.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeUtilTest {
    private val timeUtil: TimeUtil = TimeUtilImpl()

    @Test
    fun testGetCurrentTimeMillis() {
        val currentTime = System.currentTimeMillis()
        assertTrue(timeUtil.getCurrentTimeMillis() >= currentTime)
    }

    @Test
    fun testIs1HourOld() {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 1 * 60 * 60 * 1000
        assertFalse(timeUtil.isOlderThan24Hours(currentTime - oneHourInMillis))
    }

    @Test
    fun testIsLessThan24HoursOld() {
        val currentTime = System.currentTimeMillis()
        val twentyThreeHoursInMillis = 23 * 60 * 60 * 1000
        assertFalse(timeUtil.isOlderThan24Hours(currentTime - twentyThreeHoursInMillis))
    }

    @Test
    fun testIsMoreThan24HoursOld() {
        val currentTime = System.currentTimeMillis()
        val fortyEightHoursInMillis = 48 * 60 * 60 * 1000
        assertTrue(timeUtil.isOlderThan24Hours(currentTime - fortyEightHoursInMillis))
    }
}
