package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.di.ApplicationContext
import dev.hossain.weatheralert.work.DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

/**
 * Manages user preferences using DataStore.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore">DataStore</a>
 */
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
                        WeatherService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
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
                            WeatherService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
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
                    WeatherService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
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
         * @see savePreferredWeatherService
         */
        val preferredWeatherService: Flow<WeatherService> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                        WeatherService.valueOf(it)
                    } ?: defaultWeatherService
                }

        /**
         * Retrieves the active weather service based on user preference in synchronous manner.
         */
        val preferredWeatherServiceSync: WeatherService =
            runBlocking {
                val weatherService =
                    dataStore.data
                        .map { preferences: Preferences ->
                            preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                                WeatherService.valueOf(it)
                            } ?: defaultWeatherService
                        }.first()
                Timber.d("Returning preferredWeatherServiceSync: $weatherService")
                return@runBlocking weatherService
            }

        /**
         * @see preferredWeatherService
         */
        suspend fun savePreferredWeatherService(service: WeatherService) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.preferredWeatherServiceKey] = service.name
            }
        }

        val preferredUpdateInterval: Flow<Long> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.preferredUpdateIntervalKey] ?: DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
                }

        val preferredUpdateIntervalSync: Long =
            runBlocking {
                val interval = preferredUpdateInterval.first()
                Timber.d("Returning preferredUpdateIntervalSync: $interval")
                return@runBlocking interval
            }

        suspend fun savePreferredUpdateInterval(interval: Long) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.preferredUpdateIntervalKey] = interval
            }
        }
    }
