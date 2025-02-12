package org.openweathermap.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import java.time.Instant
import java.time.ZoneId

/**
 * Extension function to convert a WeatherForecast object to an AppForecastData object.
 *
 * @receiver WeatherForecast The WeatherForecast object to be converted.
 * @return AppForecastData The converted AppForecastData object.
 */
internal fun WeatherForecast.toForecastData(): AppForecastData =
    AppForecastData(
        latitude = lat,
        longitude = lon,
        snow =
            Snow(
                dailyCumulativeSnow =
                    hourly
                        .sumOf { it.snow?.snowVolumeInAnHour ?: 0.0 } * 10,
                nextDaySnow =
                    daily
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .firstOrNull()
                        ?.snowVolume ?: 0.0,
                weeklyCumulativeSnow = 0.0,
            ),
        rain =
            Rain(
                dailyCumulativeRain =
                    hourly
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .sumOf { it.rain?.rainVolumeInAnHour ?: 0.0 },
                nextDayRain =
                    daily
                        .firstOrNull()
                        ?.rainVolume ?: 0.0,
                weeklyCumulativeRain = 0.0,
            ),
        hourlyPrecipitation = hourly.map { it.toHourlyPrecipitation() },
    )

private fun HourlyForecast.toHourlyPrecipitation(): HourlyPrecipitation =
    HourlyPrecipitation(
        // ISO-8601 date-time string.
        isoDateTime =
            Instant
                .ofEpochSecond(date)
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .toString(),
        snow = (snow?.snowVolumeInAnHour ?: 0.0) * 10,
        rain = rain?.rainVolumeInAnHour ?: 0.0,
    )
