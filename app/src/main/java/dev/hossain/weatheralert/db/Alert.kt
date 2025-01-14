package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory

/**
 * Represents a user's alert for a specific city.
 */
@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            // The referenced entity
            entity = City::class,
            // `"id"`: Column in the referenced table
            parentColumns = ["id"],
            // `"city_id"`: Column in the current table
            childColumns = ["city_id"],
            // Optional: cascade delete related alerts if a city is deleted
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Index for performance on foreign key queries (required for foreign key)
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
