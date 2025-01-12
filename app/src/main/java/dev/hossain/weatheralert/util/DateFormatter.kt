package dev.hossain.weatheralert.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Converts timestamp to human readable date format.
 */
fun formatToDate(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
    return dateTime.format(formatter)
}
