package dev.hossain.weatheralert.datamodel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [WeatherForecastService] enum class properties and values.
 */
class WeatherForecastServiceTest {
    @Test
    fun `OPEN_WEATHER_MAP requiresApiKey is true`() {
        assertThat(WeatherForecastService.OPEN_WEATHER_MAP.requiresApiKey).isTrue()
    }

    @Test
    fun `TOMORROW_IO requiresApiKey is true`() {
        assertThat(WeatherForecastService.TOMORROW_IO.requiresApiKey).isTrue()
    }

    @Test
    fun `OPEN_METEO requiresApiKey is false`() {
        assertThat(WeatherForecastService.OPEN_METEO.requiresApiKey).isFalse()
    }

    @Test
    fun `WEATHER_API requiresApiKey is false`() {
        assertThat(WeatherForecastService.WEATHER_API.requiresApiKey).isFalse()
    }

    @Test
    fun `OPEN_WEATHER_MAP isEnabled is true`() {
        assertThat(WeatherForecastService.OPEN_WEATHER_MAP.isEnabled).isTrue()
    }

    @Test
    fun `TOMORROW_IO isEnabled is true`() {
        assertThat(WeatherForecastService.TOMORROW_IO.isEnabled).isTrue()
    }

    @Test
    fun `WEATHER_API isEnabled is true`() {
        assertThat(WeatherForecastService.WEATHER_API.isEnabled).isTrue()
    }

    @Test
    fun `WeatherForecastService has exactly four values`() {
        assertThat(WeatherForecastService.entries).hasSize(4)
    }

    @Test
    fun `WeatherForecastService entries contain all expected services`() {
        val entries = WeatherForecastService.entries
        assertThat(entries).contains(WeatherForecastService.OPEN_WEATHER_MAP)
        assertThat(entries).contains(WeatherForecastService.TOMORROW_IO)
        assertThat(entries).contains(WeatherForecastService.OPEN_METEO)
        assertThat(entries).contains(WeatherForecastService.WEATHER_API)
    }

    @Test
    fun `services requiring API key are OPEN_WEATHER_MAP and TOMORROW_IO`() {
        val servicesRequiringKey = WeatherForecastService.entries.filter { it.requiresApiKey }
        assertThat(servicesRequiringKey).containsExactly(
            WeatherForecastService.OPEN_WEATHER_MAP,
            WeatherForecastService.TOMORROW_IO,
        )
    }

    @Test
    fun `services not requiring API key are OPEN_METEO and WEATHER_API`() {
        val servicesNotRequiringKey = WeatherForecastService.entries.filter { !it.requiresApiKey }
        assertThat(servicesNotRequiringKey).containsExactly(
            WeatherForecastService.OPEN_METEO,
            WeatherForecastService.WEATHER_API,
        )
    }
}
