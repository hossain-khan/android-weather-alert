package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.work.DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Manages user preferences using DataStore.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/datastore">DataStore</a>
 */
@Inject
class PreferencesManager
    constructor(
        private val context: Context,
    ) {
        private val dataStore = context.dataStore

        /**
         * This use default weather service provider with the highest API limit and good accuracy.
         * Made this default to reduce load on other services that does require API key with daily and monthly limit.
         *
         * See https://github.com/hossain-khan/android-weather-alert/tree/main/service#readme
         */
        private val defaultWeatherForecastService = WeatherForecastService.WEATHER_API

        fun userApiKey(service: WeatherForecastService): Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    when (service) {
                        WeatherForecastService.OPEN_WEATHER_MAP -> preferences[UserPreferences.openWeatherServiceApiKey]
                        WeatherForecastService.TOMORROW_IO -> preferences[UserPreferences.tomorrowIoServiceApiKey]
                        else -> throw IllegalStateException("No API key needed for $service")
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
                            else -> throw IllegalStateException("No API key needed for $service")
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
                    else -> throw IllegalStateException("No API key needed for $service")
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
                else -> throw IllegalStateException("No API key needed for $service")
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

        /**
         * Retrieves whether the user has completed the onboarding flow.
         *
         * @see setOnboardingCompleted
         */
        val isOnboardingCompleted: Flow<Boolean> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.onboardingCompletedKey] ?: false
                }

        /**
         * Retrieves whether the user has completed the onboarding flow in synchronous manner.
         */
        val isOnboardingCompletedSync: Boolean =
            runBlocking {
                val isCompleted = isOnboardingCompleted.first()
                Timber.d("Returning isOnboardingCompletedSync: $isCompleted")
                return@runBlocking isCompleted
            }

        /**
         * Sets the onboarding completion status.
         *
         * @see isOnboardingCompleted
         */
        suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.onboardingCompletedKey] = completed
            }
        }
    }
