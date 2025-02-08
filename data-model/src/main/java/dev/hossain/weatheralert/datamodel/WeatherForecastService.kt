package dev.hossain.weatheralert.datamodel

import androidx.annotation.Keep

/**
 * List of supported weather services.
 */
@Keep
enum class WeatherForecastService(
    /**
     * Indicates API key is required to avoid rate limiting or overuse.
     * Some services does not require API key or has high limit making it not required to provide API key.
     */
    val requiresApiKey: Boolean = false,
    /**
     * Indicates if the weather forecast service is enabled or disabled at build time.
     */
    val isEnabled: Boolean = true,
) {
    /**
     * OpenWeatherMap API for weather forecast.
     * - https://openweathermap.org/api
     */
    OPEN_WEATHER_MAP(
        // See limits at https://github.com/hossain-khan/android-weather-alert/tree/main/service#weather-services
        requiresApiKey = true,
    ),

    /**
     * Tomorrow.io API for weather forecast.
     * - https://app.tomorrow.io/home
     */
    TOMORROW_IO(
        // See limits at https://github.com/hossain-khan/android-weather-alert/tree/main/service#weather-services
        requiresApiKey = true,
    ),

    /**
     * Open-Meteo API for weather forecast.
     * - https://open-meteo.com/en/docs
     */
    OPEN_METEO(
        /**
         * ⛔️ Disabled to service as the forecast data was not reliable.
         * See https://github.com/hossain-khan/android-weather-alert/pull/164
         *
         * ⚠️ UPDATE: This service is enabled **ONLY** for debug builds.
         */
        isEnabled = BuildConfig.DEBUG,
    ),

    /**
     * WeatherAPI API for weather forecast. (Yes, very generic name for the product)
     * - https://www.weatherapi.com/docs/
     */
    WEATHER_API,
}
