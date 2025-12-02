package dev.hossain.weatheralert.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Converts timestamp to human readable date format.
 */
fun formatToDate(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
    return dateTime.format(formatter)
}

fun convertIsoToHourAmPm(isoDateTime: String): String {
    // Parse the ISO 8601 date-time string
    val zonedDateTime = ZonedDateTime.parse(isoDateTime)

    // Define a DateTimeFormatter to format the hour with AM/PM
    val formatter = DateTimeFormatter.ofPattern("ha", Locale.ENGLISH)

    // Format the ZonedDateTime to the desired format
    val formattedTime = zonedDateTime.format(formatter)

    return formattedTime
}

fun slimTimeLabel(hourOfDayLabel: String): String = hourOfDayLabel.replace("AM", "a").replace("PM", "p")

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

            hoursAgo > 0 -> {
                "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            }

            else -> {
                "$minutesAgo ${if (minutesAgo == 1L || minutesAgo == 0L) "minute" else "minutes"} ago"
            }
        }
    return timeAgoText
}

/**
 * Formats a snooze timestamp to a human-readable "Snoozed until" format.
 * Returns null if the timestamp is null or in the past.
 */
fun formatSnoozeUntil(snoozedUntil: Long?): String? {
    if (snoozedUntil == null || snoozedUntil <= System.currentTimeMillis()) {
        return null
    }

    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(snoozedUntil), ZoneId.systemDefault())
    val now = LocalDateTime.now(ZoneId.systemDefault())
    val tomorrow = now.plusDays(1).toLocalDate()

    return when {
        dateTime.toLocalDate() == now.toLocalDate() -> {
            // Same day - show just the time
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            "Snoozed until ${dateTime.format(formatter)}"
        }

        dateTime.toLocalDate() == tomorrow -> {
            // Tomorrow - show "tomorrow" with time
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            "Snoozed until tomorrow ${dateTime.format(formatter)}"
        }

        else -> {
            // Further in the future - show full date
            val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
            "Snoozed until ${dateTime.format(formatter)}"
        }
    }
}

/**
 * Formats a timestamp to a human-readable date and time format.
 * Used for displaying when alerts were triggered in the history.
 */
fun formatTimestampToDateTime(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.ENGLISH)
    return dateTime.format(formatter)
}
