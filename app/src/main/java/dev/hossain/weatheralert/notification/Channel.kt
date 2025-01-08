package dev.hossain.weatheralert.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

internal const val NOTIFICATION_CHANNEL_ID = "weather_alerts_channel"

/**
 * Creates a notification channel for weather alerts app.
 *
 * @param context The context to use for creating the notification channel.
 */
fun createAppNotificationChannel(context: Context) {
    val name = "Weather Alerts"
    val descriptionText = "Notifications for weather thresholds"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel =
        NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
