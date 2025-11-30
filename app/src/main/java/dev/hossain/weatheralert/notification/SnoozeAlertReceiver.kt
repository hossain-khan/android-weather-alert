package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.hossain.weatheralert.WeatherAlertApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

/**
 * BroadcastReceiver to handle snooze action from notification.
 * Snooze options presented in notification actions:
 * - 1 hour
 * - 3 hours
 *
 * Additional snooze durations supported by backend for future use:
 * - Until tomorrow (next day at 8 AM)
 * - 1 week
 */
class SnoozeAlertReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val alertId = intent.getLongExtra(EXTRA_ALERT_ID, -1)
        val snoozeDuration = intent.getStringExtra(EXTRA_SNOOZE_DURATION) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val notificationTag = intent.getStringExtra(EXTRA_NOTIFICATION_TAG)

        Timber.d("SnoozeAlertReceiver: alertId=$alertId, duration=$snoozeDuration, notificationId=$notificationId")

        if (alertId == -1L) {
            Timber.e("Invalid alertId received in SnoozeAlertReceiver")
            return
        }

        val snoozedUntil = calculateSnoozeUntil(snoozeDuration)
        Timber.d("Snoozing alert $alertId until $snoozedUntil (${java.util.Date(snoozedUntil)})")

        // Get the AlertDao from the AppGraph
        val app = context.applicationContext as WeatherAlertApp
        val alertDao = app.appGraph.alertDao

        // Use goAsync() to ensure the system keeps the process alive during async work
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the alert's snooze time in the database
                alertDao.updateSnoozeUntil(alertId, snoozedUntil)
                Timber.d("Alert $alertId snoozed until ${java.util.Date(snoozedUntil)}")

                // Dismiss the notification after snooze update succeeds
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationTag != null && notificationId != -1) {
                    notificationManager.cancel(notificationTag, notificationId)
                } else if (notificationId != -1) {
                    notificationManager.cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun calculateSnoozeUntil(snoozeDuration: String): Long {
        val now = System.currentTimeMillis()
        return when (snoozeDuration) {
            SNOOZE_1_HOUR -> now + HOUR_IN_MILLIS
            SNOOZE_3_HOURS -> now + 3 * HOUR_IN_MILLIS
            SNOOZE_TOMORROW -> {
                // Calculate tomorrow at 8 AM
                val calendar =
                    Calendar.getInstance().apply {
                        timeInMillis = now
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 8)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                calendar.timeInMillis
            }
            SNOOZE_1_WEEK -> now + 7 * DAY_IN_MILLIS
            else -> now + HOUR_IN_MILLIS // Default to 1 hour
        }
    }

    companion object {
        const val ACTION_SNOOZE_ALERT = "dev.hossain.weatheralert.ACTION_SNOOZE_ALERT"
        const val EXTRA_ALERT_ID = "extra_alert_id"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_NOTIFICATION_TAG = "extra_notification_tag"

        // Snooze duration options - currently only 1h and 3h are shown in notification actions
        // due to space constraints, but the backend supports all duration options.
        const val SNOOZE_1_HOUR = "snooze_1_hour"
        const val SNOOZE_3_HOURS = "snooze_3_hours"
        const val SNOOZE_TOMORROW = "snooze_tomorrow"
        const val SNOOZE_1_WEEK = "snooze_1_week"

        private const val HOUR_IN_MILLIS = 60 * 60 * 1000L
        private const val DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS
    }
}
