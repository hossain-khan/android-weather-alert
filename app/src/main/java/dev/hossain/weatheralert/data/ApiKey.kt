package dev.hossain.weatheralert.data

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject

/**
 * Interface representing an API key.
 */
interface ApiKey {
    /**
     * The API key as a string.
     */
    val key: String

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
}

/**
 * Implementation of the [ApiKey] interface.
 * This class provides the API key from the build configuration.
 */
@ContributesBinding(AppScope::class)
class ApiKeyImpl
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ApiKey {
        /**
         * Retrieves the API key from the build configuration.
         */
        override val key: String
            get() {
                val activeWeatherServiceSync = preferencesManager.preferredWeatherServiceSync
                return when (activeWeatherServiceSync) {
                    WeatherService.OPEN_WEATHER_MAP -> {
                        // Check if user has provided their own API key.
                        preferencesManager.savedApiKey(activeWeatherServiceSync) ?: BuildConfig.OPEN_WEATHER_API_KEY
                    }

                    WeatherService.TOMORROW_IO -> {
                        // Check if user has provided their own API key.
                        preferencesManager.savedApiKey(activeWeatherServiceSync) ?: BuildConfig.TOMORROW_IO_API_KEY
                    }
                }
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
            }
    }
