package dev.hossain.weatheralert.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A reusable component for displaying a label-value pair in developer tools screens.
 *
 * This composable creates a row with a label on the left (using secondary color)
 * and a value on the right (using primary color). It's commonly used across devtools
 * screens to display consistent key-value information.
 *
 * @param label The label text to display on the left side
 * @param value The value text to display on the right side
 * @param modifier Optional modifier to customize the row's layout
 *
 * @see DatabaseInspectorScreen For database information display
 * @see StateManagementScreen For state management information display
 * @see WorkerTesterScreen For worker status display
 * @see HistorySimulatorScreen For history simulation information display
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Formats a timestamp in milliseconds to a human-readable date-time string.
 *
 * This utility function converts epoch milliseconds to a formatted string
 * in the pattern "MMM dd, yyyy HH:mm:ss" (e.g., "Jan 15, 2024 14:30:45").
 *
 * @param timestamp The timestamp in milliseconds since epoch
 * @return A formatted date-time string, or "N/A" if timestamp is 0
 *
 * @see formatDateShort For a shorter date format without time
 * @see DatabaseInspectorScreen.DatabaseStats For last update timestamp display
 * @see StateManagementScreen.UserState For last fetch timestamp display
 */
fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

/**
 * Formats a timestamp in milliseconds to a short date string.
 *
 * This utility function converts epoch milliseconds to a compact date string
 * in the pattern "MMM dd, HH:mm" (e.g., "Jan 15, 14:30").
 * Useful when space is limited and full date-time is not needed.
 *
 * @param timestamp The timestamp in milliseconds since epoch
 * @return A formatted short date string, or "N/A" if timestamp is 0
 *
 * @see formatDate For full date-time format
 * @see HistorySimulatorScreen For recent entry timestamp display
 */
fun formatDateShort(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Formats a byte count into a human-readable size string.
 *
 * This utility function converts raw byte counts into formatted strings
 * with appropriate units (B, KB, MB, GB). It uses 1024 as the base for
 * binary units.
 *
 * Examples:
 * - 512 bytes → "512 B"
 * - 1536 bytes → "1.5 KB"
 * - 2097152 bytes → "2.0 MB"
 *
 * @param bytes The number of bytes to format
 * @return A formatted size string with appropriate unit
 *
 * @see DatabaseInspectorScreen.DatabaseStats For database size display
 */
fun formatBytes(bytes: Long): String =
    when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
    }

/**
 * Masks an API key for secure display in the UI.
 *
 * This utility function takes an API key string and returns a masked version
 * that shows only the first 4 and last 4 characters, with asterisks in between.
 * This prevents sensitive API keys from being fully exposed in developer tools
 * while still allowing identification of which key is being used.
 *
 * Examples:
 * - "abc123def456ghi789" → "abc1***i789"
 * - Short keys (< 8 chars) → "****"
 * - Empty/blank keys → "Not set"
 *
 * @param apiKey The API key string to mask
 * @return A masked version of the API key safe for display
 *
 * @see StateManagementScreen.UserState For API key display in state management
 */
fun maskApiKey(apiKey: String): String =
    when {
        apiKey.isBlank() -> "Not set"
        apiKey.length < 8 -> "****"
        else -> "${apiKey.take(4)}***${apiKey.takeLast(4)}"
    }
