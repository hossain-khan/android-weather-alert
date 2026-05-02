package dev.hossain.weatheralert.data

import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [AlertTileData.isSnoozed] function.
 */
@RunWith(RobolectricTestRunner::class)
class AlertTileDataTest {
    @Test
    fun `isSnoozed returns false when snoozedUntil is null`() {
        val alertTileData =
            AlertTileData(
                alertId = 1L,
                cityInfo = "Toronto, CA",
                lat = 43.7,
                lon = -79.42,
                category = WeatherAlertCategory.SNOW_FALL,
                threshold = "10 mm",
                currentStatus = "5 mm",
                isAlertActive = false,
                alertNote = "",
                forecastSourceName = "OpenWeather",
                snoozedUntil = null,
            )

        assertThat(alertTileData.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns false when snoozedUntil is in the past`() {
        val pastTimestamp = System.currentTimeMillis() - 60 * 60 * 1000L // 1 hour ago
        val alertTileData =
            AlertTileData(
                alertId = 2L,
                cityInfo = "Vancouver, CA",
                lat = 49.2827,
                lon = -123.1207,
                category = WeatherAlertCategory.RAIN_FALL,
                threshold = "15 mm",
                currentStatus = "8 mm",
                isAlertActive = false,
                alertNote = "Check weather",
                forecastSourceName = "WeatherAPI",
                snoozedUntil = pastTimestamp,
            )

        assertThat(alertTileData.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns true when snoozedUntil is in the future`() {
        val futureTimestamp = System.currentTimeMillis() + 60 * 60 * 1000L // 1 hour from now
        val alertTileData =
            AlertTileData(
                alertId = 3L,
                cityInfo = "Montreal, CA",
                lat = 45.5017,
                lon = -73.5673,
                category = WeatherAlertCategory.SNOW_FALL,
                threshold = "20 mm",
                currentStatus = "25 mm",
                isAlertActive = true,
                alertNote = "",
                forecastSourceName = "Tomorrow.io",
                snoozedUntil = futureTimestamp,
            )

        assertThat(alertTileData.isSnoozed()).isTrue()
    }

    @Test
    fun `isSnoozed returns true when snoozedUntil is one week in the future`() {
        val oneWeekFromNow = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
        val alertTileData =
            AlertTileData(
                alertId = 4L,
                cityInfo = "Calgary, CA",
                lat = 51.0447,
                lon = -114.0719,
                category = WeatherAlertCategory.SNOW_FALL,
                threshold = "5 mm",
                currentStatus = "0 mm",
                isAlertActive = false,
                alertNote = "Winter tires",
                forecastSourceName = "OpenWeather",
                snoozedUntil = oneWeekFromNow,
            )

        assertThat(alertTileData.isSnoozed()).isTrue()
    }

    @Test
    fun `isAlertActive is false when weather does not exceed threshold`() {
        val alertTileData =
            AlertTileData(
                alertId = 5L,
                cityInfo = "Ottawa, CA",
                lat = 45.4215,
                lon = -75.6972,
                category = WeatherAlertCategory.RAIN_FALL,
                threshold = "20 mm",
                currentStatus = "5 mm",
                isAlertActive = false,
                alertNote = "",
                forecastSourceName = "WeatherAPI",
                snoozedUntil = null,
            )

        assertThat(alertTileData.isAlertActive).isFalse()
        assertThat(alertTileData.isSnoozed()).isFalse()
    }
}
