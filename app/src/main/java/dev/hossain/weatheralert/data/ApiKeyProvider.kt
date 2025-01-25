package dev.hossain.weatheralert.data

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.datamodel.WeatherService
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject

/**
 * Interface representing an API key.
 */
interface ApiKeyProvider {
    /**
     * The API key as a string for active API service (user preferred service).
     */
    val activeServiceApiKey: String

    /**
     * Unlike [activeServiceApiKey], this method provides the API key for the given weather service.
     */
    fun apiKey(weatherService: WeatherService): String

    /**
     * Checks if the provided API key is valid for the given weather service.
     *
     * @param weatherService The weather service for which the API key is being validated.
     * @param apiKey The API key to validate.
     * @return `true` if the API key is valid, `false` otherwise.
     */
    fun isValidKey(
        weatherService: WeatherService,
        apiKey: String,
    ): Boolean

    /**
     * Checks if the API key for selected service is provided by user or not.
     */
    fun hasUserProvidedApiKey(weatherApiService: WeatherService): Boolean
}

/**
 * Implementation of the [ApiKeyProvider] interface.
 * This class provides the API key from the build configuration.
 */
@ContributesBinding(AppScope::class)
class ApiKeyProviderImpl
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ApiKeyProvider {
        /**
         * Retrieves the API key from the build configuration.
         */
        override val activeServiceApiKey: String
            get() {
                val activeWeatherServiceSync = preferencesManager.preferredWeatherServiceSync
                return apiKey(activeWeatherServiceSync)
            }

        override fun apiKey(weatherService: WeatherService): String =
            when (weatherService) {
                WeatherService.OPEN_WEATHER_MAP -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(weatherService) ?: BuildConfig.OPEN_WEATHER_API_KEY
                }

                WeatherService.TOMORROW_IO -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(weatherService) ?: BuildConfig.TOMORROW_IO_API_KEY
                }

                WeatherService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
            }

        override fun isValidKey(
            weatherService: WeatherService,
            apiKey: String,
        ): Boolean =
            when (weatherService) {
                WeatherService.OPEN_WEATHER_MAP -> {
                    apiKey.matches(Regex("^[a-f0-9]{32}\$"))
                }
                WeatherService.TOMORROW_IO -> {
                    apiKey.matches(Regex("^[A-Za-z0-9]{32}$"))
                }

                WeatherService.OPEN_METEO -> true
            }

        override fun hasUserProvidedApiKey(weatherApiService: WeatherService): Boolean {
            val apiKey = apiKey(weatherApiService)
            return when (weatherApiService) {
                WeatherService.OPEN_WEATHER_MAP -> apiKey.isNotEmpty() && apiKey != BuildConfig.OPEN_WEATHER_API_KEY
                WeatherService.TOMORROW_IO -> apiKey.isNotEmpty() && apiKey != BuildConfig.TOMORROW_IO_API_KEY
                WeatherService.OPEN_METEO -> false
            }
        }
    }
