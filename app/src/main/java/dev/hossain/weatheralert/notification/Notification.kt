package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.WeatherAlertCategory
import timber.log.Timber

/**
 * Triggers a notification with the given content.
 */
internal fun triggerNotification(
    context: Context,
    notificationId: Int,
    notificationTag: String,
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
            .setSmallIcon(R.drawable.weather_alert_icon)
            .setContentTitle("Weather Alert")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

    notificationManager.notify(notificationTag, notificationId, notification)
}

/**
 * Debug notification by triggering a notification with hard-coded content.
 */
internal fun debugNotification(context: Context) {
    // Debug notification by triggering a notification
    triggerNotification(
        context = context,
        notificationId = 1,
        notificationTag = "debug",
        alertCategory = WeatherAlertCategory.SNOW_FALL,
        currentValue = 10.0,
        thresholdValue = 5.0f,
    )
}
