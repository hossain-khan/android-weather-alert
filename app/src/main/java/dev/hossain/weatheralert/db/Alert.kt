package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.data.WeatherAlertCategory

/**
 * Represents a user's alert for a specific city.
 */
@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int,
    @ColumnInfo(name = "alert_category") val alertCategory: WeatherAlertCategory,
    val threshold: Float,
    val notes: String = "",
)
