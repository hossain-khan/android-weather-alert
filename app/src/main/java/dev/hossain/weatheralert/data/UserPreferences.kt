package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension property to create a DataStore instance with the name "weather_alerts".
 */
val Context.dataStore by preferencesDataStore("weather_alerts_prefs")

/**
 * Object to hold user preference keys for the weather alert application.
 */
object UserPreferences {
    /**
     * Key for storing user provided API key.
     */
    val userApiKey = stringPreferencesKey("user_byo_api_key")
}
