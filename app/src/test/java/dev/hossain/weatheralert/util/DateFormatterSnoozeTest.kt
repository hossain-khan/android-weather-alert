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
    fun `formatSnoozeUntil returns formatted string when snoozedUntil is in the future`() {
        val futureTimestamp = System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour in future

        val result = formatSnoozeUntil(futureTimestamp)

        assertThat(result).isNotNull()
        assertThat(result).startsWith("Snoozed until")
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
}
