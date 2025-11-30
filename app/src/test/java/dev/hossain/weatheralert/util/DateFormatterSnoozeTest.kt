package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
        // Calculate tomorrow at noon
        val calendar =
            java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 12)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
        val tomorrowTimestamp = calendar.timeInMillis

        val result = formatSnoozeUntil(tomorrowTimestamp)

        assertThat(result).isNotNull()
        assertThat(result).contains("tomorrow")
    }
}
