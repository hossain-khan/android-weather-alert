package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.weatheralert.data.UserPreferences.savedAlerts
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
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

        private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        private val jsonAdapter: JsonAdapter<ConfiguredAlerts> = moshi.adapter(ConfiguredAlerts::class.java)

        val userConfiguredAlerts: Flow<ConfiguredAlerts> =
            dataStore.data.map { prefs ->
                val json = prefs[savedAlerts] ?: ""
                try {
                    if (json.isNotEmpty()) {
                        jsonAdapter.fromJson(json) ?: ConfiguredAlerts(emptyList())
                    } else {
                        ConfiguredAlerts(emptyList())
                    }
                } catch (e: Exception) {
                    // Log the exception and reset the value
                    Timber.e(e, "Failed to parse user configured alerts. Resetting to default value.")
                    updateUserConfiguredAlerts(ConfiguredAlerts(emptyList()))
                    ConfiguredAlerts(emptyList())
                }
            }

        suspend fun updateUserConfiguredAlerts(alerts: ConfiguredAlerts) {
            val json = jsonAdapter.toJson(alerts)
            dataStore.edit { prefs ->
                prefs[savedAlerts] = json
            }
        }
    }
