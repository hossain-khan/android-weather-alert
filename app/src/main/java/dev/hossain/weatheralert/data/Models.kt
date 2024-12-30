package dev.hossain.weatheralert.data

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.ui.graphics.vector.ImageVector
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

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

@Parcelize
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
    val isAlertActive: Boolean,
    val uuid: String =
        java.util.UUID
            .randomUUID()
            .toString(),
) : Parcelable

enum class WeatherAlertCategory(
    val label: String,
    val unit: String,
) {
    SNOW_FALL("Snow", "cm"),
    RAIN_FALL("Rain", "mm"),
}

internal fun WeatherAlertCategory.icon(): ImageVector =
    when (this) {
        WeatherAlertCategory.SNOW_FALL -> Icons.Outlined.AcUnit
        WeatherAlertCategory.RAIN_FALL -> Icons.Outlined.Umbrella
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
