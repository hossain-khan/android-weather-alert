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
    fun getCurrentTimeMillis(): Long

    fun isOlderThan24Hours(timeInMillis: Long): Boolean
}

@ContributesBinding(AppScope::class)
class TimeUtilImpl
    @Inject
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
