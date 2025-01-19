package dev.hossain.weatheralert.data

import android.os.Parcelable
import androidx.annotation.DrawableRes
import dev.hossain.weatheralert.R
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

@DrawableRes
internal fun WeatherAlertCategory.iconRes(): Int =
    when (this) {
        WeatherAlertCategory.SNOW_FALL -> R.drawable.cold_snowflake_bold_icon
        WeatherAlertCategory.RAIN_FALL -> R.drawable.rain_umbrella_bold_icon
    }

data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val action: () -> Unit,
)
