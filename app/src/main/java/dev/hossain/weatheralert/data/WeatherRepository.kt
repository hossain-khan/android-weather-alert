package dev.hossain.weatheralert.data
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getDailyForecast(latitude: Double, longitude: Double, apiKey: String): WeatherForecast {
        return api.getDailyForecast(latitude = latitude, longitude = longitude, apiKey = apiKey)
    }

    fun getWeatherForecastFlow(latitude: Double, longitude: Double, apiKey: String): Flow<WeatherForecast> = flow {
        emit(getDailyForecast(latitude, longitude, apiKey))
    }
}