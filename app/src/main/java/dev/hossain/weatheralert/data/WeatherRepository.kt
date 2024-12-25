package dev.hossain.weatheralert.data

import dev.hossain.weatheralert.api.WeatherApi

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getDailyForecast(latitude: Double, longitude: Double, apiKey: String): WeatherForecast {
        return api.getDailyForecast(latitude, longitude, appid = apiKey)
    }
}
