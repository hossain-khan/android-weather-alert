package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("weather_alerts")

object UserPreferences {
    val snowThreshold = floatPreferencesKey("snow_threshold")
    val rainThreshold = floatPreferencesKey("rain_threshold")
}
