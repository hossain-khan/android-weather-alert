package dev.hossain.weatheralert.datamodel

/**
 * Marker interface for all weather API service response that must
 * provide converted [AppForecastData] using [convertToForecastData].
 */
interface WeatherApiServiceResponse {
    /**
     * Converts the weather API service response to [AppForecastData] that is used
     * for caching and throughout the app.
     */
    fun convertToForecastData(): AppForecastData
}
