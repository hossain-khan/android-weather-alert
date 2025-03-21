package org.openweathermap.api

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openweathermap.api.model.WeatherForecast
import java.util.TimeZone

/**
 * Tests [WeatherForecast] to [AppForecastData] conversion.
 */
class WeatherForecastConverterTest {
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
    fun convertsWeatherForecastToAppForecastData() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-hourly-snow-oshawa.json")

        val result = weatherForecast.convertToForecastData()

        assertThat(result.latitude).isEqualTo(43.9319)
        assertThat(result.longitude).isEqualTo(-78.851)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(38.7)
        assertThat(result.snow.nextDaySnow).isEqualTo(2.06)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsWeatherForecastToAppForecastData_withHourlyData() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-hourly-rain-colombo.json")

        val result = weatherForecast.convertToForecastData()

        // Validate hourly data
        assertThat(result.hourlyPrecipitation.size).isEqualTo(48)

        // First timestamp is 1738375200
        // GMT	Sat Feb 01 2025 02:00:00 GMT+0000
        // Your Time Zone Fri Jan 31 2025 21:00:00 GMT-0500 (Eastern Standard Time)
        // Expected: Unix timestamp should be converted to local time zone

        assertThat(result.hourlyPrecipitation[0].isoDateTime).isEqualTo("2025-01-31T21:00-05:00")
    }

    @Test
    fun handlesEmptyHourlyAndDailyForecasts() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-yazoo-city-mississippi-raining.json")

        val result = weatherForecast.convertToForecastData()

        assertThat(result.latitude).isEqualTo(32.864)
        assertThat(result.longitude).isEqualTo(-90.43)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(8.85)
        assertThat(result.rain.nextDayRain).isEqualTo(10.64)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun handlesNullSnowAndRainVolumes() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-null-snow-rain.json")

        val result = weatherForecast.convertToForecastData()

        assertThat(result.latitude).isEqualTo(10.20)
        assertThat(result.longitude).isEqualTo(-30.40)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun `given light snow on oshawa city further out - does not show snow in next 24 hours`() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-oshawa-2025-02-25-light-snow.json")

        val result = weatherForecast.convertToForecastData()

        assertThat(result.latitude).isEqualTo(43.9)
        assertThat(result.longitude).isEqualTo(-78.85)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.87)
        assertThat(result.rain.nextDayRain).isEqualTo(0.87)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    // Helper method to load WeatherForecast from JSON
    private fun loadWeatherForecastFromJson(fileName: String): WeatherApiServiceResponse {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        val json = inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
        return Moshi
            .Builder()
            .build()
            .adapter(WeatherForecast::class.java)
            .fromJson(json)!!
    }
}
