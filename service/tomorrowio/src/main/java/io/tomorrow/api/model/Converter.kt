package io.tomorrow.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow

/**
 * Extension function to convert a WeatherResponse object to an AppForecastData object.
 *
 * @receiver WeatherResponse The WeatherResponse object to be converted.
 * @return AppForecastData The converted AppForecastData object.
 */
internal fun WeatherResponse.toForecastData(): AppForecastData =
    AppForecastData(
        latitude = location.latitude,
        longitude = location.longitude,
        snow =
            Snow(
                dailyCumulativeSnow =
                    timelines.hourly
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .sumOf { it.values.snowDepth ?: 0.0 },
                nextDaySnow =
                    timelines.daily
                        .firstOrNull()
                        ?.values
                        ?.snowAccumulation ?: 0.0,
                weeklyCumulativeSnow = 0.0,
            ),
        rain =
            Rain(
                dailyCumulativeRain =
                    timelines.hourly
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .sumOf { it.values.rainAccumulation ?: 0.0 },
                nextDayRain =
                    timelines.daily
                        .firstOrNull()
                        ?.values
                        ?.rainAccumulation ?: 0.0,
                weeklyCumulativeRain = 0.0,
            ),
        hourlyPrecipitation = timelines.hourly.map { it.toHourlyPrecipitation() },
    )

private fun TimelineData.toHourlyPrecipitation(): HourlyPrecipitation =
    HourlyPrecipitation(
        isoDateTime = time,
        snow = values.snowDepth ?: 0.0,
        rain = values.rainAccumulation ?: 0.0,
    )
