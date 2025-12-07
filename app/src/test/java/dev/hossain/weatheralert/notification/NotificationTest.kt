package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.os.BundleCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.deeplinking.BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Test for notification deep linking functionality.
 *
 * Verifies that:
 * 1. Notification PendingIntent properly includes the deep link destination screen
 * 2. Intent has correct flags for Android 15+ BAL restriction (FLAG_ACTIVITY_CLEAR_TASK)
 * 3. The deep link extra is properly set for MainActivity to parse
 */
@RunWith(RobolectricTestRunner::class)
class NotificationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Clear any existing notifications
        notificationManager.cancelAll()
    }

    @Test
    fun `triggerNotification creates notification with deep link to WeatherAlertDetailsScreen`() {
        // Given
        val userAlertId = 123L
        val notificationTag = "test-notification"
        val alertCategory = WeatherAlertCategory.SNOW_FALL
        val currentValue = 15.5
        val thresholdValue = 10.0f
        val cityName = "Toronto"
        val reminderNotes = "Test reminder"

        // When
        triggerNotification(
            context = context,
            userAlertId = userAlertId,
            notificationTag = notificationTag,
            alertCategory = alertCategory,
            currentValue = currentValue,
            thresholdValue = thresholdValue,
            cityName = cityName,
            reminderNotes = reminderNotes,
        )

        // Then - Verify notification was posted
        val shadowNotificationManager = shadowOf(notificationManager)
        val notifications = shadowNotificationManager.allNotifications
        assertThat(notifications).hasSize(1)

        val notification = notifications[0]
        assertThat(notification).isNotNull()

        // Verify PendingIntent exists
        val pendingIntent = notification.contentIntent
        assertThat(pendingIntent).isNotNull()

        // Get the intent from PendingIntent using reflection or shadow
        val shadowPendingIntent = shadowOf(pendingIntent)
        val intent = shadowPendingIntent.savedIntent

        // Verify deep link destination screen is included
        val destinationScreen: Screen? =
            BundleCompat.getParcelable(
                intent.extras!!,
                BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN,
                Screen::class.java,
            )
        assertThat(destinationScreen).isNotNull()
        assertThat(destinationScreen).isInstanceOf(WeatherAlertDetailsScreen::class.java)
        assertThat((destinationScreen as WeatherAlertDetailsScreen).alertId).isEqualTo(userAlertId)
    }

    @Test
    fun `notification intent has FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK flags`() {
        // Given
        val userAlertId = 456L
        val notificationTag = "test-notification-flags"

        // When
        triggerNotification(
            context = context,
            userAlertId = userAlertId,
            notificationTag = notificationTag,
            alertCategory = WeatherAlertCategory.RAIN_FALL,
            currentValue = 20.0,
            thresholdValue = 15.0f,
            cityName = "Vancouver",
            reminderNotes = "",
        )

        // Then - Get the notification and extract intent
        val shadowNotificationManager = shadowOf(notificationManager)
        val notification = shadowNotificationManager.allNotifications[0]
        val pendingIntent = notification.contentIntent
        val shadowPendingIntent = shadowOf(pendingIntent)
        val intent = shadowPendingIntent.savedIntent

        // Verify intent flags for Android 15+ BAL restriction fix
        val hasNewTaskFlag = (intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0
        val hasClearTaskFlag = (intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0

        assertThat(hasNewTaskFlag).isTrue()
        assertThat(hasClearTaskFlag).isTrue()
    }

    @Test
    fun `notification PendingIntent has IMMUTABLE flag for security`() {
        // Given
        val userAlertId = 789L
        val notificationTag = "test-pending-intent-flags"

        // When
        triggerNotification(
            context = context,
            userAlertId = userAlertId,
            notificationTag = notificationTag,
            alertCategory = WeatherAlertCategory.SNOW_FALL,
            currentValue = 25.0,
            thresholdValue = 20.0f,
            cityName = "Montreal",
            reminderNotes = "Test",
        )

        // Then - Get the notification and check PendingIntent flags
        val shadowNotificationManager = shadowOf(notificationManager)
        val notification = shadowNotificationManager.allNotifications[0]
        val pendingIntent = notification.contentIntent
        val shadowPendingIntent = shadowOf(pendingIntent)

        // Verify PendingIntent has IMMUTABLE flag for security
        val hasImmutableFlag = (shadowPendingIntent.flags and PendingIntent.FLAG_IMMUTABLE) != 0
        assertThat(hasImmutableFlag).isTrue()
    }

    @Test
    fun `notification includes snooze actions with distinct request codes`() {
        // Given
        val userAlertId = 999L
        val notificationTag = "test-snooze-actions"

        // When
        triggerNotification(
            context = context,
            userAlertId = userAlertId,
            notificationTag = notificationTag,
            alertCategory = WeatherAlertCategory.RAIN_FALL,
            currentValue = 30.0,
            thresholdValue = 25.0f,
            cityName = "Calgary",
            reminderNotes = "",
        )

        // Then - Verify notification has snooze actions
        val shadowNotificationManager = shadowOf(notificationManager)
        val notification = shadowNotificationManager.allNotifications[0]

        // Verify notification has actions (snooze buttons)
        assertThat(notification.actions).hasLength(2)

        // Verify action titles
        assertThat(notification.actions[0].title.toString()).isEqualTo("Snooze 1 day")
        assertThat(notification.actions[1].title.toString()).isEqualTo("Snooze 1 week")

        // Verify actions have PendingIntents
        assertThat(notification.actions[0].actionIntent).isNotNull()
        assertThat(notification.actions[1].actionIntent).isNotNull()
    }
}
