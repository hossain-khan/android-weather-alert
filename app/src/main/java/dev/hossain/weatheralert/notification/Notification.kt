package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dev.hossain.weatheralert.R
import timber.log.Timber

internal fun triggerNotification(
    context: Context,
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

internal fun debugNotification(context: Context) {
    // Debug notification by triggering a notification
    triggerNotification(
        context = context,
        snowTomorrow = 10.0,
        rainTomorrow = 10.0,
        snowThreshold = 5f,
        rainThreshold = 5f,
    )
}
