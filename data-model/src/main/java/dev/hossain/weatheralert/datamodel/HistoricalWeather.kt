package dev.hossain.weatheralert.datamodel

import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.WeatherForecastService

/**
 * Immutable data model for historical weather record.
 */
data class HistoricalWeather(
    val id: Long = 0,
    val cityId: Long,
    val date: Long, // epoch millis for the day
    val latitude: Double,
    val longitude: Double,
    val snow: Double,
    val rain: Double,
    val forecastSourceService: WeatherForecastService,
    val hourlyPrecipitation: List<HourlyPrecipitation> = emptyList(),
)
