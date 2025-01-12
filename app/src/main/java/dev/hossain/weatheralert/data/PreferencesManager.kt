package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.squareup.anvil.annotations.optional.SingleIn
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Manages user preferences using DataStore.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore">DataStore</a>
 */
@SingleIn(AppScope::class)
class PreferencesManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore
        private val defaultWeatherService = WeatherService.OPEN_WEATHER_MAP

        fun userApiKey(service: WeatherService): Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    when (service) {
                        WeatherService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                        WeatherService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                    }
                }

        /**
         * Retrieves the saved API key from the user preferences in synchronous manner.
         */
        fun savedApiKey(service: WeatherService): String? =
            runBlocking {
                dataStore.data
                    .map { preferences: Preferences ->
                        when (service) {
                            WeatherService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                            WeatherService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                        }
                    }.firstOrNull()
            }

        suspend fun saveUserApiKey(
            service: WeatherService,
            apiKey: String,
        ) {
            dataStore.edit { preferences: MutablePreferences ->
                when (service) {
                    WeatherService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey] = apiKey
                    WeatherService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey] = apiKey
                }
            }
        }

        suspend fun clearUserApiKeys() {
            dataStore.edit { preferences: MutablePreferences ->
                preferences.remove(UserPreferences.openWeatherServiceApiKey)
                preferences.remove(UserPreferences.tomorrowIoServiceApiKey)
            }
        }

        /**
         * Retrieves the active weather service based on user preference.
         * If user has not selected any service, it will return the default service.
         * @see defaultWeatherService
         * @see saveWeatherService
         */
        val activeWeatherService: Flow<WeatherService> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.weatherServiceKey]?.let {
                        WeatherService.valueOf(it)
                    } ?: defaultWeatherService
                }

        /**
         * Retrieves the active weather service based on user preference in synchronous manner.
         */
        val activeWeatherServiceSync: WeatherService =
            runBlocking {
                dataStore.data
                    .map { preferences: Preferences ->
                        preferences[UserPreferences.weatherServiceKey]?.let {
                            WeatherService.valueOf(it)
                        } ?: defaultWeatherService
                    }.first()
            }

        /**
         * @see activeWeatherService
         */
        suspend fun saveWeatherService(service: WeatherService) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.weatherServiceKey] = service.name
            }
        }
    }
