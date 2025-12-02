package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [PreferencesManager] last weather check timestamp methods.
 */
@RunWith(RobolectricTestRunner::class)
class PreferencesManagerLastCheckTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val preferencesManager = PreferencesManager(context)

    @Before
    fun setUp() =
        runTest {
            // Clear last weather check time before each test
            context.dataStore.edit { preferences ->
                preferences.remove(UserPreferences.lastWeatherCheckTimeKey)
            }
        }

    @Test
    fun lastWeatherCheckTime_returnsZeroByDefault() =
        runTest {
            val timestamp = preferencesManager.lastWeatherCheckTime.first()
            assertThat(timestamp).isEqualTo(0L)
        }

    @Test
    fun saveLastWeatherCheckTime_savesTimestamp() =
        runTest {
            val testTimestamp = 1234567890L
            preferencesManager.saveLastWeatherCheckTime(testTimestamp)

            val savedTimestamp = preferencesManager.lastWeatherCheckTime.first()
            assertThat(savedTimestamp).isEqualTo(testTimestamp)
        }

    @Test
    fun saveLastWeatherCheckTime_updatesExistingTimestamp() =
        runTest {
            val firstTimestamp = 1111111111L
            val secondTimestamp = 2222222222L

            // First save
            preferencesManager.saveLastWeatherCheckTime(firstTimestamp)
            val firstSaved = preferencesManager.lastWeatherCheckTime.first()
            assertThat(firstSaved).isEqualTo(firstTimestamp)

            // Update with new timestamp
            preferencesManager.saveLastWeatherCheckTime(secondTimestamp)
            val secondSaved = preferencesManager.lastWeatherCheckTime.first()
            assertThat(secondSaved).isEqualTo(secondTimestamp)
        }

    @Test
    fun saveLastWeatherCheckTime_savesCurrentTime() =
        runTest {
            val currentTime = System.currentTimeMillis()
            preferencesManager.saveLastWeatherCheckTime(currentTime)

            val savedTimestamp = preferencesManager.lastWeatherCheckTime.first()
            // Check that the saved timestamp is close to the current time (within 1 second)
            assertThat(savedTimestamp).isAtLeast(currentTime - 1000)
            assertThat(savedTimestamp).isAtMost(currentTime + 1000)
        }
}
