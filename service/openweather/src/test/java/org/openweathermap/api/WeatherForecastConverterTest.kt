package org.openweathermap.api

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse
import org.junit.Test
import org.openweathermap.api.model.WeatherForecast

/**
 * Tests [WeatherForecast] to [AppForecastData] conversion.
 */
class WeatherForecastConverterTest {
    @Test
    fun convertsWeatherForecastToAppForecastData() {
        val weatherForecast = loadWeatherForecastFromJson("open-weather-hourly-snow-oshawa.json")

        val result = weatherForecast.convertToForecastData()

        assertThat(result.latitude).isEqualTo(43.9319)
        assertThat(result.longitude).isEqualTo(-78.851)
        assertThat(result.snow.dailyCumulativeSnow).isEqualTo(4.03)
        assertThat(result.snow.nextDaySnow).isEqualTo(2.06)
        assertThat(result.snow.weeklyCumulativeSnow).isEqualTo(0.0)
        assertThat(result.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(result.rain.nextDayRain).isEqualTo(0.0)
        assertThat(result.rain.weeklyCumulativeRain).isEqualTo(0.0)
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
