package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.datamodel.ForecastServiceSource
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
        private val defaultForecastServiceSource = ForecastServiceSource.OPEN_WEATHER_MAP

        fun userApiKey(service: ForecastServiceSource): Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    when (service) {
                        ForecastServiceSource.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                        ForecastServiceSource.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                        ForecastServiceSource.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
                    }
                }

        /**
         * Retrieves the saved API key from the user preferences in synchronous manner.
         */
        fun savedApiKey(service: ForecastServiceSource): String? =
            runBlocking {
                dataStore.data
                    .map { preferences: Preferences ->
                        when (service) {
                            ForecastServiceSource.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                            ForecastServiceSource.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                            ForecastServiceSource.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
                        }
                    }.firstOrNull()
            }

        suspend fun saveUserApiKey(
            service: ForecastServiceSource,
            apiKey: String,
        ) {
            dataStore.edit { preferences: MutablePreferences ->
                when (service) {
                    ForecastServiceSource.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey] = apiKey
                    ForecastServiceSource.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey] = apiKey
                    ForecastServiceSource.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
                }
            }
        }

        /**
         * @see removeApiKey
         */
        suspend fun clearUserApiKeys() {
            dataStore.edit { preferences: MutablePreferences ->
                preferences.remove(UserPreferences.openWeatherServiceApiKey)
                preferences.remove(UserPreferences.tomorrowIoServiceApiKey)
            }
        }

        /**
         * @see clearUserApiKeys
         */
        suspend fun removeApiKey(service: ForecastServiceSource) {
            when (service) {
                ForecastServiceSource.OPEN_WEATHER_MAP ->
                    dataStore.edit { preferences: MutablePreferences ->
                        preferences.remove(UserPreferences.openWeatherServiceApiKey)
                    }
                ForecastServiceSource.TOMORROW_IO ->
                    dataStore.edit { preferences: MutablePreferences ->
                        preferences.remove(UserPreferences.tomorrowIoServiceApiKey)
                    }
                ForecastServiceSource.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
            }
        }

        /**
         * Retrieves the active weather service based on user preference.
         * If user has not selected any service, it will return the default service.
         * @see defaultForecastServiceSource
         * @see savePreferredWeatherService
         */
        val preferredForecastServiceSource: Flow<ForecastServiceSource> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                        ForecastServiceSource.valueOf(it)
                    } ?: defaultForecastServiceSource
                }

        /**
         * Retrieves the active weather service based on user preference in synchronous manner.
         */
        val preferredForecastServiceSourceSync: ForecastServiceSource =
            runBlocking {
                val forecastServiceSource =
                    dataStore.data
                        .map { preferences: Preferences ->
                            preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                                ForecastServiceSource.valueOf(it)
                            } ?: defaultForecastServiceSource
                        }.first()
                Timber.d("Returning preferredWeatherServiceSync: $forecastServiceSource")
                return@runBlocking forecastServiceSource
            }

        /**
         * @see preferredForecastServiceSource
         */
        suspend fun savePreferredWeatherService(service: ForecastServiceSource) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.preferredWeatherServiceKey] = service.name
            }
        }

        /**
         * Retrieves the user selected update interval for weather alerts.
         *
         * @see preferredUpdateIntervalSync
         * @see savePreferredUpdateInterval
         */
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

        /**
         * @see preferredUpdateInterval
         */
        suspend fun savePreferredUpdateInterval(interval: Long) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.preferredUpdateIntervalKey] = interval
            }
        }
    }
