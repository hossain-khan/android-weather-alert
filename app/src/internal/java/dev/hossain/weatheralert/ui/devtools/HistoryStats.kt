package dev.hossain.weatheralert.ui.devtools

/**
 * Statistics about alert history.
 *
 * @property totalCount Total number of alert history records
 * @property last7DaysCount Number of alerts triggered in the last 7 days
 * @property last30DaysCount Number of alerts triggered in the last 30 days
 * @property snowCount Number of snow alerts
 * @property rainCount Number of rain alerts
 */
data class HistoryStats(
    val totalCount: Int,
    val last7DaysCount: Int,
    val last30DaysCount: Int,
    val snowCount: Int,
    val rainCount: Int,
)
