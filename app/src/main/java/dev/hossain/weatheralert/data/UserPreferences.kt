package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.hossain.weatheralert.work.supportedWeatherUpdateInterval

/**
 * Extension property to create a DataStore instance with the name "weather_alerts".
 * - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
 */
val Context.dataStore by preferencesDataStore("weather_alerts_prefs")

/**
 * Object to hold user preference keys for the weather alert application.
 */
object UserPreferences {
    /**
     * Preference key for OpenWeatherMap weather service API key provided by user.
     */
    val openWeatherServiceApiKey = stringPreferencesKey("open_weather_service_api_key")

    /**
     * Preference key for Tomorrow.io weather service API key provided by user.
     */
    val tomorrowIoServiceApiKey = stringPreferencesKey("tomorrow_io_weather_service_api_key")

    /**
     * Key for storing user preferred weather service provider.
     */
    val preferredWeatherServiceKey = stringPreferencesKey("user_preferred_weather_service_key")

    /**
     * User selected update interval for weather alerts.
     * Supported values are listed in [supportedWeatherUpdateInterval].
     */
    val preferredUpdateIntervalKey = longPreferencesKey("user_preferred_update_interval_key")
}
