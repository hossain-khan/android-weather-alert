package dev.hossain.weatheralert.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(context: Context) {
    val name = "Weather Alerts"
    val descriptionText = "Notifications for weather thresholds"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel =
        NotificationChannel("weather_alerts", name, importance).apply {
            description = descriptionText
        }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
