package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeUtilTest {
    private val timeUtil: TimeUtil = TimeUtilImpl()
    private val clockProvider: ClockProvider = DefaultClockProvider()

    @Test
    fun testGetCurrentTimeMillis() {
        val currentTime = System.currentTimeMillis()
        assertThat(timeUtil.getCurrentTimeMillis(clockProvider.getClock()) >= currentTime).isTrue()
    }

    @Test
    fun testIs1HourOld() {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 1 * 60 * 60 * 1000
        assertThat(
            timeUtil.isOlderThan24Hours(
                clock = clockProvider.getClock(),
                timeInMillis = currentTime - oneHourInMillis,
            ),
        ).isFalse()
    }

    @Test
    fun testIsLessThan24HoursOld() {
        val currentTime = System.currentTimeMillis()
        val twentyThreeHoursInMillis = 23 * 60 * 60 * 1000
        assertThat(
            timeUtil.isOlderThan24Hours(
                clock = clockProvider.getClock(),
                timeInMillis = currentTime - twentyThreeHoursInMillis,
            ),
        ).isFalse()
    }

    @Test
    fun testIsMoreThan24HoursOld() {
        val currentTime = System.currentTimeMillis()
        val fortyEightHoursInMillis = 48 * 60 * 60 * 1000
        assertThat(
            timeUtil.isOlderThan24Hours(
                clock = clockProvider.getClock(),
                timeInMillis = currentTime - fortyEightHoursInMillis,
            ),
        ).isTrue()
    }
}
