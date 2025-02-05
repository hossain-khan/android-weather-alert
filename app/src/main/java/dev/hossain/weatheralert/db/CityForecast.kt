package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.WeatherForecastService

@Entity(
    tableName = "city_forecasts",
    foreignKeys = [
        ForeignKey(
            entity = Alert::class,
            parentColumns = ["id"],
            childColumns = ["alert_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // Index for performance on foreign key queries (required for foreign key)
        Index(value = ["alert_id"]),
    ],
)
data class CityForecast constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "forecast_id")
    val forecastId: Long = 0,
    /**
     * Foreign key to City table.
     * @see City.id
     */
    @ColumnInfo(name = "city_id")
    val cityId: Long,
    /**
     * Foreign key to Alert table.
     * @see Alert.id
     */
    @ColumnInfo(name = "alert_id", defaultValue = ALERT_ID_NONE.toString())
    val alertId: Long,
    val latitude: Double,
    val longitude: Double,
    val dailyCumulativeSnow: Double,
    val nextDaySnow: Double,
    val dailyCumulativeRain: Double,
    val nextDayRain: Double,
    val forecastSourceService: WeatherForecastService,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    // Empty JSON array list by default
    @ColumnInfo(defaultValue = "[]")
    val hourlyPrecipitation: List<HourlyPrecipitation>,
)
