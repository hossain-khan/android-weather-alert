package dev.hossain.weatheralert.datamodel

/**
 * Marker interface for all weather API service response that must provide converted [AppForecastData].
 */
interface WeatherApiServiceResponse {
    fun convertToForecastData(): AppForecastData
}