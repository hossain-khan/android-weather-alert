package com.weatherapi.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import java.time.Instant
import java.time.ZoneId

internal fun ForecastWeatherResponse.toForecastData(): AppForecastData {
    // Get current time millis in current time zone
    val currentTimeMillis = Instant.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val currentTimeSeconds = currentTimeMillis / 1000

    val hourlyPrecipitation =
        forecast.forecastDay.map { it.toHourlyPrecipitation(currentTimeSeconds) }.flatten()
    return AppForecastData(
        latitude = location.lat,
        longitude = location.lon,
        snow = forecast.toSnow(hourlyPrecipitation),
        rain = forecast.toRain(hourlyPrecipitation),
        hourlyPrecipitation = hourlyPrecipitation,
    )
}

private fun ForecastDay.toHourlyPrecipitation(currentTimeSeconds: Long): List<HourlyPrecipitation> =
    this.hour.filter { it.timeEpoch > currentTimeSeconds }.map {
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

private fun Forecast.toSnow(nextHourlyPrecipitation: List<HourlyPrecipitation>): Snow {
    val dailyCumulativeSnow = nextHourlyPrecipitation.take(CUMULATIVE_DATA_HOURS_24).sumOf { it.snow }
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

private fun Forecast.toRain(nextHourlyPrecipitation: List<HourlyPrecipitation>): Rain {
    val dailyCumulativeRain = nextHourlyPrecipitation.take(CUMULATIVE_DATA_HOURS_24).sumOf { it.rain }
    val nextDayRain = forecastDay.getOrNull(1)?.day?.totalPrecipMm ?: 0.0
    return Rain(
        dailyCumulativeRain = dailyCumulativeRain,
        nextDayRain = nextDayRain,
        weeklyCumulativeRain = 0.0,
    )
}
