package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension property to create a DataStore instance with the name "weather_alerts".
 */
val Context.dataStore by preferencesDataStore("weather_alerts")

/**
 * Object to hold user preference keys for the weather alert application.
 */
object UserPreferences {
    /**
     * Key for storing the snow threshold preference.
     */
    val snowThreshold = floatPreferencesKey("snow_threshold")

    /**
     * Key for storing the rain threshold preference.
     */
    val rainThreshold = floatPreferencesKey("rain_threshold")

    /**
     * Key for storing the configured user alerts as a JSON string.
     */
    val savedAlerts = stringPreferencesKey("configured_user_alerts")
}
