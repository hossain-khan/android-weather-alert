package dev.hossain.weatheralert.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherAlertKeys
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.weatherAlertDataStore
import kotlinx.coroutines.flow.first

class WeatherCheckWorker(
    private val context: Context,
    params: WorkerParameters,
//    private val preferencesManager: PreferencesManager, // Injected
    private val weatherService: WeatherRepository, // Injected
) : CoroutineWorker(context, params) {
    private val preferencesManager = PreferencesManager(context)

    override suspend fun doWork(): Result {
        try {
            // Fetch thresholds from DataStore
            val snowThreshold = preferencesManager.snowThreshold.first()
            val rainThreshold = preferencesManager.rainThreshold.first()

            // Fetch forecast
            val forecast =
                weatherService.getDailyForecast(
                    // Use Toronto coordinates for now
                    latitude = 43.7,
                    longitude = -79.42,
                    apiKey = BuildConfig.WEATHER_API_KEY,
                )

            // Check if thresholds are exceeded
            val snowTomorrow = forecast.daily[1].snowVolume ?: 0.0 // Example: Snow forecast for tomorrow
            val rainTomorrow = forecast.daily[1].rainVolume ?: 0.0 // Example: Rain forecast for tomorrow

            if (snowTomorrow > snowThreshold || rainTomorrow > rainThreshold) {
                // Trigger a rich notification
                triggerNotification(snowTomorrow, rainTomorrow, snowThreshold, rainThreshold)

                val snowAlert = "Tomorrow: $snowTomorrow cm"
                val rainAlert = "Tomorrow: $rainTomorrow mm"
                saveWeatherAlertsToDataStore(context, snowAlert, rainAlert)
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun triggerNotification(
        snowTomorrow: Double,
        rainTomorrow: Double,
        snowThreshold: Float,
        rainThreshold: Float,
    ) {
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
