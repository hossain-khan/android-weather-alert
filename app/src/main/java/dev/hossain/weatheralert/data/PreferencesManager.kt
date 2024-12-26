package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore

        val snowThreshold: Flow<Float> =
            dataStore.data.map { prefs ->
                prefs[UserPreferences.snowThreshold] ?: DEFAULT_SNOW_THRESHOLD
            }

        val rainThreshold: Flow<Float> =
            dataStore.data.map { prefs ->
                prefs[UserPreferences.rainThreshold] ?: DEFAULT_RAIN_THRESHOLD
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
