package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.work.DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
import dev.hossain.weatheralert.work.WEATHER_UPDATE_INTERVAL_12_HOURS
import dev.hossain.weatheralert.work.WEATHER_UPDATE_INTERVAL_18_HOURS
import dev.hossain.weatheralert.work.WEATHER_UPDATE_INTERVAL_6_HOURS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [PreferencesManager] weather update interval methods.
 */
@RunWith(RobolectricTestRunner::class)
class PreferencesManagerUpdateIntervalTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val preferencesManager = PreferencesManager(context)

    @Before
    fun setUp() =
        runTest {
            // Clear update interval preference before each test
            context.dataStore.edit { preferences ->
                preferences.remove(UserPreferences.preferredUpdateIntervalKey)
            }
        }

    @Test
    fun preferredUpdateInterval_returnsDefaultIntervalWhenNotSet() =
        runTest {
            val interval = preferencesManager.preferredUpdateInterval.first()
            assertThat(interval).isEqualTo(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS)
        }

    @Test
    fun savePreferredUpdateInterval_saves6HourInterval() =
        runTest {
            preferencesManager.savePreferredUpdateInterval(WEATHER_UPDATE_INTERVAL_6_HOURS)

            val saved = preferencesManager.preferredUpdateInterval.first()
            assertThat(saved).isEqualTo(WEATHER_UPDATE_INTERVAL_6_HOURS)
        }

    @Test
    fun savePreferredUpdateInterval_saves12HourInterval() =
        runTest {
            preferencesManager.savePreferredUpdateInterval(WEATHER_UPDATE_INTERVAL_12_HOURS)

            val saved = preferencesManager.preferredUpdateInterval.first()
            assertThat(saved).isEqualTo(WEATHER_UPDATE_INTERVAL_12_HOURS)
        }

    @Test
    fun savePreferredUpdateInterval_saves18HourInterval() =
        runTest {
            preferencesManager.savePreferredUpdateInterval(WEATHER_UPDATE_INTERVAL_18_HOURS)

            val saved = preferencesManager.preferredUpdateInterval.first()
            assertThat(saved).isEqualTo(WEATHER_UPDATE_INTERVAL_18_HOURS)
        }

    @Test
    fun savePreferredUpdateInterval_updatesExistingInterval() =
        runTest {
            // First set to 6 hours
            preferencesManager.savePreferredUpdateInterval(WEATHER_UPDATE_INTERVAL_6_HOURS)
            assertThat(preferencesManager.preferredUpdateInterval.first()).isEqualTo(WEATHER_UPDATE_INTERVAL_6_HOURS)

            // Update to 18 hours
            preferencesManager.savePreferredUpdateInterval(WEATHER_UPDATE_INTERVAL_18_HOURS)
            assertThat(preferencesManager.preferredUpdateInterval.first()).isEqualTo(WEATHER_UPDATE_INTERVAL_18_HOURS)
        }

    @Test
    fun defaultWeatherUpdateInterval_is12Hours() {
        assertThat(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS).isEqualTo(12L)
    }
}
