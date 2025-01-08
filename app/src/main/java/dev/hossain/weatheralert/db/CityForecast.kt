package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_forecasts")
data class CityForecast(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "forecast_id")
    val forecastId: Int = 0,
    val cityId: Int,
    val latitude: Double,
    val longitude: Double,
    val dailyCumulativeSnow: Double,
    val nextDaySnow: Double,
    val dailyCumulativeRain: Double,
    val nextDayRain: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
