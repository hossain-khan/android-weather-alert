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
 * Tests tomorrow.io [ForecastWeatherResponse] to [AppForecastData] converter.
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
    fun convertsBostonWeatherResponseToAppForecastData() {
        val weatherResponse = loadWeatherResponseFromJson("weatherapi-buffalo-2025-02-05.json")

        val result = weatherResponse.convertToForecastData()

        assertThat(result.latitude).isEqualTo(42.3478)
        assertThat(result.longitude).isEqualTo(-71.0466)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(26.6)
        assertThat(result.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
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
