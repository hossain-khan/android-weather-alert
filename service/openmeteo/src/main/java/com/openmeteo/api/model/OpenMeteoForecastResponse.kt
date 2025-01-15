package com.openmeteo.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse

data class OpenMeteoForecastResponse(
    /**
     * Directly convert to the forecast data needed by the app.
     */
    val appForecastData: AppForecastData,
) : WeatherApiServiceResponse {
    override fun convertToForecastData(): AppForecastData = appForecastData
}
