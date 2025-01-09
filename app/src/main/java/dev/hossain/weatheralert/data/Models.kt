package dev.hossain.weatheralert.data

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import java.util.UUID

const val DEFAULT_SNOW_THRESHOLD = 20.0f // mm
const val DEFAULT_RAIN_THRESHOLD = 10.0f // mm

@Parcelize
data class AlertTileData constructor(
    val alertId: Int,
    val cityInfo: String,
    val lat: Double,
    val lon: Double,
    val category: WeatherAlertCategory,
    val threshold: String,
    val currentStatus: String,
    val isAlertActive: Boolean,
    val alertNote: String,
    /**
     * Unique identifier for the alert item in the lazy column.
     * Added to avoid app crashing due to duplicate keys.
     */
    val uuid: String = UUID.randomUUID().toString(),
) : Parcelable

enum class WeatherAlertCategory(
    val label: String,
    /**
     * 🛑 THIS IS A BIG MESS. Fix it in the app. ⚠️
     * - https://github.com/hossain-khan/android-weather-alert/issues/60
     */
    val unit: String,
) {
    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     */
    SNOW_FALL("Snow", "mm"),

    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     */
    RAIN_FALL("Rain", "mm"),
}

// ⚠️ Icon should be imported instead
// See https://github.com/hossain-khan/android-weather-alert/issues/31
internal fun WeatherAlertCategory.icon(): ImageVector =
    when (this) {
        WeatherAlertCategory.SNOW_FALL -> Icons.Outlined.AcUnit
        WeatherAlertCategory.RAIN_FALL -> Icons.Outlined.Umbrella
    }

data class ForecastData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val snow: Snow = Snow(),
    val rain: Rain = Rain(),
)

data class Snow(
    val dailyCumulativeSnow: Double = 0.0,
    val nextDaySnow: Double = 0.0,
    val weeklyCumulativeSnow: Double = 0.0,
)

data class Rain(
    val dailyCumulativeRain: Double = 0.0,
    val nextDayRain: Double = 0.0,
    val weeklyCumulativeRain: Double = 0.0,
)
