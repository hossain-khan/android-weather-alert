package dev.hossain.weatheralert.core.work

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.hossain.weatheralert.MainActivity
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.network.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.BuildConfig
import kotlinx.coroutines.flow.first

class WeatherCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val dataStore: AlertConfigDataStore
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "WeatherCheckWorker"
        private const val CHANNEL_ID = "WeatherAlertChannel"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        val configs = dataStore.getAlertConfigs().first()
        if (configs.isEmpty()) return Result.success() // No alerts configured

        // Replace with the actual lat and lon
        // You might want to fetch the user's location here
        val lat = 37.7749 // Example: San Francisco latitude
        val lon = -122.4194 // Example: San Francisco longitude
        val apiKey = BuildConfig.WEATHER_API_KEY // Replace with your OpenWeatherMap API key

        try {
            val weatherData = weatherRepository.getWeatherData(lat, lon, apiKey)

            for (config in configs) {
                val threshold = config.threshold
                val forecast = when (config.category) {
                    is AlertCategory.Snow -> weatherData.daily.firstOrNull()?.snow
                    is AlertCategory.Rain -> weatherData.daily.firstOrNull()?.rain
                }

                if (forecast != null && forecast >= threshold) {
                    val alertMessage = when (config.category) {
                        is AlertCategory.Snow -> "Snow Alert: ${forecast}cm expected. Threshold is ${threshold}cm."
                        is AlertCategory.Rain -> "Rain Alert: ${forecast}mm expected. Threshold is ${threshold}mm."
                    }
                    showNotification(alertMessage, config.category)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            // Handle errors (e.g., network issues)
            return Result.retry()
        }
    }

    private fun showNotification(message: String, category: AlertCategory) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = when (category) {
            is AlertCategory.Snow -> "Snow Alert"
            is AlertCategory.Rain -> "Rain Alert"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.weather_alert_icon)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setTicker(applicationContext.getString(R.string.app_name))
            .setSmallIcon(R.drawable.weather_alert_icon)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Stop Monitoring", intent)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }
}