package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/*
Problem:

    You're using @ContributesBinding to bind PreferencesManager to PreferencesManager. This means you are telling Dagger/Anvil: "Whenever I need a PreferencesManager, use this PreferencesManager." This is redundant and incorrect.
    @ContributesBinding is designed to bind an implementation to an interface (or a superclass, though less common).
 */
//@ContributesBinding(AppScope::class, boundType = PreferencesManager::class)
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
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
