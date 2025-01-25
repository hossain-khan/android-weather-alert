package dev.hossain.weatheralert.datamodel

import androidx.annotation.Keep

/**
 * List of supported weather services.
 */
@Keep
enum class WeatherService(
    /**
     * Indicates if the weather forecast service is enabled or disabled at build time.
     */
    val isEnabled: Boolean = true,
) {
    /**
     * OpenWeatherMap API for weather forecast.
     * - https://openweathermap.org/api
     *
     * @see OpenWeatherService
     */
    OPEN_WEATHER_MAP,

    /**
     * Tomorrow.io API for weather forecast.
     * - https://app.tomorrow.io/home
     *
     * @see TomorrowIoService
     */
    TOMORROW_IO,

    /**
     * Open-Meteo API for weather forecast.
     * - https://open-meteo.com/en/docs
     *
     * @see OpenMeteoService
     */
    OPEN_METEO(
        /**
         * Disabled to service as the forecast data was not reliable.
         * See https://github.com/hossain-khan/android-weather-alert/pull/164
         */
        isEnabled = false,
    ),
}
