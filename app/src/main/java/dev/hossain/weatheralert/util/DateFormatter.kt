package dev.hossain.weatheralert.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Converts timestamp to human readable date format.
 */
fun formatToDate(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
    return dateTime.format(formatter)
}

fun formatTimestampToElapsedTime(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - timestamp
    val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
    val hoursAgo = TimeUnit.MILLISECONDS.toHours(timeDifference)

    val timeAgoText =
        when {
            hoursAgo > 0 && minutesAgo % 60 > 0 -> {
                "$hoursAgo ${if (hoursAgo == 1L) {
                    "hour"
                } else {
                    "hours"
                }} and ${minutesAgo % 60} ${if (minutesAgo % 60 == 1L) {
                    "minute"
                } else {
                    "minutes"
                }} ago"
            }
            hoursAgo > 0 -> "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            else -> "$minutesAgo ${if (minutesAgo == 1L || minutesAgo == 0L) "minute" else "minutes"} ago"
        }
    return timeAgoText
}
