package dev.hossain.weatheralert.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherForecast(
    val daily: List<DailyForecast>
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    @Json(name = "dt") val date: Long, // Unix timestamp
    @Json(name = "temp") val temperature: Temperature,
    @Json(name = "weather") val weather: List<WeatherDescription>,
    @Json(name = "rain") val rainVolume: Double?, // mm (nullable)
    @Json(name = "snow") val snowVolume: Double? // cm (nullable)
)

@JsonClass(generateAdapter = true)
data class Temperature(
    val min: Double,
    val max: Double
)

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    val description: String,
    val icon: String
)