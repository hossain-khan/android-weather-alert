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
}

/**
 * Implementation of the [ApiKey] interface.
 * This class provides the API key from the build configuration.
 */
@ContributesBinding(AppScope::class)
class ApiKeyImpl
    @Inject
    constructor() : ApiKey {
        /**
         * Retrieves the API key from the build configuration.
         */
        override val key: String
            get() = BuildConfig.WEATHER_API_KEY
    }
