package io.tomorrow.api

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse
import io.tomorrow.api.model.WeatherResponse
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/**
 * Tests tomorrow.io [WeatherResponse] to [AppForecastData] converter.
 */
class WeatherResponseConverterTest {
    private val originalTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        // Set the default time zone to a specific time zone (e.g., "America/Toronto")
        TimeZone.setDefault(TimeZone.getTimeZone("America/Toronto"))
    }

    @After
    fun tearDown() {
        // Restore the original time zone after the test
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun convertsBostonWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("tomorrow-io-boston-forecast-2025-01-10.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(42.3478)
        assertThat(result.longitude).isEqualTo(-71.0466)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(21.59)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsBostonWeatherResponseToAppForecastData_hourlyDateTimeUpdatedToLocalZone() {
        val weatherResponse = loadWeatherResponseFromJson("tomorrow-io-boston-forecast-2025-01-10.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.hourlyPrecipitation.size).isEqualTo(120)

        // First item is: 2025-01-11T03:00:00Z in UTC | Jan 11, 2025, 3:00:00 a.m.
        // In local timezone (EST), it should be 2025-01-10T22:00:00-05:00 | Jan 10, 2025, 10:00:00 p.m.
        assertThat(result.hourlyPrecipitation[0].isoDateTime).isEqualTo("2025-01-10T22:00:00-05:00")
    }

    @Test
    fun convertsOshawaWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("tomorrow-io-oshawa-forecast-2025-01-10.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(43.9)
        assertThat(result.longitude).isEqualTo(-78.85)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(55.040000000000006)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun handlesEmptyHourlyAndDailyForecasts() {
        val weatherResponse = loadWeatherResponseFromJson("tomorrow-weather-empty-forecasts.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(40.7128)
        assertThat(result.longitude).isEqualTo(-74.0060)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun handlesNullSnowAndRainVolumes() {
        val weatherResponse = loadWeatherResponseFromJson("tomorrow-weather-null-snow-rain.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(42.3478)
        assertThat(result.longitude).isEqualTo(-71.0466)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    // Helper method to load WeatherResponse from JSON
    private fun loadWeatherResponseFromJson(fileName: String): WeatherApiServiceResponse {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("File not found: $fileName")
        val json = inputStream.bufferedReader().use { it.readText() }
        return Moshi
            .Builder()
            .build()
            .adapter(WeatherResponse::class.java)
            .fromJson(json)!!
    }
}
