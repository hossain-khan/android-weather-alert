package dev.hossain.weatheralert.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

const val DEFAULT_SNOW_THRESHOLD = 5.0f // cm
const val DEFAULT_RAIN_THRESHOLD = 10.0f // mm

@JsonClass(generateAdapter = true)
data class WeatherForecast(
    val daily: List<DailyForecast>,
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    @Json(name = "dt") val date: Long, // Unix timestamp
    @Json(name = "temp") val temperature: Temperature,
    @Json(name = "weather") val weather: List<WeatherDescription>,
    @Json(name = "rain") val rainVolume: Double?, // mm (nullable)
    @Json(name = "snow") val snowVolume: Double?, // cm (nullable)
)

@JsonClass(generateAdapter = true)
data class Temperature(
    val min: Double,
    val max: Double,
)

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    val description: String,
    val icon: String,
)

data class AlertTileData constructor(
    /**
     * e.g., "Snowfall", "Rainfall"
     */
    val category: String,
    /**
     * e.g., "5 cm", "10 mm"
     */
    val threshold: String,
    /**
     * e.g., "Tomorrow: 7 cm", "Tomorrow: 15 mm"
     */
    val currentStatus: String,
)

enum class WeatherAlertCategory {
    SNOWFALL,
    RAINFALL,
}

@JsonClass(generateAdapter = true)
data class WeatherAlert(
    val alertCategory: WeatherAlertCategory,
    val threshold: Float,
    val lat: Double,
    val lon: Double,
    val cityName: String = "",
)

@JsonClass(generateAdapter = true)
data class ConfiguredAlerts(
    val alerts: List<WeatherAlert>,
)
