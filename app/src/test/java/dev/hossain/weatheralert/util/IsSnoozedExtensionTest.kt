package dev.hossain.weatheralert.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for the [Long?.isSnoozed] extension function.
 */
class IsSnoozedExtensionTest {
    @Test
    fun `isSnoozed returns false for null`() {
        val snoozedUntil: Long? = null

        assertThat(snoozedUntil.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns false for timestamp in the past`() {
        val pastTimestamp = System.currentTimeMillis() - 1000L // 1 second ago

        assertThat(pastTimestamp.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns false for timestamp equal to current time`() {
        val currentTimestamp = System.currentTimeMillis()

        // The function uses >, so equal to current time should return false
        assertThat(currentTimestamp.isSnoozed()).isFalse()
    }

    @Test
    fun `isSnoozed returns true for timestamp in the future`() {
        val futureTimestamp = System.currentTimeMillis() + 60 * 60 * 1000L // 1 hour from now

        assertThat(futureTimestamp.isSnoozed()).isTrue()
    }

    @Test
    fun `isSnoozed returns true for timestamp far in the future`() {
        val farFutureTimestamp = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L // 1 week from now

        assertThat(farFutureTimestamp.isSnoozed()).isTrue()
    }

    @Test
    fun `isSnoozed returns false for zero timestamp`() {
        val zeroTimestamp = 0L

        assertThat(zeroTimestamp.isSnoozed()).isFalse()
    }
}
