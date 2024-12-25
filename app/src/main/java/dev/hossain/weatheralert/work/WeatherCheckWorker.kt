package dev.hossain.weatheralert.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WeatherCheckWorker(
    private val context: Context,
    params: WorkerParameters,
    private val preferencesManager: PreferencesManager, // Injected
    private val weatherService: WeatherService // Injected
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            // Fetch thresholds from DataStore
            val snowThreshold = preferencesManager.snowThreshold.first()
            val rainThreshold = preferencesManager.rainThreshold.first()

            // Fetch forecast
            val forecast = weatherService.getWeatherForecast(/* Pass required params */)

            // Check if thresholds are exceeded
            val snowTomorrow = forecast.daily[1].snow ?: 0.0 // Example: Snow forecast for tomorrow
            val rainTomorrow = forecast.daily[1].rain ?: 0.0 // Example: Rain forecast for tomorrow

            if (snowTomorrow > snowThreshold || rainTomorrow > rainThreshold) {
                // Trigger a rich notification
                triggerNotification(snowTomorrow, rainTomorrow, snowThreshold, rainThreshold)
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
        rainThreshold: Float
    ) {
        val notificationText = buildString {
            if (snowTomorrow > snowThreshold) append("Snowfall: $snowTomorrow cm\n")
            if (rainTomorrow > rainThreshold) append("Rainfall: $rainTomorrow mm")
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "weather_alerts")
            .setSmallIcon(R.drawable.weather_alert_icon) // Replace with your icon
            .setContentTitle("Weather Alert")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }
}
