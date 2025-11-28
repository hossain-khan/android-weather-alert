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

    @Test
    fun formatTimestampToElapsedTime_oneHourAndOneMinuteAgo() {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1) - TimeUnit.MINUTES.toMillis(1)
        val result = formatTimestampToElapsedTime(timestamp)
        assertThat(result).isEqualTo("1 hour and 1 minute ago")
    }

    @Test
    fun convertIsoToHourAmPm_validIsoDateTime() {
        val isoDateTime = "2023-10-10T14:30:00Z"
        val result = convertIsoToHourAmPm(isoDateTime)
        assertThat(result).isEqualTo("2PM")
    }

    @Test
    fun convertIsoToHourAmPm_isoDateTimeWithMilliseconds() {
        val isoDateTime = "2023-10-10T14:30:00.123Z"
        val result = convertIsoToHourAmPm(isoDateTime)
        assertThat(result).isEqualTo("2PM")
    }

    @Test
    fun convertIsoToHourAmPm_isoDateTimeWithOffset() {
        val isoDateTime = "2023-10-10T14:30:00+02:00"
        val result = convertIsoToHourAmPm(isoDateTime)
        assertThat(result).isEqualTo("2PM")
    }

    @Test
    fun convertIsoToHourAmPm_isoDateTimeWithDifferentTimeZone() {
        val isoDateTime = "2023-10-10T14:30:00-05:00"
        val result = convertIsoToHourAmPm(isoDateTime)
        assertThat(result).isEqualTo("2PM")
    }

    @Test
    fun convertIsoToHourAmPm_morningTime() {
        val isoDateTime = "2023-10-10T09:00:00Z"
        val result = convertIsoToHourAmPm(isoDateTime)
        assertThat(result).isEqualTo("9AM")
    }

    @Test
    fun slimTimeLabel_replacesAmWithA() {
        val result = slimTimeLabel("9AM")
        assertThat(result).isEqualTo("9a")
    }

    @Test
    fun slimTimeLabel_replacesPmWithP() {
        val result = slimTimeLabel("2PM")
        assertThat(result).isEqualTo("2p")
    }

    @Test
    fun slimTimeLabel_leavesLowercaseUnchanged() {
        // The function only replaces uppercase AM/PM, so lowercase remains as-is
        val result = slimTimeLabel("9am")
        assertThat(result).isEqualTo("9am")
    }

    @Test
    fun slimTimeLabel_handlesNoAmPm() {
        val result = slimTimeLabel("12:00")
        assertThat(result).isEqualTo("12:00")
    }

    @Test
    fun formatToDate_formatsTimestampCorrectly() {
        // Jan 15, 2025 at 2:30 PM in UTC
        val timestamp = 1736952600000L
        val result = formatToDate(timestamp)
        // The format is "MMM d 'at' h:mm a" - result depends on system timezone
        assertThat(result).isNotEmpty()
        assertThat(result).contains("at")
    }
}
