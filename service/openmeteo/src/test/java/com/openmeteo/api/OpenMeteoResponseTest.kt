package com.openmeteo.api

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals // Ensure this import is present
import org.junit.Test

class OpenMeteoResponseTest {
    @Test
    fun `test load and parse JSON file`() {
        val response = loadWeatherForecastFromJson("open-meteo-forecast-lac-mann-snowing-2025-01-20.json")

        // Verify the parsed data
        assertThat(response).isNotNull()
        assertEquals(49.592735, response.latitude!!, 0.000001)
        assertEquals(-75.17785, response.longitude!!, 0.000001)

        // It might be good to add assertions for the renamed fields
        // and the hourly times if we knew what to expect, but the primary goal
        // is to make parsing successful.
        // For example, we could check if the hourly time list is not empty if populated.
        assertThat(response.hourly?.time).isNotNull()
        assertThat(response.hourly?.time).isNotEmpty()
        // And check one of the converted timestamps
        assertEquals(1737456000000L, response.hourly?.time?.get(0))

        // Check renamed fields (assuming they are not null)
        assertThat(response.generationtimeMs).isNotNull()
        assertThat(response.utcOffsetSeconds).isNotNull()
        assertThat(response.timezoneAbbreviation).isNotNull()
        assertThat(response.timezoneAbbreviation).isEqualTo("GMT")

    }

    // Helper method to load WeatherForecast from JSON
    private fun loadWeatherForecastFromJson(fileName: String): Forecast.Response {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        val jsonText = inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")

        val json =
            Json {
                ignoreUnknownKeys = true // This is good, helps with forward compatibility
            }

        return json.decodeFromString<Forecast.Response>(jsonText)
    }
}
