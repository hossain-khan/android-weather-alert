package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.deeplinking.BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
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
    userAlertId: Long,
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
                    append("Snow Alert in $cityName")
                }
                WeatherAlertCategory.RAIN_FALL -> {
                    append("Rain Alert in $cityName")
                }
            }
        }

    val notificationShortText =
        buildString {
            append("Approximately ")
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
                    append("exceeding your configured threshold of ${thresholdValue.formatUnit(WeatherAlertCategory.SNOW_FALL.unit)}.")
                }
                WeatherAlertCategory.RAIN_FALL -> {
                    append(
                        "$cityName is forecasted to receive ${currentValue.formatUnit(
                            WeatherAlertCategory.RAIN_FALL.unit,
                        )} of rainfall within the next 24 hours, ",
                    )
                    append("exceeding your configured threshold of ${thresholdValue.formatUnit(WeatherAlertCategory.RAIN_FALL.unit)}.")
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // NOTE: This is not the right way to pass a deep link destination screen.
            // Ideally, we should use a proper deep link mechanism or navigation component.
            // See https://slackhq.github.io/circuit/deep-linking-android/
            putExtra(BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN, WeatherAlertDetailsScreen(userAlertId))
        }
    val pendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    // ⚠️ Potential precision loss and overflow when converting to int.
    val notificationId = userAlertId.toInt()

    // Create snooze action pending intents
    val snooze1HourIntent =
        createSnoozePendingIntent(
            context = context,
            alertId = userAlertId,
            snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_HOUR,
            notificationId = notificationId,
            notificationTag = notificationTag,
            requestCode = (userAlertId * 10 + 1).toInt(),
        )
    val snooze3HoursIntent =
        createSnoozePendingIntent(
            context = context,
            alertId = userAlertId,
            snoozeDuration = SnoozeAlertReceiver.SNOOZE_3_HOURS,
            notificationId = notificationId,
            notificationTag = notificationTag,
            requestCode = (userAlertId * 10 + 2).toInt(),
        )

    val notification =
        NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_alert_notification)
            .setContentTitle(notificationTitleText)
            .setContentText(notificationShortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationLongDescription))
            .setLargeIcon(Icon.createWithResource(context, notificationLargeIcon))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Add snooze actions - limited to 2 actions to fit notification constraints
            .addAction(R.drawable.snooze_24dp, "Snooze 1h", snooze1HourIntent)
            .addAction(R.drawable.snooze_24dp, "Snooze 3h", snooze3HoursIntent)
            .build()

    notificationManager.notify(
        notificationTag,
        notificationId,
        notification,
    )
}

/**
 * Creates a PendingIntent for snoozing an alert notification.
 */
private fun createSnoozePendingIntent(
    context: Context,
    alertId: Long,
    snoozeDuration: String,
    notificationId: Int,
    notificationTag: String,
    requestCode: Int,
): PendingIntent {
    val snoozeIntent =
        Intent(context, SnoozeAlertReceiver::class.java).apply {
            action = SnoozeAlertReceiver.ACTION_SNOOZE_ALERT
            putExtra(SnoozeAlertReceiver.EXTRA_ALERT_ID, alertId)
            putExtra(SnoozeAlertReceiver.EXTRA_SNOOZE_DURATION, snoozeDuration)
            putExtra(SnoozeAlertReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(SnoozeAlertReceiver.EXTRA_NOTIFICATION_TAG, notificationTag)
        }
    return PendingIntent.getBroadcast(
        context,
        requestCode,
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

/**
 * Debug notification by triggering a notification with hard-coded content.
 */
internal fun debugNotification(context: Context) {
    // Debug notification by triggering a notification
    triggerNotification(
        context = context,
        userAlertId = 1,
        notificationTag = "debug",
        alertCategory = WeatherAlertCategory.SNOW_FALL,
        currentValue = 30.0,
        thresholdValue = 15.0f,
        cityName = "Toronto",
        reminderNotes = "* Charge batteries\n* Check tire pressure\n* Order Groceries",
    )
}
