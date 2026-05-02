package dev.hossain.weatheralert.datamodel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [WeatherAlertCategory] enum class properties and values.
 */
class WeatherAlertCategoryTest {
    @Test
    fun `SNOW_FALL has correct label`() {
        assertThat(WeatherAlertCategory.SNOW_FALL.label).isEqualTo("Snow")
    }

    @Test
    fun `SNOW_FALL has correct unit`() {
        assertThat(WeatherAlertCategory.SNOW_FALL.unit).isEqualTo("mm")
    }

    @Test
    fun `RAIN_FALL has correct label`() {
        assertThat(WeatherAlertCategory.RAIN_FALL.label).isEqualTo("Rain")
    }

    @Test
    fun `RAIN_FALL has correct unit`() {
        assertThat(WeatherAlertCategory.RAIN_FALL.unit).isEqualTo("mm")
    }

    @Test
    fun `WeatherAlertCategory has exactly two values`() {
        assertThat(WeatherAlertCategory.entries).hasSize(2)
    }

    @Test
    fun `WeatherAlertCategory entries contain SNOW_FALL and RAIN_FALL`() {
        val categories = WeatherAlertCategory.entries.map { it.name }
        assertThat(categories).containsExactly("SNOW_FALL", "RAIN_FALL")
    }

    @Test
    fun `SNOW_FALL valueOf resolves correctly`() {
        val category = WeatherAlertCategory.valueOf("SNOW_FALL")
        assertThat(category).isEqualTo(WeatherAlertCategory.SNOW_FALL)
    }

    @Test
    fun `RAIN_FALL valueOf resolves correctly`() {
        val category = WeatherAlertCategory.valueOf("RAIN_FALL")
        assertThat(category).isEqualTo(WeatherAlertCategory.RAIN_FALL)
    }
}
