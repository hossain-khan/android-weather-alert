package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeUtilTest {
    private val timeUtil: TimeUtil = TimeUtilImpl(DefaultClockProvider())

    @Test
    fun getCurrentTimeMillis_returnsCurrentSystemTime() {
        val currentTime = System.currentTimeMillis()
        assertThat(timeUtil.getCurrentTimeMillis() >= currentTime).isTrue()
    }

    @Test
    fun isOlderThan24Hours_returnsFalseForTimestamp1HourAgo() {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 1 * 60 * 60 * 1000
        assertThat(
            timeUtil.isOlderThan24Hours(
                timeInMillis = currentTime - oneHourInMillis,
            ),
        ).isFalse()
    }

    @Test
    fun isOlderThan24Hours_returnsFalseForTimestamp23HoursAgo() {
        val currentTime = System.currentTimeMillis()
        val twentyThreeHoursInMillis = 23 * 60 * 60 * 1000
        assertThat(
            timeUtil.isOlderThan24Hours(
                timeInMillis = currentTime - twentyThreeHoursInMillis,
            ),
        ).isFalse()
    }

    @Test
    fun isOlderThan24Hours_returnsFalseForTimestampExactly24HoursAgo() {
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L
        assertThat(
            timeUtil.isOlderThan24Hours(
                timeInMillis = currentTime - twentyFourHoursInMillis,
            ),
        ).isFalse()
    }

    @Test
    fun isOlderThan24Hours_returnsTrueForTimestamp48HoursAgo() {
        val currentTime = System.currentTimeMillis()
        val fortyEightHoursInMillis = 48 * 60 * 60 * 1000L
        assertThat(
            timeUtil.isOlderThan24Hours(
                timeInMillis = currentTime - fortyEightHoursInMillis,
            ),
        ).isTrue()
    }

    @Test
    fun isOlderThan24Hours_returnsTrueForTimestampJustOver24HoursAgo() {
        val currentTime = System.currentTimeMillis()
        val justOver24HoursInMillis = (24 * 60 * 60 * 1000L) + 1
        assertThat(
            timeUtil.isOlderThan24Hours(
                timeInMillis = currentTime - justOver24HoursInMillis,
            ),
        ).isTrue()
    }
}
