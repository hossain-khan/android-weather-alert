package dev.hossain.weatheralert.util

import dev.zacsweers.metro.Inject
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

interface ClockProvider {
    fun getClock(): Clock
}

@Inject
class DefaultClockProvider
    constructor() : ClockProvider {
        override fun getClock(): Clock = Clock.systemDefaultZone()
    }

interface TimeUtil {
    fun getCurrentTimeMillis(): Long

    fun isOlderThan24Hours(timeInMillis: Long): Boolean
}

@Inject
class TimeUtilImpl
    constructor(
        private val clock: ClockProvider,
    ) : TimeUtil {
        override fun getCurrentTimeMillis(): Long =
            Instant
                .now(clock.getClock())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        override fun isOlderThan24Hours(timeInMillis: Long): Boolean {
            val currentTime = getCurrentTimeMillis()
            return (currentTime - timeInMillis) > 24 * 60 * 60 * 1000
        }
    }
