package dev.hossain.weatheralert.util

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject

interface TimeUtil {
    fun getCurrentTimeMillis(): Long

    fun isOlderThan24Hours(timeInMillis: Long): Boolean
}

@ContributesBinding(AppScope::class)
class TimeUtilImpl
    @Inject
    constructor() : TimeUtil {
        override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

        override fun isOlderThan24Hours(timeInMillis: Long): Boolean {
            val currentTime = getCurrentTimeMillis()
            return (currentTime - timeInMillis) > 24 * 60 * 60 * 1000
        }
    }
