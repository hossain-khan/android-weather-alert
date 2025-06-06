package dev.hossain.weatheralert.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherForecastService

/**
 * Internal config to show logo with right sizing and description for each weather service.
 *
 * DEVELOPER NOTE: This data essentially could be merged with [WeatherForecastService] enum, but keeping it separate for now.
 */
data class WeatherServiceConfig(
    /**
     * Can be used to display name of the service.
     */
    val serviceName: String,
    @DrawableRes val logoResId: Int,
    val logoWidth: Dp,
    val logoHeight: Dp,
    val description: String,
    /**
     * URL to visit to get new API.
     */
    val apiServiceUrl: String,
    /**
     * Label for the URL that use used in annotated string.
     */
    val apiServiceUrlLabel: String,
    val apiExhaustedMessage: String,
    /**
     * Guide text for the API format.
     */
    val apiFormatGuide: String,
    /**
     * Optional API service name that is required for the API key.
     */
    val apiServiceProductName: String,
)

internal fun WeatherForecastService.serviceConfig(): WeatherServiceConfig =
    when (this) {
        WeatherForecastService.OPEN_WEATHER_MAP ->
            WeatherServiceConfig(
                serviceName = "OpenWeather",
                logoResId = R.drawable.openweather_logo,
                logoWidth = 100.dp,
                logoHeight = 50.dp,
                description =
                    "Free API service with larger usage limits. " +
                        "However, it requires a credit card to activate the free API subscription.",
                apiServiceUrl = "https://openweathermap.org/api",
                apiServiceUrlLabel = "openweathermap.org",
                apiExhaustedMessage =
                    "Unfortunately, the API key provided with the app has been exhausted.\n\n" +
                        "To continue to use this app, you need to provide your own API key from OpenWeatherMap.",
                apiFormatGuide = "API key should be 32 characters long and contain only hexadecimal characters.",
                apiServiceProductName = "One Call API 3.0",
            )
        WeatherForecastService.TOMORROW_IO ->
            WeatherServiceConfig(
                serviceName = "Tomorrow.io",
                logoResId = R.drawable.tomorrow_io_logo,
                logoWidth = 120.dp,
                logoHeight = 30.dp,
                description = "Free API service with accurate data but limited usage limits. No credit card required.",
                apiServiceUrl = "https://www.tomorrow.io/weather-api/",
                apiServiceUrlLabel = "tomorrow.io",
                apiExhaustedMessage =
                    "Unfortunately, the API key provided with the app has been exhausted.\n\n" +
                        "To continue to use this app, you need to provide your own API key from Tomorrow.io.",
                apiFormatGuide = "API key should be 32 characters long and contain only letters and numbers.",
                apiServiceProductName = "Weather API",
            )

        WeatherForecastService.OPEN_METEO ->
            WeatherServiceConfig(
                serviceName = "Open-Meteo",
                logoResId = R.drawable.open_mateo_logo,
                logoWidth = 140.dp,
                logoHeight = 30.dp,
                description = "Free API service with high limit. No service API key is required.",
                apiServiceUrl = "https://open-meteo.com/en/docs",
                apiServiceUrlLabel = "open-meteo.com",
                apiExhaustedMessage = "Not applicable for Open-Meteo API.",
                apiFormatGuide = "Not applicable.",
                apiServiceProductName = "Weather API",
            )

        WeatherForecastService.WEATHER_API ->
            WeatherServiceConfig(
                serviceName = "WeatherAPI",
                logoResId = R.drawable.weatherapi_logo,
                // Original: 150x70, Medium: 107x50, Small: 86x40
                logoWidth = 86.dp,
                logoHeight = 40.dp,
                description = "Free API service with high limit.",
                apiServiceUrl = "https://www.weatherapi.com/docs/",
                apiServiceUrlLabel = "weatherapi.com",
                apiExhaustedMessage = "Not applicable for WeatherAPI.",
                apiFormatGuide = "Not applicable.",
                apiServiceProductName = "WeatherAPI",
            )
    }
