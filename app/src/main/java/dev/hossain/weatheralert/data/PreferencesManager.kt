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

        val userApiKey: Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.userApiKey]
                }

        /**
         * Retrieves the saved API key from the user preferences in synchronous manner.
         */
        val savedApiKey: String?
            get() =
                runBlocking {
                    dataStore.data
                        .map { preferences: Preferences ->
                            preferences[UserPreferences.userApiKey]
                        }.firstOrNull()
                }

        suspend fun saveUserApiKey(apiKey: String) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.userApiKey] = apiKey
            }
        }

        suspend fun clearUserApiKey() {
            dataStore.edit { preferences: MutablePreferences ->
                preferences.remove(UserPreferences.userApiKey)
            }
        }

        /**
         * Retrieves the active weather service based on user preference.
         * If user has not selected any service, it will return the default service.
         * @see defaultWeatherService
         * @see selectWeatherService
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
        suspend fun selectWeatherService(service: WeatherService) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.weatherServiceKey] = service.name
            }
        }
    }
