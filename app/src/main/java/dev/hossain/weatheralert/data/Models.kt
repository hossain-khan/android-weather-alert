package dev.hossain.weatheralert.data

import android.os.Parcelable
import androidx.annotation.DrawableRes
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import kotlinx.parcelize.Parcelize
import java.util.UUID

const val DEFAULT_SNOW_THRESHOLD = 20.0f // mm
const val DEFAULT_RAIN_THRESHOLD = 10.0f // mm

/**
 * Data class used by the UI to display alert card.
 */
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

/**
 * Returns the icon resource for the [WeatherAlertCategory].
 */
@DrawableRes
internal fun WeatherAlertCategory.iconRes(): Int =
    when (this) {
        WeatherAlertCategory.SNOW_FALL -> R.drawable.cold_snowflake_bold_icon
        WeatherAlertCategory.RAIN_FALL -> R.drawable.rain_umbrella_bold_icon
    }

/**
 * Data class used by the UI to display snackbar with optional [action].
 *
 * Usage in the app:
 * ```kotlin
 * // In your presenter
 * var snackbarData: SnackbarData? by remember { mutableStateOf(null) }
 *
 * // Pass the data using state
 * State(snackbarData = snackbarData)
 *
 * // Finally when handling state events
 * snackbarData = SnackbarData("Message", "Action Label") {}
 * ```
 *
 * In your main composable screen:
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 *
 * LaunchedEffect(state.snackbarData) {
 *     val data = state.snackbarData
 *     if (data != null) {
 *         val snackbarResult = snackbarHostState.showSnackbar(data.message, data.actionLabel)
 *         when (snackbarResult) {
 *             SnackbarResult.Dismissed -> {
 *                 Timber.d("Snackbar dismissed")
 *             }
 *
 *             SnackbarResult.ActionPerformed -> {
 *                 data.action()
 *             }
 *         }
 *     } else {
 *         Timber.d("Snackbar data is null - hide")
 *         snackbarHostState.currentSnackbarData?.dismiss()
 *     }
 * }
 * ```
 */
data class SnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val action: () -> Unit,
)
