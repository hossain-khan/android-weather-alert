package dev.hossain.weatheralert.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Enqueue weather check worker to run in background using WorkManager.
 * - https://developer.android.com/topic/libraries/architecture/workmanager
 */
fun scheduleWeatherAlerts(context: Context) {
    val weatherWorker =
        PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            6,
            TimeUnit.HOURS, // Check every 6 hours
        ).setConstraints(
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WeatherAlertWork",
        ExistingPeriodicWorkPolicy.UPDATE,
        weatherWorker,
    )
}
