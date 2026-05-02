package dev.hossain.weatheralert.work

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for work scheduler constants in [Scheduler.kt].
 */
class SchedulerConstantsTest {
    @Test
    fun `WEATHER_UPDATE_INTERVAL_6_HOURS is 6`() {
        assertThat(WEATHER_UPDATE_INTERVAL_6_HOURS).isEqualTo(6L)
    }

    @Test
    fun `WEATHER_UPDATE_INTERVAL_12_HOURS is 12`() {
        assertThat(WEATHER_UPDATE_INTERVAL_12_HOURS).isEqualTo(12L)
    }

    @Test
    fun `WEATHER_UPDATE_INTERVAL_18_HOURS is 18`() {
        assertThat(WEATHER_UPDATE_INTERVAL_18_HOURS).isEqualTo(18L)
    }

    @Test
    fun `DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS is 12 hours`() {
        assertThat(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS).isEqualTo(WEATHER_UPDATE_INTERVAL_12_HOURS)
    }

    @Test
    fun `supportedWeatherUpdateInterval contains all three intervals`() {
        assertThat(supportedWeatherUpdateInterval).containsExactly(
            WEATHER_UPDATE_INTERVAL_6_HOURS,
            WEATHER_UPDATE_INTERVAL_12_HOURS,
            WEATHER_UPDATE_INTERVAL_18_HOURS,
        )
    }

    @Test
    fun `supportedWeatherUpdateInterval has exactly 3 intervals`() {
        assertThat(supportedWeatherUpdateInterval).hasSize(3)
    }

    @Test
    fun `supportedWeatherUpdateInterval is ordered from smallest to largest`() {
        val intervals = supportedWeatherUpdateInterval
        assertThat(intervals[0]).isLessThan(intervals[1])
        assertThat(intervals[1]).isLessThan(intervals[2])
    }

    @Test
    fun `default interval is within supported intervals`() {
        assertThat(supportedWeatherUpdateInterval).contains(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS)
    }
}
