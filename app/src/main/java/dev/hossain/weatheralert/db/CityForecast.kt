package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.ForecastServiceSource
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation

@Entity(tableName = "city_forecasts")
data class CityForecast constructor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "forecast_id")
    val forecastId: Long = 0,
    val cityId: Long,
    val latitude: Double,
    val longitude: Double,
    val dailyCumulativeSnow: Double,
    val nextDaySnow: Double,
    val dailyCumulativeRain: Double,
    val nextDayRain: Double,
    val forecastSourceService: ForecastServiceSource,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    // Empty JSON array list by default
    @ColumnInfo(defaultValue = "[]")
    val hourlyPrecipitation: List<HourlyPrecipitation>,
)
