package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class DateFormatterSnoozeTest {
    @Test
    fun `formatSnoozeUntil returns null when snoozedUntil is null`() {
        val result = formatSnoozeUntil(null)

        assertThat(result).isNull()
    }

    @Test
    fun `formatSnoozeUntil returns null when snoozedUntil is in the past`() {
        val pastTimestamp = System.currentTimeMillis() - 60 * 60 * 1000 // 1 hour ago

        val result = formatSnoozeUntil(pastTimestamp)

        assertThat(result).isNull()
    }

    @Test
    fun `formatSnoozeUntil returns null when snoozedUntil equals current time`() {
        val currentTimestamp = System.currentTimeMillis()

        val result = formatSnoozeUntil(currentTimestamp)

        // The condition is `snoozedUntil <= currentTimeMillis()` so equal time returns null
        assertThat(result).isNull()
    }

    @Test
    fun `formatSnoozeUntil returns formatted string when snoozedUntil is later today`() {
        val futureTimestamp = System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour in future

        val result = formatSnoozeUntil(futureTimestamp)

        assertThat(result).isNotNull()
        assertThat(result).startsWith("Snoozed until")
        // Should not contain "tomorrow" since it is later today
        assertThat(result).doesNotContain("tomorrow")
    }

    @Test
    fun `formatSnoozeUntil includes tomorrow in string for next day`() {
        // Use java.time APIs for more deterministic date calculation
        val now = LocalDateTime.now()
        val tomorrow =
            now
                .plusDays(1)
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        val tomorrowTimestamp =
            tomorrow
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        val result = formatSnoozeUntil(tomorrowTimestamp)

        assertThat(result).isNotNull()
        assertThat(result).contains("tomorrow")
    }

    @Test
    fun `formatSnoozeUntil includes date for far future timestamp`() {
        // A timestamp 3 days from now (beyond tomorrow)
        val now = LocalDateTime.now()
        val threeDaysLater =
            now
                .plusDays(3)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        val farFutureTimestamp =
            threeDaysLater
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        val result = formatSnoozeUntil(farFutureTimestamp)

        assertThat(result).isNotNull()
        assertThat(result).startsWith("Snoozed until")
        // Should not contain "tomorrow" for 3 days out
        assertThat(result).doesNotContain("tomorrow")
        // Should contain "at" from the "MMM d 'at' h:mm a" format
        assertThat(result).contains("at")
    }

    @Test
    fun `formatTimestampToDateTime formats timestamp correctly`() {
        // Use a fixed timestamp: Jan 15, 2025 at 10:30 AM UTC
        // Note: result is in system timezone, so we only check the format is non-empty
        val timestamp = 1736938200000L // 2025-01-15T10:30:00Z

        val result = formatTimestampToDateTime(timestamp)

        assertThat(result).isNotNull()
        assertThat(result).isNotEmpty()
        // Should follow "MMM d, yyyy 'at' h:mm a" format
        assertThat(result).contains("2025")
        assertThat(result).contains("at")
        assertThat(result).containsMatch("(AM|PM)")
    }

    @Test
    fun `formatTimestampToDateTime formats January date with correct month abbreviation`() {
        // Jan 15, 2025 at 10:30 AM UTC
        val timestamp = 1736938200000L // 2025-01-15T10:30:00Z

        val result = formatTimestampToDateTime(timestamp)

        assertThat(result).contains("Jan")
    }

    @Test
    fun `formatTimestampToDateTime formats year correctly`() {
        // Jan 15, 2025 UTC
        val timestamp = 1736938200000L // 2025-01-15T10:30:00Z

        val result = formatTimestampToDateTime(timestamp)

        assertThat(result).contains("2025")
    }
}
