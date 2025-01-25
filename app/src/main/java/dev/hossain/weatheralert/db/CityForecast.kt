package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.WeatherService

@Entity(tableName = "city_forecasts")
data class CityForecast(
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
    val forecastSourceService: WeatherService,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
