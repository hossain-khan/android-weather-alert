package com.openmeteo.api

import com.openmeteo.api.OpenMeteo.Contexts
import com.openmeteo.api.common.Response
import com.openmeteo.api.common.time.Timezone
import com.openmeteo.api.common.units.PrecipitationUnit
import com.openmeteo.api.common.units.TemperatureUnit
import com.openmeteo.api.model.OpenMeteoForecastResponse
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/**
 * Service to interact with OpenMeteo API.
 *
 * - https://open-meteo.com/en/features
 * - https://open-meteo.com/en/docs to try API.
 */
interface OpenMeteoService {
    suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
    ): OpenMeteoForecastResponse
}

class OpenMeteoServiceImpl constructor(
    private val openMeteoClient: com.openmeteo.api.OpenMeteo
) : OpenMeteoService {
    @OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
    override suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
    ): OpenMeteoForecastResponse =
        withContext(Dispatchers.IO) {
            //Latitude and longitude are now part of the injected openMeteoClient's state or configuration.
            //The getWeatherForecast method's latitude and longitude parameters are used for the AppForecastData.

            val forecast: Forecast.Response =
                openMeteoClient // Use the injected client
                    .forecast {
                        hourly =
                            Forecast.Hourly {
                                listOf(snowfall, snowDepth, rain)
                            }
                        daily =
                            Forecast.Daily {
                                listOf(snowfallSum, rainSum)
                            }
                        forecastDays = 3
                        temperatureUnit = TemperatureUnit.Celsius
                        precipitationUnit = PrecipitationUnit.Millimeters
                        timezone = Timezone.auto
                    }.getOrThrow()

            val currentTimestamp = System.currentTimeMillis()
            var dailyCumulativeSnow = 0.0
            var nextDaySnow = 0.0
            var dailyCumulativeRain = 0.0
            var nextDayRain = 0.0
            var hourlyData: List<dev.hossain.weatheralert.datamodel.HourlyPrecipitation> = emptyList()
            Forecast.Daily.run {
                forecast.daily.getValue(snowfallSum).run {
                    nextDaySnow = values.values.firstOrNull() ?: 0.0
                }
                forecast.daily.getValue(rainSum).run {
                    nextDayRain = values.values.firstOrNull() ?: 0.0
                }
            }
            Forecast.Hourly.run {
                forecast.hourly.getValue(snowfall).run {
                    values
                        .filterKeys { it.time > currentTimestamp }
                        .values
                        .mapNotNull { it }
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .forEach { v ->
                            dailyCumulativeSnow += v
                        }

                    hourlyData =
                        values
                            .filterKeys { it.time > currentTimestamp }
                            .map {
                                dev.hossain.weatheralert.datamodel.HourlyPrecipitation(
                                    // ISO-8601 date-time string.
                                    isoDateTime =
                                        Instant
                                            .ofEpochMilli(it.key.time)
                                            .atZone(ZoneId.systemDefault())
                                            .toOffsetDateTime()
                                            .toString(),
                                    // Value is in cm, convert to mm
                                    snow = (it.value?.toDouble() ?: 0.0) * 10,
                                    rain = 0.0, // TODO - for rain loop again
                                )
                            }
                }
                forecast.hourly.getValue(snowDepth).run {
                    // This data is not used in the app.
                }
                forecast.hourly.getValue(rain).run {
                    values
                        .filterKeys { it.time > currentTimestamp }
                        .values
                        .mapNotNull { it }
                        .take(CUMULATIVE_DATA_HOURS_24)
                        .forEach { v ->
                            dailyCumulativeRain += v
                        }
                }
            }
            OpenMeteoForecastResponse(
                appForecastData =
                    AppForecastData(
                        latitude = latitude.toDouble(),
                        longitude = longitude.toDouble(),
                        snow =
                            Snow(
                                // Value is in cm, convert to mm
                                dailyCumulativeSnow = dailyCumulativeSnow * 10,
                                nextDaySnow = nextDaySnow * 10,
                                weeklyCumulativeSnow = 0.0,
                            ),
                        rain =
                            Rain(
                                // Value is in cm, convert to mm
                                dailyCumulativeRain = dailyCumulativeRain * 10,
                                nextDayRain = nextDayRain * 10,
                                weeklyCumulativeRain = 0.0,
                            ),
                        hourlyPrecipitation = hourlyData,
                    ),
            )
        }
}
