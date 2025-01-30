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

class OpenMeteoServiceImpl constructor() : OpenMeteoService {
    @OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
    override suspend fun getWeatherForecast(
        latitude: Float,
        longitude: Float,
    ): OpenMeteoForecastResponse =
        withContext(Dispatchers.IO) {
            val om =
                OpenMeteo(
                    latitude = latitude,
                    longitude = longitude,
                    apikey = null,
                    contexts = Contexts(),
                )

            val forecast: Forecast.Response =
                om
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

            var dailyCumulativeSnow = 0.0
            var nextDaySnow = 0.0
            var dailyCumulativeRain = 0.0
            var nextDayRain = 0.0
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
                    values.values.mapNotNull { it }.take(CUMULATIVE_DATA_HOURS_24).forEach { v ->
                        dailyCumulativeSnow += v
                    }
                }
                forecast.hourly.getValue(snowDepth).run {
                    // TODO - check if we really need this data
                }
                forecast.hourly.getValue(rain).run {
                    values.values.mapNotNull { it }.take(CUMULATIVE_DATA_HOURS_24).forEach { v ->
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
                                dailyCumulativeSnow = dailyCumulativeSnow,
                                nextDaySnow = nextDaySnow,
                                weeklyCumulativeSnow = 0.0,
                            ),
                        rain =
                            Rain(
                                dailyCumulativeRain = dailyCumulativeRain,
                                nextDayRain = nextDayRain,
                                weeklyCumulativeRain = 0.0,
                            ),
                        hourlyPrecipitation = emptyList(), // NOT IMPLEMENTED YET
                    ),
            )
        }
}
