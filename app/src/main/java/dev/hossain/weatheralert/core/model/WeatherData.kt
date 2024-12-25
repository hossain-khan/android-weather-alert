package dev.hossain.weatheralert.core.model

// Data classes to model the OpenWeatherMap API response
// (Adapt this based on the One Call API response structure)
data class WeatherData(
    val daily: List<DailyForecast>
)

data class DailyForecast(
    val dt: Long,
    val rain: Double?,
    val snow: Double?,
)