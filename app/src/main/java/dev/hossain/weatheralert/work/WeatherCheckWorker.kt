package dev.hossain.weatheralert.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.WeatherAlertCategory
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.notification.triggerNotification
import dev.hossain.weatheralert.util.HttpPingSender
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
        private val alertDao: AlertDao,
        private val weatherRepository: WeatherRepository,
    ) : CoroutineWorker(context, params) {
        private val httpPingSender = HttpPingSender(context)

        override suspend fun doWork(): Result {
            Timber.d("WeatherCheckWorker: Checking weather forecast")
            // Fetch thresholds from DataStore
            val userConfiguredAlerts = alertDao.getAlertsWithCity()

            if (userConfiguredAlerts.isEmpty()) {
                Timber.d("No user configured alerts found.")
                return Result.success()
            }

            val stringBuilder = StringBuilder()
            stringBuilder.append("Alerts:${userConfiguredAlerts.size}, ")

            userConfiguredAlerts.forEach { configuredAlert ->
                // Fetch forecast
                val forecastApiResult =
                    weatherRepository.getDailyForecast(
                        cityId = configuredAlert.city.id,
                        latitude = configuredAlert.city.lat,
                        longitude = configuredAlert.city.lng,
                        skipCache = true,
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

                        Timber.d("Snow: $snowTomorrow, Rain: $rainTomorrow")

                        stringBuilder.append("${configuredAlert.city.cityName} - ")
                        stringBuilder.append("Snow: $snowTomorrow, Rain: $rainTomorrow, ")
                        stringBuilder.append("Threshold: ${configuredAlert.alert.threshold}, ")
                        stringBuilder.append("Category: ${configuredAlert.alert.alertCategory}")

                        when (configuredAlert.alert.alertCategory) {
                            WeatherAlertCategory.SNOW_FALL -> {
                                if (snowTomorrow > configuredAlert.alert.threshold) {
                                    triggerNotification(
                                        context = context,
                                        notificationId = configuredAlert.alert.id,
                                        notificationTag = configuredAlert.toNotificationTag(),
                                        alertCategory = configuredAlert.alert.alertCategory,
                                        currentValue = snowTomorrow,
                                        thresholdValue = configuredAlert.alert.threshold,
                                    )
                                }
                            }
                            WeatherAlertCategory.RAIN_FALL -> {
                                if (rainTomorrow > configuredAlert.alert.threshold) {
                                    triggerNotification(
                                        context = context,
                                        notificationId = configuredAlert.alert.id,
                                        notificationTag = configuredAlert.toNotificationTag(),
                                        alertCategory = configuredAlert.alert.alertCategory,
                                        currentValue = rainTomorrow,
                                        thresholdValue = configuredAlert.alert.threshold,
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
            sendWorkerRunningPing(stringBuilder.toString())
            return Result.success()
        }

        private fun sendWorkerRunningPing(message: String) {
            httpPingSender.sendPingToDevice(
                pingUUID = "55ac31ff-5893-4e89-bcef-8a854bfb1e9c",
                extraMessage = "[$message]",
            )
        }
    }
