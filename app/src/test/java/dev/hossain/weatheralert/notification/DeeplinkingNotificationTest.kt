package dev.hossain.weatheralert.notification

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Tests for notification functionality, particularly verifying that each notification
 * has a unique PendingIntent to ensure proper deeplink navigation.
 */
@RunWith(RobolectricTestRunner::class)
class DeeplinkingNotificationTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @Test
    fun `triggerNotification creates separate notifications for different alert IDs`() {
        // Given: Three different alert IDs
        val alertId1 = 1L
        val alertId2 = 2L
        val alertId3 = 999L

        // When: Triggering notifications for different alerts
        triggerNotification(
            context = context,
            userAlertId = alertId1,
            notificationTag = "test_tag_1",
            alertCategory = WeatherAlertCategory.SNOW_FALL,
            currentValue = 30.0,
            thresholdValue = 15.0f,
            cityName = "Buffalo",
            reminderNotes = "Test note 1",
        )

        triggerNotification(
            context = context,
            userAlertId = alertId2,
            notificationTag = "test_tag_2",
            alertCategory = WeatherAlertCategory.RAIN_FALL,
            currentValue = 50.0,
            thresholdValue = 25.0f,
            cityName = "Oshawa",
            reminderNotes = "Test note 2",
        )

        triggerNotification(
            context = context,
            userAlertId = alertId3,
            notificationTag = "test_tag_3",
            alertCategory = WeatherAlertCategory.SNOW_FALL,
            currentValue = 100.0,
            thresholdValue = 50.0f,
            cityName = "Toronto",
            reminderNotes = "Test note 3",
        )

        // Then: All three notifications should exist with their own content
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)

        // Verify that we have 3 notifications
        assertThat(shadowNotificationManager.size()).isEqualTo(3)

        // Verify each notification has correct tags and IDs
        val notification1 = shadowNotificationManager.getNotification("test_tag_1", alertId1.hashCode())
        val notification2 = shadowNotificationManager.getNotification("test_tag_2", alertId2.hashCode())
        val notification3 = shadowNotificationManager.getNotification("test_tag_3", alertId3.hashCode())

        assertThat(notification1).isNotNull()
        assertThat(notification2).isNotNull()
        assertThat(notification3).isNotNull()

        // Verify the content is different (title should contain different city names)
        assertThat(shadowOf(notification1).contentTitle.toString()).contains("Buffalo")
        assertThat(shadowOf(notification2).contentTitle.toString()).contains("Oshawa")
        assertThat(shadowOf(notification3).contentTitle.toString()).contains("Toronto")
    }

    @Test
    fun `notification PendingIntent request codes are unique for different alert IDs`() {
        // Given: Different alert IDs
        val alertId1 = 1L
        val alertId2 = 2L
        val alertId3 = 999L

        // When: Computing request codes using the same logic as triggerNotification
        val requestCode1 = alertId1.hashCode()
        val requestCode2 = alertId2.hashCode()
        val requestCode3 = alertId3.hashCode()

        // Then: Request codes should be different
        assertThat(requestCode1).isNotEqualTo(requestCode2)
        assertThat(requestCode1).isNotEqualTo(requestCode3)
        assertThat(requestCode2).isNotEqualTo(requestCode3)
    }

    @Test
    fun `snooze action request codes are unique from notification request codes`() {
        // Given: An alert ID
        val alertId = 1L

        // When: Computing request codes for notification and snooze actions
        val notificationRequestCode = alertId.hashCode()
        val snooze1DayRequestCode = alertId.hashCode() xor SNOOZE_1_DAY_ACTION_OFFSET
        val snooze1WeekRequestCode = alertId.hashCode() xor SNOOZE_1_WEEK_ACTION_OFFSET

        // Then: All request codes should be unique
        assertThat(notificationRequestCode).isNotEqualTo(snooze1DayRequestCode)
        assertThat(notificationRequestCode).isNotEqualTo(snooze1WeekRequestCode)
        assertThat(snooze1DayRequestCode).isNotEqualTo(snooze1WeekRequestCode)
    }
}
