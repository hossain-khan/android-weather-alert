package io.tomorrow.api.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

class RealTimeWeatherResponseTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun loadJsonFromResources(fileName: String): String {
        return RealTimeWeatherResponseTest::class.java.classLoader!!
            .getResourceAsStream(fileName)!!
            .bufferedReader()
            .use { it.readText() }
    }

    @Test
    fun `parses RealTimeWeatherResponse from JSON correctly`() {
        val jsonString = loadJsonFromResources("sample_realtime_weather.json")
        val adapter = moshi.adapter(RealTimeWeatherResponse::class.java)
        val response = adapter.fromJson(jsonString)

        assertThat(response).isNotNull()
        response!! // Ensure response is not null for subsequent assertions

        // Assertions for location
        assertThat(response.location.latitude).isEqualTo(43.653480529785156)
        assertThat(response.location.longitude).isEqualTo(-79.3839340209961)
        // Note: 'name' and 'type' are not part of the Location data class in TomorrowIo.kt,
        // so they won't be parsed into the object and cannot be asserted here.

        // Assertions for data
        assertThat(response.data.time).isEqualTo("2025-01-12T01:40:00Z")

        // Assertions for weather values
        val values = response.data.values
        assertThat(values.cloudBase).isEqualTo(1.5)
        assertThat(values.cloudCeiling).isEqualTo(2.5)
        assertThat(values.cloudCover).isEqualTo(75.0)
        assertThat(values.dewPoint).isEqualTo(-4.88)
        assertThat(values.freezingRainIntensity).isEqualTo(0.0)
        assertThat(values.hailProbability).isEqualTo(97.7)
        assertThat(values.hailSize).isEqualTo(4.27)
        assertThat(values.humidity).isEqualTo(88.0)
        assertThat(values.precipitationProbability).isEqualTo(0)
        assertThat(values.pressureSurfaceLevel).isEqualTo(999.33)
        assertThat(values.rainIntensity).isEqualTo(0.0)
        assertThat(values.sleetIntensity).isEqualTo(0.0)
        assertThat(values.snowIntensity).isEqualTo(0.0)
        assertThat(values.temperature).isEqualTo(-3.19)
        assertThat(values.temperatureApparent).isEqualTo(-7.02)
        assertThat(values.uvHealthConcern).isEqualTo(0)
        assertThat(values.uvIndex).isEqualTo(0)
        assertThat(values.visibility).isEqualTo(16.0)
        assertThat(values.weatherCode).isEqualTo(1000)
        assertThat(values.windDirection).isEqualTo(270.38)
        assertThat(values.windGust).isEqualTo(6.5)
        assertThat(values.windSpeed).isEqualTo(2.69)
    }
}
