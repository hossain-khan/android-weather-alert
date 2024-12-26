package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager(
    private val context: Context,
) {
    private val dataStore = context.dataStore

    val snowThreshold: Flow<Float> =
        dataStore.data.map { prefs ->
            prefs[UserPreferences.snowThreshold] ?: 5.0f // Default: 5 cm
        }

    val rainThreshold: Flow<Float> =
        dataStore.data.map { prefs ->
            prefs[UserPreferences.rainThreshold] ?: 10.0f // Default: 10 mm
        }

    suspend fun updateSnowThreshold(value: Float) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.snowThreshold] = value
        }
    }

    suspend fun updateRainThreshold(value: Float) {
        dataStore.edit { prefs ->
            prefs[UserPreferences.rainThreshold] = value
        }
    }
}
