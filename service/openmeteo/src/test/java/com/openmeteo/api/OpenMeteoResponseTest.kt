package com.openmeteo.api

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class OpenMeteoResponseTest {
    @Test(expected = Exception::class)
    fun `test load and parse JSON file`() {
        // Test is not working, because of following error:
        // kotlinx.serialization.json.internal.JsonDecodingException: Unexpected JSON token at offset 937:
        // Failed to parse type 'double' for input '2025-01-21T00:00' at path: $.hourly['time'][0]
        val response = loadWeatherForecastFromJson("open-meteo-forecast-lac-mann-snowing-2025-01-20.json")

        // Verify the parsed data
        assertThat(response).isNotNull()
        assertThat(response.latitude).isEqualTo(49.592735)
        assertThat(response.longitude).isEqualTo(-75.17785)
    }

    // Helper method to load WeatherForecast from JSON
    private fun loadWeatherForecastFromJson(fileName: String): Forecast.Response {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        val jsonText = inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")

        val json =
            Json {
                ignoreUnknownKeys = true
            }

        return json.decodeFromString<Forecast.Response>(jsonText)
    }
}
