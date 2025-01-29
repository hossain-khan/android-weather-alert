package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class DateTimeFormatterTest {
    @Test
    fun formatTimestampToElapsedTime_minutesAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("5 minutes ago")
    }

    @Test
    fun formatTimestampToElapsedTime_oneMinuteAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("1 minute ago")
    }

    @Test
    fun formatTimestampToElapsedTime_zeroMinuteAgo() {
        val timestamp = System.currentTimeMillis()
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("0 minute ago")
    }

    @Test
    fun formatTimestampToElapsedTime_hoursAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("3 hours ago")
    }

    @Test
    fun formatTimestampToElapsedTime_hoursAndMinutesAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) - TimeUnit.MINUTES.toMillis(30)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("2 hours and 30 minutes ago")
    }

    @Test
    fun formatTimestampToElapsedTime_oneHourAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("1 hour ago")
    }
}
