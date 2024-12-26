package dev.hossain.weatheralert.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.repository.AlertConfigRepository
import dev.hossain.weatheralert.data.repository.WeatherRepository
import dev.hossain.weatheralert.domain.model.AlertType

class WeatherCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val checkWeatherAlertsUseCase: CheckWeatherAlertsUseCase, // ❌ This was never generated!
    private val alertConfigRepository: AlertConfigRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Get all configured alerts
            val configs = alertConfigRepository.getAlertConfigs()

            // 2. Get weather forecast for tomorrow
            val forecast = weatherRepository.getTomorrowsForecast()

            // 3. Check each alert configuration against forecast
            configs.forEach { config ->
                when (config.type) {
                    AlertType.SNOW_FALL -> {
                        if ((forecast.snowfall ?: 0f) >= config.threshold) {
                            createNotification(
                                WeatherAlert( // ❌ This was never generated!
                                    id = config.id,
                                    title = "Snow Alert ❄️",
                                    description = "Expected snowfall tomorrow: ${forecast.snowfall}cm. " +
                                            "Prepare your snow blower!",
                                    severity = AlertSeverity.HIGH
                                )
                            )
                        }
                    }
                    AlertType.RAIN_FALL -> {
                        if ((forecast.rainfall ?: 0f) >= config.threshold) {
                            createNotification(
                                WeatherAlert( // ❌ This was never generated!
                                    id = config.id,
                                    title = "Rain Alert 🌧️",
                                    description = "Expected rainfall tomorrow: ${forecast.rainfall}mm",
                                    severity = AlertSeverity.MEDIUM
                                )
                            )
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking weather alerts", e)
            Result.retry()
        }
    }

    private fun createNotification(alert: WeatherAlert) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important weather alerts"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build rich notification with big text style
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_alert)
            .setContentTitle(alert.title)
            .setContentText(alert.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.description))
            .setColor(ContextCompat.getColor(applicationContext, R.color.notification_color))
            // Add action to open app
            .setContentIntent(createPendingIntent())
            .build()

        notificationManager.notify(alert.id.hashCode(), notification)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(
            applicationContext.packageName
        )?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "WeatherCheckWorker"
        private const val CHANNEL_ID = "weather_alerts"

        // Helper to schedule periodic work
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "weather_check",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}

// Factory for WorkManager to create our worker with dependencies
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {
    @Binds
    @IntoMap
    @WorkerKey(WeatherCheckWorker::class)
    abstract fun bindWeatherCheckWorker(factory: WeatherCheckWorker.Factory): ChildWorkerFactory
}

// Custom factory for worker
class WeatherCheckWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards ChildWorkerFactory>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry = workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
        return foundEntry?.value?.create(appContext, workerParameters)
    }
}