package dev.hossain.weatheralert.util

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.di.AppScope
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

interface ClockProvider {
    fun getClock(): Clock
}

@ContributesBinding(AppScope::class)
class DefaultClockProvider
    @Inject
    constructor() : ClockProvider {
        override fun getClock(): Clock = Clock.systemDefaultZone()
    }

interface TimeUtil {
    fun getCurrentTimeMillis(clock: Clock): Long

    fun isOlderThan24Hours(
        clock: Clock,
        timeInMillis: Long,
    ): Boolean
}

@ContributesBinding(AppScope::class)
class TimeUtilImpl
    @Inject
    constructor() : TimeUtil {
        override fun getCurrentTimeMillis(clock: Clock): Long =
            Instant
                .now(clock)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        override fun isOlderThan24Hours(
            clock: Clock,
            timeInMillis: Long,
        ): Boolean {
            val currentTime = getCurrentTimeMillis(clock)
            return (currentTime - timeInMillis) > 24 * 60 * 60 * 1000
        }
    }
