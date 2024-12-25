package dev.hossain.weatheralert.core.network

import dev.hossain.weatheralert.core.model.WeatherData
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val api: OpenWeatherMapApi) {
    suspend fun getWeatherData(lat: Double, lon: Double, appId: String): WeatherData {
        return api.getOneCallWeather(lat, lon, appId = appId)
    }
}