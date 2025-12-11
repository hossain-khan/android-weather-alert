package dev.hossain.weatheralert.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared UI utilities for Developer Portal screens.
 *
 * This file contains common composables and utility functions used across multiple
 * devtools screens to reduce code duplication and maintain consistency.
 */

/**
 * A label-value row component used to display key-value pairs in developer tools.
 *
 * This composable is used consistently across Database Inspector, State Management,
 * History Simulator, and Worker Tester screens for displaying information pairs.
 *
 * @param label The label text (e.g., "Cities", "Weather Service")
 * @param value The value text to display
 * @param modifier Optional modifier for the row
 *
 * Example usage:
 * ```kotlin
 * InfoRow(label = "Total Alerts", value = "5")
 * InfoRow(label = "Last Check", value = "Dec 11, 14:30")
 * ```
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Formats a timestamp in milliseconds to a human-readable date string.
 *
 * Used across devtools screens to display timestamps consistently.
 * Format: "MMM dd, yyyy HH:mm" (e.g., "Dec 11, 2024 14:30")
 *
 * @param timestamp Unix timestamp in milliseconds
 * @return Formatted date string
 *
 * Example usage:
 * ```kotlin
 * val timestamp = System.currentTimeMillis()
 * val formatted = formatDate(timestamp) // "Dec 11, 2024 14:30"
 * ```
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Formats a timestamp for short display without year.
 *
 * Used in State Management screen for more compact date display.
 * Format: "MMM dd, HH:mm" (e.g., "Dec 11, 14:30")
 *
 * @param timestamp Unix timestamp in milliseconds
 * @return Formatted date string without year
 *
 * Example usage:
 * ```kotlin
 * val lastCheck = prefs.lastCheckTime
 * val formatted = formatDateShort(lastCheck) // "Dec 11, 14:30"
 * ```
 */
fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Formats byte size to human-readable format (B, KB, MB).
 *
 * Used in Database Inspector to display file sizes.
 *
 * @param bytes Size in bytes
 * @return Formatted size string (e.g., "2.5 MB", "512 KB", "128 B")
 *
 * Example usage:
 * ```kotlin
 * val dbSize = dbFile.length()
 * val formatted = formatBytes(dbSize) // "1.2 MB"
 * ```
 */
fun formatBytes(bytes: Long): String =
    when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }

/**
 * Masks an API key for safe display in UI.
 *
 * Shows only the last 4 characters of the API key, replacing the rest with asterisks.
 * Used in State Management screen to display API keys without exposing sensitive data.
 *
 * @param apiKey The API key to mask (can be null or empty)
 * @return Masked string in format "****abc1" or "Not set" if empty
 *
 * Example usage:
 * ```kotlin
 * val key = "1234567890abcdef"
 * val masked = maskApiKey(key) // "****cdef"
 * val empty = maskApiKey(null) // "Not set"
 * ```
 */
fun maskApiKey(apiKey: String?): String =
    if (apiKey.isNullOrEmpty()) {
        "Not set"
    } else if (apiKey.length <= 4) {
        "****"
    } else {
        "****" + apiKey.takeLast(4)
    }
