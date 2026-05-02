package dev.hossain.weatheralert.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [createAppNotificationChannel] function.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationChannelTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Test
    fun `createAppNotificationChannel creates channel with correct ID`() {
        createAppNotificationChannel(context)

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel!!.id).isEqualTo(NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun `createAppNotificationChannel creates channel with correct name`() {
        createAppNotificationChannel(context)

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel!!.name.toString()).isEqualTo("Weather Alerts")
    }

    @Test
    fun `createAppNotificationChannel creates channel with high importance`() {
        createAppNotificationChannel(context)

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel!!.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
    }

    @Test
    fun `createAppNotificationChannel creates channel with correct description`() {
        createAppNotificationChannel(context)

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel!!.description).isEqualTo("Notifications for weather thresholds")
    }

    @Test
    fun `createAppNotificationChannel is idempotent when called multiple times`() {
        createAppNotificationChannel(context)
        createAppNotificationChannel(context)

        val channels: List<NotificationChannel> = notificationManager.notificationChannels

        // Calling createAppNotificationChannel multiple times should not create duplicate channels
        val weatherAlertsChannels = channels.filter { it.id == NOTIFICATION_CHANNEL_ID }
        assertThat(weatherAlertsChannels).hasSize(1)
    }
}
