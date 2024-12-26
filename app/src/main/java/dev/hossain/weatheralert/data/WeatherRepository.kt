package dev.hossain.weatheralert.data

import dev.hossain.weatheralert.api.WeatherApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getDailyForecast(latitude: Double, longitude: Double, apiKey: String): WeatherForecast {
        return api.getDailyForecast(latitude = latitude, longitude = longitude, apiKey = apiKey)
    }

    fun getWeatherForecastFlow(latitude: Double, longitude: Double, apiKey: String) = flow {
        while (true) {
            val forecast = getDailyForecast(latitude, longitude, apiKey)
            emit(forecast)
            delay(60 * 60 * 1000) // 1 hour
        }
    }
}
