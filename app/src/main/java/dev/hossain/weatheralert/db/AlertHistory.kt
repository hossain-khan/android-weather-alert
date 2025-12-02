package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory

/**
 * Represents the history of triggered weather alerts.
 * This entity stores records of when alerts were triggered and their associated values.
 */
@Entity(
    tableName = "alert_history",
    foreignKeys = [
        ForeignKey(
            entity = Alert::class,
            parentColumns = ["id"],
            childColumns = ["alert_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["alert_id"]), Index(value = ["triggered_at"])],
)
data class AlertHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "alert_id") val alertId: Long,
    @ColumnInfo(name = "triggered_at") val triggeredAt: Long,
    @ColumnInfo(name = "weather_value") val weatherValue: Double,
    @ColumnInfo(name = "threshold_value") val thresholdValue: Float,
    @ColumnInfo(name = "city_name") val cityName: String,
    @ColumnInfo(name = "alert_category") val alertCategory: WeatherAlertCategory,
)
