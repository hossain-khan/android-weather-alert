package dev.hossain.weatheralert.data.model

data class WeatherForecast(
    val temperature: Float,
    val snowfall: Float?,
    val rainfall: Float?,
    val timestamp: Long
)