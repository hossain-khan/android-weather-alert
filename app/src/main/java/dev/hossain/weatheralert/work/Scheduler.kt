package dev.hossain.weatheralert.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

internal const val WORKER_LOG_TAG = "Worker-TAG"

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
 * The unique work name that uniquely identifies PeriodicWorkRequest to refresh weather data.
 */
private const val WEATHER_CHECKER_WORKER_ID = "WeatherAlertWork"

/**
 * Worker ID for debug purpose.
 */
private const val WEATHER_CHECKER_WORKER_DEBUG_ID = "WeatherAlertWork_DEBUG"

/**
 * Enqueue weather check worker to run in background using WorkManager.
 * - https://developer.android.com/topic/libraries/architecture/workmanager
 */
fun scheduleWeatherAlertsWork(
    context: Context,
    updateIntervalHours: Long,
) {
    Timber.tag(WORKER_LOG_TAG).d("Scheduling weather check worker to run every $updateIntervalHours hours")
    val weatherWorker =
        PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            // Check every N hours
            // Must be min 15 minutes (See PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
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
        uniqueWorkName = WEATHER_CHECKER_WORKER_ID,
        existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
        request = weatherWorker,
    )
}

/**
 * Enqueue one-time weather check using worker for debugging.
 */
fun scheduleOneTimeWeatherAlertWorkerDebug(context: Context) {
    Timber.tag(WORKER_LOG_TAG).d("Scheduling one-time weather check worker for debugging")
    val oneTimeWorkRequest: OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<WeatherCheckWorker>()
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            ).build()

    val operation: Operation =
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName = WEATHER_CHECKER_WORKER_DEBUG_ID,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = oneTimeWorkRequest,
        )

    operation.state.observeForever { state ->
        Timber.tag(WORKER_LOG_TAG).d("One-time weather check worker state: $state")
    }
}
