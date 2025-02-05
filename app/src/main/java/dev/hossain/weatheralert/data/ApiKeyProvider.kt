package dev.hossain.weatheralert.data

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.datamodel.ForecastServiceSource
import dev.hossain.weatheralert.di.AppScope
import timber.log.Timber
import javax.inject.Inject

/**
 * Provides API key for the supported weather services.
 * @see ForecastServiceSource
 */
interface ApiKeyProvider {
    /**
     * The API key as a string for active API service (user preferred service).
     */
    val activeServiceApiKey: String

    /**
     * Unlike [activeServiceApiKey], this method provides the API key for the given weather service.
     */
    fun apiKey(forecastServiceSource: ForecastServiceSource): String

    /**
     * Checks if the provided API key is valid for the given weather service.
     *
     * @param forecastServiceSource The weather service for which the API key is being validated.
     * @param apiKey The API key to validate.
     * @return `true` if the API key is valid, `false` otherwise.
     */
    fun isValidKey(
        forecastServiceSource: ForecastServiceSource,
        apiKey: String,
    ): Boolean

    /**
     * Checks if the API key for selected service is provided by user or not.
     */
    fun hasUserProvidedApiKey(weatherApiService: ForecastServiceSource): Boolean
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
                val activeWeatherServiceSync = preferencesManager.preferredForecastServiceSourceSync
                return apiKey(activeWeatherServiceSync)
            }

        override fun apiKey(forecastServiceSource: ForecastServiceSource): String =
            when (forecastServiceSource) {
                ForecastServiceSource.OPEN_WEATHER_MAP -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(forecastServiceSource) ?: BuildConfig.OPEN_WEATHER_API_KEY
                }

                ForecastServiceSource.TOMORROW_IO -> {
                    // Check if user has provided their own API key.
                    preferencesManager.savedApiKey(forecastServiceSource) ?: BuildConfig.TOMORROW_IO_API_KEY
                }

                ForecastServiceSource.OPEN_METEO -> {
                    Timber.w("No API key required for Open-Meteo")
                    ""
                }
            }

        override fun isValidKey(
            forecastServiceSource: ForecastServiceSource,
            apiKey: String,
        ): Boolean =
            when (forecastServiceSource) {
                ForecastServiceSource.OPEN_WEATHER_MAP -> {
                    apiKey.matches(Regex("^[a-f0-9]{32}\$"))
                }
                ForecastServiceSource.TOMORROW_IO -> {
                    apiKey.matches(Regex("^[A-Za-z0-9]{32}$"))
                }

                ForecastServiceSource.OPEN_METEO -> {
                    Timber.w("No API key required for Open-Meteo")
                    true
                }
            }

        override fun hasUserProvidedApiKey(weatherApiService: ForecastServiceSource): Boolean {
            val apiKey = apiKey(weatherApiService)
            return when (weatherApiService) {
                ForecastServiceSource.OPEN_WEATHER_MAP -> apiKey.isNotEmpty() && apiKey != BuildConfig.OPEN_WEATHER_API_KEY
                ForecastServiceSource.TOMORROW_IO -> apiKey.isNotEmpty() && apiKey != BuildConfig.TOMORROW_IO_API_KEY
                ForecastServiceSource.OPEN_METEO -> false
            }
        }
    }
