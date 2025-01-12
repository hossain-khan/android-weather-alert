package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.WeatherAlertCategory
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.stripMarkdownSyntax
import timber.log.Timber

/**
 * Triggers a notification with the given content.
 *
 * See https://developer.android.com/develop/ui/views/notifications/build-notification
 */
internal fun triggerNotification(
    context: Context,
    notificationId: Int,
    notificationTag: String,
    alertCategory: WeatherAlertCategory,
    currentValue: Double,
    thresholdValue: Float,
    cityName: String,
    reminderNotes: String,
) {
    Timber.d("Triggering notification for $alertCategory value: $currentValue, limit: $thresholdValue")

    @DrawableRes val notificationLargeIcon: Int =
        when (alertCategory) {
            WeatherAlertCategory.SNOW_FALL -> {
                R.drawable.winter_snowflake
            }
            WeatherAlertCategory.RAIN_FALL -> {
                R.drawable.cloud_heavy_rain
            }
        }

    val notificationTitleText =
        buildString {
            when (alertCategory) {
                WeatherAlertCategory.SNOW_FALL -> {
                    append("Snow Alert")
                }
                WeatherAlertCategory.RAIN_FALL -> {
                    append("Rain Alert")
                }
            }
            append(" - $cityName")
        }

    val notificationShortText =
        buildString {
            append("About ")
            when (alertCategory) {
                WeatherAlertCategory.SNOW_FALL -> {
                    append("${currentValue.formatUnit(WeatherAlertCategory.SNOW_FALL.unit)} snowfall expected.")
                }
                WeatherAlertCategory.RAIN_FALL -> {
                    append("${currentValue.formatUnit(WeatherAlertCategory.RAIN_FALL.unit)} rainfall expected.")
                }
            }
        }

    val notificationLongDescription =
        buildString {
            append("Your custom weather alert has been activated.\n")
            when (alertCategory) {
                WeatherAlertCategory.SNOW_FALL -> {
                    append(
                        "$cityName is forecasted to receive ${currentValue.formatUnit(
                            WeatherAlertCategory.SNOW_FALL.unit,
                        )} of snowfall within the next 24 hours, ",
                    )
                    append("surpassing your configured threshold of ${thresholdValue.formatUnit(WeatherAlertCategory.SNOW_FALL.unit)}.")
                }
                WeatherAlertCategory.RAIN_FALL -> {
                    append(
                        "$cityName is forecasted to receive ${currentValue.formatUnit(
                            WeatherAlertCategory.RAIN_FALL.unit,
                        )} of rainfall within the next 24 hours, ",
                    )
                    append("surpassing your configured threshold of ${thresholdValue.formatUnit(WeatherAlertCategory.RAIN_FALL.unit)}.")
                }
            }
            if (reminderNotes.isNotBlank()) {
                append("\n―――――――――――――――――\n")
                append("Reminder Notes:\n${stripMarkdownSyntax(reminderNotes)}")
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
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.weather_alert_icon)
            .setContentTitle(notificationTitleText)
            .setContentText(notificationShortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationLongDescription))
            .setLargeIcon(Icon.createWithResource(context, notificationLargeIcon))
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
        alertCategory = WeatherAlertCategory.RAIN_FALL,
        currentValue = 10.0,
        thresholdValue = 5.0f,
        cityName = "Toronto",
        reminderNotes = "* Charge batteries\n* Check tire pressure\n* Order Groceries",
    )
}
