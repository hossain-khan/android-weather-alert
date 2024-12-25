package dev.hossain.weatheralert.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.model.AlertConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class AlertConfigDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val TAG = "AlertConfigDataStore"
        private val SNOW_THRESHOLD_KEY = doublePreferencesKey("snow_threshold")
        private val RAIN_THRESHOLD_KEY = doublePreferencesKey("rain_threshold")
    }

    fun getAlertConfigs(): Flow<List<AlertConfig>> {
        return dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences.", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val snowThreshold = preferences[SNOW_THRESHOLD_KEY] ?: 0.0
                val rainThreshold = preferences[RAIN_THRESHOLD_KEY] ?: 0.0

                listOfNotNull(
                    if (snowThreshold > 0) AlertConfig(
                        AlertCategory.Snow,
                        snowThreshold
                    ) else null,
                    if (rainThreshold > 0) AlertConfig(AlertCategory.Rain, rainThreshold) else null
                )
            }
    }

    suspend fun saveAlertConfig(alertConfig: AlertConfig) {
        dataStore.edit { preferences ->
            when (alertConfig.category) {
                is AlertCategory.Snow -> preferences[SNOW_THRESHOLD_KEY] = alertConfig.threshold
                is AlertCategory.Rain -> preferences[RAIN_THRESHOLD_KEY] = alertConfig.threshold
            }
        }
    }

    suspend fun deleteAlertConfig(category: AlertCategory) {
        dataStore.edit { preferences ->
            when (category) {
                is AlertCategory.Snow -> preferences.remove(SNOW_THRESHOLD_KEY)
                is AlertCategory.Rain -> preferences.remove(RAIN_THRESHOLD_KEY)
            }
        }
    }
}