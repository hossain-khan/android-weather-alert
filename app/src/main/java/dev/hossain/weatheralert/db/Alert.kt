package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.data.WeatherAlertCategory

/**
 * Represents a user's alert for a specific city.
 */
@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = City::class,
            parentColumns = ["id"],
            childColumns = ["city_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["city_id"])],
)
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /**
     * Foreign key to City table.
     * @see [City.id]
     */
    @ColumnInfo(name = "city_id") val cityId: Int,
    @ColumnInfo(name = "alert_category") val alertCategory: WeatherAlertCategory,
    val threshold: Float,
    val notes: String = "",
)
