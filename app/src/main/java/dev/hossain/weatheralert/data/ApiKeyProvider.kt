package dev.hossain.weatheralert.data

import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import timber.log.Timber

/**
 * Provides API key for the supported weather services.
 * @see WeatherForecastService
 */
interface ApiKeyProvider {
    /**
     * Provides the API key for the given weather service from user-preference or build config.
     */
    fun apiKey(weatherForecastService: WeatherForecastService): String

    /**
     * Checks if the provided API key is valid for the given weather service.
     *
     * @param weatherForecastService The weather service for which the API key is being validated.
     * @param apiKey The API key to validate.
     * @return `true` if the API key is valid, `false` otherwise.
     */
    fun isValidKey(
        weatherForecastService: WeatherForecastService,
        apiKey: String,
    ): Boolean

    /**
     * Checks if the API key for selected service is provided by user or not.
     */
    fun hasUserProvidedApiKey(weatherApiService: WeatherForecastService): Boolean
}

/**
 * Implementation of the [ApiKeyProvider] interface.
 * This class provides the API key from the build configuration.
 */
@ContributesBinding(AppScope::class)
@Inject
class ApiKeyProviderImpl
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ApiKeyProvider {
        override fun apiKey(weatherForecastService: WeatherForecastService): String =
            when (weatherForecastService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(weatherForecastService) ?: BuildConfig.OPEN_WEATHER_API_KEY
                }

                WeatherForecastService.TOMORROW_IO -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(weatherForecastService) ?: BuildConfig.TOMORROW_IO_API_KEY
                }

                WeatherForecastService.OPEN_METEO -> {
                    Timber.w("No API key required for $weatherForecastService")
                    ""
                }

                WeatherForecastService.WEATHER_API -> {
                    Timber.w("No API key required for $weatherForecastService")
                    ""
                }
            }

        override fun isValidKey(
            weatherForecastService: WeatherForecastService,
            apiKey: String,
        ): Boolean =
            when (weatherForecastService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> {
                    apiKey.matches(Regex("^[a-f0-9]{32}\$"))
                }

                WeatherForecastService.TOMORROW_IO -> {
                    apiKey.matches(Regex("^[A-Za-z0-9]{32}$"))
                }

                WeatherForecastService.OPEN_METEO -> {
                    Timber.w("No API key required for $weatherForecastService")
                    true
                }

                WeatherForecastService.WEATHER_API -> {
                    Timber.w("No API key required for $weatherForecastService")
                    true
                }
            }

        override fun hasUserProvidedApiKey(weatherApiService: WeatherForecastService): Boolean {
            val apiKey = apiKey(weatherApiService)
            return when (weatherApiService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> apiKey.isNotEmpty() && apiKey != BuildConfig.OPEN_WEATHER_API_KEY
                WeatherForecastService.TOMORROW_IO -> apiKey.isNotEmpty() && apiKey != BuildConfig.TOMORROW_IO_API_KEY
                WeatherForecastService.OPEN_METEO -> false
                WeatherForecastService.WEATHER_API -> false
            }
        }
    }
