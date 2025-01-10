package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

        val userApiKey: Flow<String?> =
            dataStore.data
                .map { preferences: Preferences ->
                    preferences[UserPreferences.userApiKey]
                }

        suspend fun saveUserApiKey(apiKey: String) {
            dataStore.edit { preferences: MutablePreferences ->
                preferences[UserPreferences.userApiKey] = apiKey
            }
        }
    }
