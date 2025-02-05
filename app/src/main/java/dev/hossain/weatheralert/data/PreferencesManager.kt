package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.datamodel.WeatherForecastService
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
        private val defaultWeatherForecastService = WeatherForecastService.OPEN_WEATHER_MAP

        fun userApiKey(service: WeatherForecastService): Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    when (service) {
                        WeatherForecastService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                        WeatherForecastService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                        WeatherForecastService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
                    }
                }

        /**
         * Retrieves the saved API key from the user preferences in synchronous manner.
         */
        fun savedApiKey(service: WeatherForecastService): String? =
            runBlocking {
                dataStore.data
                    .map { preferences: Preferences ->
                        when (service) {
                            WeatherForecastService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                            WeatherForecastService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                            WeatherForecastService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
                        }
                    }.firstOrNull()
            }

        suspend fun saveUserApiKey(
            service: WeatherForecastService,
            apiKey: String,
        ) {
            dataStore.edit { preferences: MutablePreferences ->
                when (service) {
                    WeatherForecastService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey] = apiKey
                    WeatherForecastService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey] = apiKey
                    WeatherForecastService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
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
        suspend fun removeApiKey(service: WeatherForecastService) {
            when (service) {
                WeatherForecastService.OPEN_WEATHER_MAP ->
                    dataStore.edit { preferences: MutablePreferences ->
                        preferences.remove(UserPreferences.openWeatherServiceApiKey)
                    }
                WeatherForecastService.TOMORROW_IO ->
                    dataStore.edit { preferences: MutablePreferences ->
                        preferences.remove(UserPreferences.tomorrowIoServiceApiKey)
                    }
                WeatherForecastService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
            }
        }

        /**
         * Retrieves the active weather service based on user preference.
         * If user has not selected any service, it will return the default service.
         * @see defaultWeatherForecastService
         * @see savePreferredWeatherService
         */
        val preferredWeatherForecastService: Flow<WeatherForecastService> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                        WeatherForecastService.valueOf(it)
                    } ?: defaultWeatherForecastService
                }

        /**
         * Retrieves the active weather service based on user preference in synchronous manner.
         */
        val preferredWeatherForecastServiceSync: WeatherForecastService =
            runBlocking {
                val weatherForecastService =
                    dataStore.data
                        .map { preferences: Preferences ->
                            preferences[UserPreferences.preferredWeatherServiceKey]?.let {
                                WeatherForecastService.valueOf(it)
                            } ?: defaultWeatherForecastService
                        }.first()
                Timber.d("Returning preferredWeatherServiceSync: $weatherForecastService")
                return@runBlocking weatherForecastService
            }

        /**
         * @see preferredWeatherForecastService
         */
        suspend fun savePreferredWeatherService(service: WeatherForecastService) {
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
