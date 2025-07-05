package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.WeatherForecastService

@Entity(tableName = "historical_weather")
data class HistoricalWeatherEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "city_id")
    val cityId: Long,
    @ColumnInfo(name = "date")
    val date: Long, // Store as epoch millis for the day
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "snow")
    val snow: Double,
    @ColumnInfo(name = "rain")
    val rain: Double,
    @ColumnInfo(name = "forecast_source_service")
    val forecastSourceService: WeatherForecastService,
    @ColumnInfo(name = "hourly_precipitation", defaultValue = "[]")
    val hourlyPrecipitation: List<HourlyPrecipitation> = emptyList(),
)
