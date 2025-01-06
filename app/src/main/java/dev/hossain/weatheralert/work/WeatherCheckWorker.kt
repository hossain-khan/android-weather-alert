package dev.hossain.weatheralert.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherAlert
import dev.hossain.weatheralert.data.WeatherAlertCategory
import dev.hossain.weatheralert.data.WeatherRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Worker to check weather forecast and trigger notification if thresholds are exceeded.
 *
 * See additional details on Worker class:
 * - https://developer.android.com/topic/libraries/architecture/workmanager
 * - https://developer.android.com/reference/androidx/work/Worker
 * - https://developer.android.com/reference/kotlin/androidx/work/WorkManager
 */
class WeatherCheckWorker
    @AssistedInject
    constructor(
        @Assisted private val context: Context,
        @Assisted params: WorkerParameters,
        private val preferencesManager: PreferencesManager,
        private val weatherRepository: WeatherRepository,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            Timber.d("WeatherCheckWorker: Checking weather forecast")
            // Fetch thresholds from DataStore
            val userConfiguredAlerts = preferencesManager.userConfiguredAlerts.first().alerts

            if (userConfiguredAlerts.isEmpty()) {
                Timber.d("No user configured alerts found.")
                return Result.success()
            }

            userConfiguredAlerts.forEach { configuredAlert: WeatherAlert ->
                // Fetch forecast
                val forecastApiResult =
                    weatherRepository.getDailyForecast(
                        latitude = configuredAlert.lat,
                        longitude = configuredAlert.lon,
                    )

                when (forecastApiResult) {
                    is ApiResult.Success -> {
                        // Check if thresholds are exceeded
                        val snowTomorrow =
                            when {
                                forecastApiResult.value.snow.dailyCumulativeSnow > 0.0 -> {
                                    forecastApiResult.value.snow.dailyCumulativeSnow
                                }

                                else -> {
                                    forecastApiResult.value.snow.nextDaySnow
                                }
                            }
                        val rainTomorrow =
                            when {
                                forecastApiResult.value.rain.dailyCumulativeRain > 0.0 -> {
                                    forecastApiResult.value.rain.dailyCumulativeRain
                                }

                                else -> {
                                    forecastApiResult.value.rain.nextDayRain
                                }
                            }

                        when (configuredAlert.alertCategory) {
                            WeatherAlertCategory.SNOW_FALL -> {
                                if (snowTomorrow > configuredAlert.threshold) {
                                    triggerNotification(
                                        configuredAlert.alertCategory,
                                        snowTomorrow,
                                        configuredAlert.threshold,
                                    )
                                }
                            }
                            WeatherAlertCategory.RAIN_FALL -> {
                                if (rainTomorrow > configuredAlert.threshold) {
                                    // Trigger a rich notification
                                    triggerNotification(
                                        configuredAlert.alertCategory,
                                        rainTomorrow,
                                        configuredAlert.threshold,
                                    )
                                }
                            }
                        }
                        return Result.success()
                    }

                    is ApiResult.Failure.HttpFailure -> {
                        return Result.retry()
                    }

                    is ApiResult.Failure.ApiFailure -> {
                        return Result.failure()
                    }

                    is ApiResult.Failure.NetworkFailure -> {
                        return Result.retry()
                    }

                    is ApiResult.Failure.UnknownFailure -> {
                        return Result.failure()
                    }
                }
            }
            return Result.success()
        }

        private fun triggerNotification(
            alertCategory: WeatherAlertCategory,
            currentValue: Double,
            thresholdValue: Float,
        ) {
            Timber.d("Triggering notification for $alertCategory value: $currentValue, limit: $thresholdValue")

            val notificationText =
                buildString {
                    append("Configured weather alert threshold exceeded.\n")
                    when (alertCategory) {
                        WeatherAlertCategory.SNOW_FALL -> {
                            append("Snowfall: $currentValue cm\n")
                        }
                        WeatherAlertCategory.RAIN_FALL -> {
                            append("Rainfall: $currentValue mm\n")
                        }
                    }
                }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val notification =
                NotificationCompat
                    .Builder(context, "weather_alerts")
                    .setSmallIcon(R.drawable.weather_alert_icon) // Replace with your icon
                    .setContentTitle("Weather Alert")
                    .setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

            notificationManager.notify(1, notification)
        }
    }
