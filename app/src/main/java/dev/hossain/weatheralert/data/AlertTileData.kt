package dev.hossain.weatheralert.data

data class AlertTileData(
    val category: String,          // e.g., "Snowfall", "Rainfall"
    val threshold: String,         // e.g., "5 cm", "10 mm"
    val currentStatus: String      // e.g., "Tomorrow: 7 cm", "Tomorrow: 15 mm"
)
