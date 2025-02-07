package com.weatherapi.api

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.weatherapi.api.model.ForecastWeatherResponse
import dev.hossain.weatheralert.datamodel.AppForecastData
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/**
 * Tests WeatherAPI [ForecastWeatherResponse] to [AppForecastData] converter.
 */
class WeatherApiResponseConverterTest {
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
    fun convertsBuffaloWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-buffalo-2025-02-05.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(42.8864)
        assertThat(result.longitude).isEqualTo(-78.8786)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsEdmontonWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-edmonton-2025-02-05.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(53.55)
        assertThat(result.longitude).isEqualTo(-113.5)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsMaringaWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-maringa-brazil-2025-02-05-rain.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(-23.4167)
        assertThat(result.longitude).isEqualTo(-51.9167)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.96)
        assertThat(result.rain.nextDayRain).isEqualTo(50.09)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsUozoWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-uozo-japan-2025-02-05-snow.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(36.8)
        assertThat(result.longitude).isEqualTo(137.4)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(122.80000000000001)
        assertThat(result.snow.nextDaySnow).isEqualTo(214.20000000000002)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(31.350000000000005)
        assertThat(result.rain.nextDayRain).isEqualTo(37.37)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
    }

    @Test
    fun convertsOshawaNotMuchSnowWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-oshawa-2025-02-06-no-snow.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(43.9)
        assertThat(result.longitude).isEqualTo(-78.867)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
        assertThat(result.hourlyPrecipitation).hasSize(25)
    }

    // Helper method to load WeatherResponse from JSON
    private fun loadWeatherResponseFromJson(fileName: String): ForecastWeatherResponse {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        val json = inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
        return Moshi
            .Builder()
            .build()
            .adapter(ForecastWeatherResponse::class.java)
            .fromJson(json)!!
    }
}
