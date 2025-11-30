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
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.deeplinking.BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.stripMarkdownSyntax
import timber.log.Timber

private const val DEFAULT_PRECIPITATION_VALUE = 30.0

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

    @DrawableRes val notificationLargeIcon: Int = when (alertCategory) {
        WeatherAlertCategory.SNOW_FALL -> {
            R.drawable.winter_snowflake
        }

        WeatherAlertCategory.RAIN_FALL -> {
            R.drawable.cloud_heavy_rain
        }
    }

    val notificationTitleText = buildString {
        when (alertCategory) {
            WeatherAlertCategory.SNOW_FALL -> {
                append("Snow Alert in $cityName")
            }

            WeatherAlertCategory.RAIN_FALL -> {
                append("Rain Alert in $cityName")
            }
        }
    }

    val notificationShortText = buildString {
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

    val notificationLongDescription = buildString {
        append("Your custom weather alert has been activated.\n")
        when (alertCategory) {
            WeatherAlertCategory.SNOW_FALL -> {
                append(
                    "$cityName is forecasted to receive ${
                        currentValue.formatUnit(
                            WeatherAlertCategory.SNOW_FALL.unit,
                        )
                    } of snowfall within the next 24 hours, ",
                )
                append(
                    "exceeding your configured threshold of ${
                        thresholdValue.formatUnit(
                            WeatherAlertCategory.SNOW_FALL.unit
                        )
                    }."
                )
            }

            WeatherAlertCategory.RAIN_FALL -> {
                append(
                    "$cityName is forecasted to receive ${
                        currentValue.formatUnit(
                            WeatherAlertCategory.RAIN_FALL.unit,
                        )
                    } of rainfall within the next 24 hours, ",
                )
                append(
                    "exceeding your configured threshold of ${
                        thresholdValue.formatUnit(
                            WeatherAlertCategory.RAIN_FALL.unit
                        )
                    }."
                )
            }
        }
        if (reminderNotes.isNotBlank()) {
            append("\n―――――――――――――――――\n")
            append("Reminder Notes:\n${stripMarkdownSyntax(reminderNotes)}")
        }
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // NOTE: This is not the right way to pass a deep link destination screen.
        // Ideally, we should use a proper deep link mechanism or navigation component.
        // See https://slackhq.github.io/circuit/deep-linking-android/
        putExtra(
            BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN, WeatherAlertDetailsScreen(userAlertId)
        )
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_weather_alert_notification)
        .setContentTitle(notificationTitleText).setContentText(notificationShortText)
        .setStyle(NotificationCompat.BigTextStyle().bigText(notificationLongDescription))
        .setLargeIcon(Icon.createWithResource(context, notificationLargeIcon))
        .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent)
        .setAutoCancel(true).build()

    notificationManager.notify(
        notificationTag,
        // ⚠️ Potential precision loss and overflow when converting to int.
        userAlertId.toInt(),
        notification,
    )
}

/**
 * Test notification by triggering a notification with user's alert configuration.
 */
internal fun testNotification(
    context: Context,
    userCityAlert: UserCityAlert,
) {
    val forecast = userCityAlert.latestCityForecast()
    val currentValue = when (userCityAlert.alert.alertCategory) {
        WeatherAlertCategory.RAIN_FALL -> forecast?.dailyCumulativeRain
            ?: DEFAULT_PRECIPITATION_VALUE

        WeatherAlertCategory.SNOW_FALL -> forecast?.dailyCumulativeSnow
            ?: DEFAULT_PRECIPITATION_VALUE
    }

    triggerNotification(
        context = context,
        userAlertId = userCityAlert.alert.id,
        notificationTag = userCityAlert.toNotificationTag(),
        alertCategory = userCityAlert.alert.alertCategory,
        currentValue = currentValue,
        thresholdValue = userCityAlert.alert.threshold,
        cityName = userCityAlert.city.city,
        reminderNotes = userCityAlert.alert.notes,
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
