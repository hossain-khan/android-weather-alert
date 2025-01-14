package dev.hossain.weatheralert.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

internal const val WEATHER_UPDATE_INTERVAL_6_HOURS = 6L
internal const val WEATHER_UPDATE_INTERVAL_12_HOURS = 12L
internal const val WEATHER_UPDATE_INTERVAL_18_HOURS = 18L

/**
 * Default weather update interval in hours.
 */
internal const val DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS = WEATHER_UPDATE_INTERVAL_12_HOURS

/**
 * Supported weather update interval in hours.
 */
internal val supportedWeatherUpdateInterval: List<Long> =
    listOf(
        WEATHER_UPDATE_INTERVAL_6_HOURS,
        WEATHER_UPDATE_INTERVAL_12_HOURS,
        WEATHER_UPDATE_INTERVAL_18_HOURS,
    )

/**
 * Enqueue weather check worker to run in background using WorkManager.
 * - https://developer.android.com/topic/libraries/architecture/workmanager
 */
fun scheduleWeatherAlertsWork(
    context: Context,
    updateIntervalHours: Long,
) {
    Timber.d("Scheduling weather check worker to run every $updateIntervalHours hours")
    val weatherWorker =
        PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            // Check every N hours
            repeatInterval = updateIntervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).setConstraints(
            Constraints
                .Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WeatherAlertWork",
        ExistingPeriodicWorkPolicy.UPDATE,
        weatherWorker,
    )
}
