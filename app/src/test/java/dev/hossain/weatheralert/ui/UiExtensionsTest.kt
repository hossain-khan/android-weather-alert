package dev.hossain.weatheralert.ui

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import org.junit.Test

/**
 * Tests for UI extension functions.
 */
class UiExtensionsTest {

    @Test
    fun `serviceConfig for OPEN_WEATHER_MAP returns correct config`() {
        val service = WeatherForecastService.OPEN_WEATHER_MAP
        val config = service.serviceConfig()

        assertThat(config.serviceName).isEqualTo("OpenWeather")
        assertThat(config.logoResId).isEqualTo(R.drawable.openweather_logo)
        assertThat(config.logoWidth).isEqualTo(100.dp)
        assertThat(config.logoHeight).isEqualTo(50.dp)
        assertThat(config.description).isEqualTo(
            "Free API service with larger usage limits. " +
                "However, it requires a credit card to activate the free API subscription."
        )
        assertThat(config.apiServiceUrl).isEqualTo("https://openweathermap.org/api")
        assertThat(config.apiServiceUrlLabel).isEqualTo("openweathermap.org")
        assertThat(config.apiExhaustedMessage).isEqualTo(
            "Unfortunately, the API key provided with the app has been exhausted.\n\n" +
                "To continue to use this app, you need to provide your own API key from OpenWeatherMap."
        )
        assertThat(config.apiFormatGuide).isEqualTo("API key should be 32 characters long and contain only hexadecimal characters.")
        assertThat(config.apiServiceProductName).isEqualTo("One Call API 3.0")
    }

    @Test
    fun `serviceConfig for TOMORROW_IO returns correct config`() {
        val service = WeatherForecastService.TOMORROW_IO
        val config = service.serviceConfig()

        assertThat(config.serviceName).isEqualTo("Tomorrow.io")
        assertThat(config.logoResId).isEqualTo(R.drawable.tomorrow_io_logo)
        assertThat(config.logoWidth).isEqualTo(120.dp)
        assertThat(config.logoHeight).isEqualTo(30.dp)
        assertThat(config.description).isEqualTo("Free API service with accurate data but limited usage limits. No credit card required.")
        assertThat(config.apiServiceUrl).isEqualTo("https://www.tomorrow.io/weather-api/")
        assertThat(config.apiServiceUrlLabel).isEqualTo("tomorrow.io")
        assertThat(config.apiExhaustedMessage).isEqualTo(
            "Unfortunately, the API key provided with the app has been exhausted.\n\n" +
                "To continue to use this app, you need to provide your own API key from Tomorrow.io."
        )
        assertThat(config.apiFormatGuide).isEqualTo("API key should be 32 characters long and contain only letters and numbers.")
        assertThat(config.apiServiceProductName).isEqualTo("Weather API")
    }

    @Test
    fun `serviceConfig for OPEN_METEO returns correct config`() {
        val service = WeatherForecastService.OPEN_METEO
        val config = service.serviceConfig()

        assertThat(config.serviceName).isEqualTo("Open-Meteo")
        assertThat(config.logoResId).isEqualTo(R.drawable.open_mateo_logo)
        assertThat(config.logoWidth).isEqualTo(140.dp)
        assertThat(config.logoHeight).isEqualTo(30.dp)
        assertThat(config.description).isEqualTo("Free API service with high limit. No service API key is required.")
        assertThat(config.apiServiceUrl).isEqualTo("https://open-meteo.com/en/docs")
        assertThat(config.apiServiceUrlLabel).isEqualTo("open-meteo.com")
        assertThat(config.apiExhaustedMessage).isEqualTo("Not applicable for Open-Meteo API.")
        assertThat(config.apiFormatGuide).isEqualTo("Not applicable.")
        assertThat(config.apiServiceProductName).isEqualTo("Weather API")
    }

    @Test
    fun `serviceConfig for WEATHER_API returns correct config`() {
        val service = WeatherForecastService.WEATHER_API
        val config = service.serviceConfig()

        assertThat(config.serviceName).isEqualTo("WeatherAPI")
        assertThat(config.logoResId).isEqualTo(R.drawable.weatherapi_logo)
        assertThat(config.logoWidth).isEqualTo(86.dp)
        assertThat(config.logoHeight).isEqualTo(40.dp)
        assertThat(config.description).isEqualTo("Free API service with high limit.")
        assertThat(config.apiServiceUrl).isEqualTo("https://www.weatherapi.com/docs/")
        assertThat(config.apiServiceUrlLabel).isEqualTo("weatherapi.com")
        assertThat(config.apiExhaustedMessage).isEqualTo("Not applicable for WeatherAPI.")
        assertThat(config.apiFormatGuide).isEqualTo("Not applicable.")
        assertThat(config.apiServiceProductName).isEqualTo("WeatherAPI")
    }
}
