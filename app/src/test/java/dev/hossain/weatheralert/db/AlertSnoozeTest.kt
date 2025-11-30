package dev.hossain.weatheralert.db

import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import org.junit.Test

class AlertSnoozeTest {
    @Test
    fun `isSnoozed returns false when snoozedUntil is null`() {
        val alert =
            Alert(
                id = 1,
                cityId = 1,
                alertCategory = WeatherAlertCategory.SNOW_FALL,
                threshold = 10.0f,
                snoozedUntil = null,
            )

        assertThat(alert.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns false when snoozedUntil is in the past`() {
        val alert =
            Alert(
                id = 1,
                cityId = 1,
                alertCategory = WeatherAlertCategory.SNOW_FALL,
                threshold = 10.0f,
                snoozedUntil = System.currentTimeMillis() - 1000, // 1 second ago
            )

        assertThat(alert.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns true when snoozedUntil is in the future`() {
        val alert =
            Alert(
                id = 1,
                cityId = 1,
                alertCategory = WeatherAlertCategory.SNOW_FALL,
                threshold = 10.0f,
                snoozedUntil = System.currentTimeMillis() + 60 * 60 * 1000, // 1 hour in the future
            )

        assertThat(alert.isSnoozed()).isTrue()
    }

    @Test
    fun `alert with default snoozedUntil is not snoozed`() {
        val alert =
            Alert(
                id = 1,
                cityId = 1,
                alertCategory = WeatherAlertCategory.RAIN_FALL,
                threshold = 5.0f,
            )

        assertThat(alert.isSnoozed()).isFalse()
        assertThat(alert.snoozedUntil).isNull()
    }
}
