package dev.hossain.weatheralert.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherAlertKeys
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.weatherAlertDataStore
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
            val snowThreshold = preferencesManager.snowThreshold.first()
            val rainThreshold = preferencesManager.rainThreshold.first()

            // Fetch forecast
            val forecastApiResult =
                weatherRepository.getDailyForecast(
                    // Use Oshawa coordinates for now 43°55'24.0"N+78°53'49.9"W/@43.9233409,-78.899766
                    latitude = 43.9233409,
                    longitude = -78.899766,
                    apiKey = BuildConfig.WEATHER_API_KEY,
                )

            when (forecastApiResult) {
                is ApiResult.Success -> {
                    // Check if thresholds are exceeded
                    val snowTomorrow = forecastApiResult.value.daily[1].snowVolume ?: 0.0 // Example: Snow forecast for tomorrow
                    val rainTomorrow = forecastApiResult.value.daily[1].rainVolume ?: 0.0 // Example: Rain forecast for tomorrow

                    if (snowTomorrow > snowThreshold || rainTomorrow > rainThreshold) {
                        // Trigger a rich notification
                        triggerNotification(snowTomorrow, rainTomorrow, snowThreshold, rainThreshold)

                        val snowAlert = "Tomorrow: $snowTomorrow cm"
                        val rainAlert = "Tomorrow: $rainTomorrow mm"
                        saveWeatherAlertsToDataStore(context, snowAlert, rainAlert)
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

        private fun triggerNotification(
            snowTomorrow: Double,
            rainTomorrow: Double,
            snowThreshold: Float,
            rainThreshold: Float,
        ) {
            Timber.d("Triggering notification for snow: $snowTomorrow, rain: $rainTomorrow")

            val notificationText =
                buildString {
                    if (snowTomorrow > snowThreshold) append("Snowfall: $snowTomorrow cm\n")
                    if (rainTomorrow > rainThreshold) append("Rainfall: $rainTomorrow mm")
                }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification =
                NotificationCompat
                    .Builder(context, "weather_alerts")
                    .setSmallIcon(R.drawable.weather_alert_icon) // Replace with your icon
                    .setContentTitle("Weather Alert")
                    .setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

            notificationManager.notify(1, notification)
        }

        private suspend fun saveWeatherAlertsToDataStore(
            context: Context,
            snowAlert: String,
            rainAlert: String,
        ) {
            context.weatherAlertDataStore.edit { preferences ->
                preferences[WeatherAlertKeys.SNOW_ALERT] = snowAlert
                preferences[WeatherAlertKeys.RAIN_ALERT] = rainAlert
            }
        }
    }
