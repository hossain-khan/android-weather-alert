package dev.hossain.weatheralert.data

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.ui.graphics.vector.ImageVector
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import kotlinx.parcelize.Parcelize
import java.util.UUID

const val DEFAULT_SNOW_THRESHOLD = 20.0f // mm
const val DEFAULT_RAIN_THRESHOLD = 10.0f // mm

@Parcelize
data class AlertTileData constructor(
    val alertId: Long,
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

// ⚠️ Icon should be imported instead
// See https://github.com/hossain-khan/android-weather-alert/issues/31
internal fun WeatherAlertCategory.icon(): ImageVector =
    when (this) {
        WeatherAlertCategory.SNOW_FALL -> Icons.Outlined.AcUnit
        WeatherAlertCategory.RAIN_FALL -> Icons.Outlined.Umbrella
    }

data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val action: () -> Unit,
)
