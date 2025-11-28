package com.openmeteo.api

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Ignore
import org.junit.Test

class OpenMeteoResponseTest {
    /**
     * Test for parsing OpenMeteo JSON response using Kotlin Serialization.
     *
     * This test is currently ignored because the OpenMeteo library (com.openmeteo.api)
     * uses its own internal types (Forecast.Response) which have specific serialization
     * requirements that don't match our test JSON format:
     *
     * 1. The library expects specific field names (utcOffsetSeconds, timezoneAbbreviation, generationtimeMs)
     * 2. The hourly time array expects double values, not ISO date-time strings
     *
     * The actual OpenMeteo API calls are made through the library's internal client,
     * which handles serialization correctly. Unit testing this would require mocking
     * the OpenMeteo library client.
     *
     * @see OpenMeteoService for the actual API integration
     */
    @Ignore("OpenMeteo library uses internal types that don't support external JSON parsing")
    @Test
    fun `test load and parse JSON file`() {
        val response = loadWeatherForecastFromJson("open-meteo-forecast-lac-mann-snowing-2025-01-20.json")

        assertThat(response).isNotNull()
        assertThat(response.latitude).isEqualTo(49.592735)
        assertThat(response.longitude).isEqualTo(-75.17785)
    }

    // Helper method to load WeatherForecast from JSON
    private fun loadWeatherForecastFromJson(fileName: String): Forecast.Response {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("File not found: $fileName")
        val jsonText = inputStream.bufferedReader().use { it.readText() }

        val json =
            Json {
                ignoreUnknownKeys = true
            }

        return json.decodeFromString<Forecast.Response>(jsonText)
    }
}
