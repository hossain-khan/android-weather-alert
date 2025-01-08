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
    fun testIsOlderThan24Hours() {
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000

        // Test with a time that is exactly 24 hours old
        assertFalse(timeUtil.isOlderThan24Hours(currentTime - twentyFourHoursInMillis))

        // Test with a time that is more than 24 hours old
        assertTrue(timeUtil.isOlderThan24Hours(currentTime - twentyFourHoursInMillis - 1))

        // Test with a time that is less than 24 hours old
        assertFalse(timeUtil.isOlderThan24Hours(currentTime - twentyFourHoursInMillis + 1))
    }
}
