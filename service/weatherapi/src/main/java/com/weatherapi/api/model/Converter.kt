package com.weatherapi.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import java.time.Instant
import java.time.ZoneId

internal fun ForecastWeatherResponse.toForecastData(): AppForecastData =
    AppForecastData(
        latitude = location.lat,
        longitude = location.lon,
        snow = forecast.toSnow(),
        rain = forecast.toRain(),
        hourlyPrecipitation = forecast.forecastDay.map { it.toHourlyPrecipitation() }.flatten(),
    )

private fun ForecastDay.toHourlyPrecipitation(): List<HourlyPrecipitation> {
    val currentTimeSeconds = System.currentTimeMillis() / 1000
    return this.hour.filter { it.timeEpoch > currentTimeSeconds }.map {
        HourlyPrecipitation(
            // ISO-8601 date-time string.
            isoDateTime =
                Instant
                    .ofEpochSecond(it.timeEpoch)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                    .toString(),
            rain = it.precipMm,
            snow = it.snowCm * 10.0,
        )
    }
}

private fun Forecast.toSnow(): Snow {
    val dailyCumulativeSnow =
        forecastDay
            .firstOrNull()
            ?.day
            ?.totalSnowCm
            ?.times(10.0) ?: 0.0
    val nextDaySnow =
        forecastDay
            .getOrNull(1)
            ?.day
            ?.totalSnowCm
            ?.times(10.0) ?: 0.0
    return Snow(
        dailyCumulativeSnow = dailyCumulativeSnow,
        nextDaySnow = nextDaySnow,
        weeklyCumulativeSnow = 0.0,
    )
}

private fun Forecast.toRain(): Rain {
    val dailyCumulativeRain = forecastDay.take(24).sumOf { it.day.totalPrecipMm }
    val nextDayRain = forecastDay.getOrNull(1)?.day?.totalPrecipMm ?: 0.0
    return Rain(
        dailyCumulativeRain = dailyCumulativeRain,
        nextDayRain = nextDayRain,
        weeklyCumulativeRain = 0.0,
    )
}
