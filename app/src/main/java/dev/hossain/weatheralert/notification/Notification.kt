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

// Unique offsets for XOR operation to create distinct request codes for each snooze action
private const val SNOOZE_1_DAY_ACTION_OFFSET = 0x1000
private const val SNOOZE_1_WEEK_ACTION_OFFSET = 0x2000

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
            // FLAG_ACTIVITY_CLEAR_TASK with FLAG_ACTIVITY_NEW_TASK ensures the activity can be launched
            // from background on Android 15+ (resolves BAL restriction). This clears the back stack and
            // creates a fresh navigation state, which is appropriate for notification deep linking.
            // The MainActivity rebuilds the proper back stack via parseDeepLinkedScreens().
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // NOTE: This is not the right way to pass a deep link destination screen.
            // Ideally, we should use a proper deep link mechanism or navigation component.
            // See https://slackhq.github.io/circuit/deep-linking-android/
            putExtra(BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN, WeatherAlertDetailsScreen(userAlertId))
        }
    val pendingIntent =
        PendingIntent.getActivity(
            context,
            userAlertId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    // ⚠️ Note: We use hashCode to generate notification IDs to avoid overflow issues with large alert IDs.
    // While hashCode can theoretically cause collisions, we also use notificationTag (which includes
    // unique cityId, alertId, and category) to ensure notifications are properly distinguished.
    // This approach provides a reasonable balance between uniqueness and avoiding integer overflow.
    val notificationId = userAlertId.hashCode()

    // Create snooze action pending intents with unique request codes based on alertId and action type
    val snooze1DayIntent =
        createSnoozePendingIntent(
            context = context,
            alertId = userAlertId,
            snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_DAY,
            notificationId = notificationId,
            notificationTag = notificationTag,
            requestCode = (userAlertId.hashCode() xor SNOOZE_1_DAY_ACTION_OFFSET),
        )
    val snooze1WeekIntent =
        createSnoozePendingIntent(
            context = context,
            alertId = userAlertId,
            snoozeDuration = SnoozeAlertReceiver.SNOOZE_1_WEEK,
            notificationId = notificationId,
            notificationTag = notificationTag,
            requestCode = (userAlertId.hashCode() xor SNOOZE_1_WEEK_ACTION_OFFSET),
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
            .addAction(R.drawable.snooze_24dp, "Snooze 1 day", snooze1DayIntent)
            .addAction(R.drawable.snooze_24dp, "Snooze 1 week", snooze1WeekIntent)
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

/**
 * Debug snooze by directly updating an alert's snooze time in the database.
 * This simulates the snooze action without needing to interact with a real notification.
 *
 * @param context Application context
 * @param alertId The ID of the alert to snooze (defaults to 1 for testing)
 * @param snoozeDuration Snooze duration option (defaults to 24 hours for testing)
 *
 * Usage in WeatherAlertApp.onCreate():
 * ```
 * debugSnooze(
 *     context = this,
 *     alertId = 1,
 *     snoozeDuration = SnoozeAlertReceiver.SNOOZE_TOMORROW
 * )
 * ```
 */
internal fun debugSnooze(
    context: Context,
    alertId: Long = 1,
    snoozeDuration: String = SnoozeAlertReceiver.SNOOZE_TOMORROW,
) {
    // Simulate the snooze action by broadcasting to SnoozeAlertReceiver
    val intent =
        Intent(context, SnoozeAlertReceiver::class.java).apply {
            action = SnoozeAlertReceiver.ACTION_SNOOZE_ALERT
            putExtra(SnoozeAlertReceiver.EXTRA_ALERT_ID, alertId)
            putExtra(SnoozeAlertReceiver.EXTRA_SNOOZE_DURATION, snoozeDuration)
            putExtra(SnoozeAlertReceiver.EXTRA_NOTIFICATION_ID, -1) // Not used for debug
            putExtra(SnoozeAlertReceiver.EXTRA_NOTIFICATION_TAG, "debug_snooze")
        }
    context.sendBroadcast(intent)
    Timber.d("Debug snooze triggered for alertId=$alertId with duration=$snoozeDuration")
}
