package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherService
import dev.hossain.weatheralert.work.DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [PreferencesManager].
 */
@RunWith(AndroidJUnit4::class)
class PreferencesManagerTest {
    private lateinit var preferencesManager: PreferencesManager

    private val testContext: Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        preferencesManager = PreferencesManager(testContext)
    }

    @Test
    fun saveUserApiKey_savesApiKeySuccessfully() =
        runTest {
            val apiKey = "test_api_key"
            preferencesManager.saveUserApiKey(WeatherService.OPEN_WEATHER_MAP, apiKey)
            val savedApiKey = preferencesManager.userApiKey(WeatherService.OPEN_WEATHER_MAP).first()
            assertThat(savedApiKey).isEqualTo(apiKey)
        }

    @Test
    fun savedApiKey_returnsNullWhenNoApiKeySaved() =
        runTest {
            val savedApiKey = preferencesManager.savedApiKey(WeatherService.OPEN_WEATHER_MAP)
            assertThat(savedApiKey).isNull()
        }

    @Test
    fun clearUserApiKeys_clearsAllApiKeys() =
        runTest {
            preferencesManager.saveUserApiKey(WeatherService.OPEN_WEATHER_MAP, "test_api_key")
            preferencesManager.saveUserApiKey(WeatherService.TOMORROW_IO, "test_api_key_2")
            preferencesManager.clearUserApiKeys()
            val savedApiKey1 = preferencesManager.savedApiKey(WeatherService.OPEN_WEATHER_MAP)
            val savedApiKey2 = preferencesManager.savedApiKey(WeatherService.TOMORROW_IO)
            assertThat(savedApiKey1).isNull()
            assertThat(savedApiKey2).isNull()
        }

    @Test
    fun removeApiKey_removesSpecificApiKey() =
        runTest {
            preferencesManager.saveUserApiKey(WeatherService.OPEN_WEATHER_MAP, "test_api_key")
            preferencesManager.removeApiKey(WeatherService.OPEN_WEATHER_MAP)
            val savedApiKey = preferencesManager.savedApiKey(WeatherService.OPEN_WEATHER_MAP)
            assertThat(savedApiKey).isNull()
        }

    @Test
    fun preferredWeatherService_returnsDefaultWhenNoPreferenceSet() =
        runTest {
            val preferredService = preferencesManager.preferredWeatherService.first()
            assertThat(preferredService).isEqualTo(WeatherService.OPEN_WEATHER_MAP)
        }

    @Test
    fun savePreferredWeatherService_savesPreferredServiceSuccessfully() =
        runTest {
            preferencesManager.savePreferredWeatherService(WeatherService.TOMORROW_IO)
            val preferredService = preferencesManager.preferredWeatherService.first()
            assertThat(preferredService).isEqualTo(WeatherService.TOMORROW_IO)
        }

    @Test
    fun preferredUpdateInterval_returnsDefaultWhenNoPreferenceSet() =
        runTest {
            testContext.dataStore.edit { it.clear() }
            val preferredInterval = preferencesManager.preferredUpdateInterval.first()
            assertThat(preferredInterval).isEqualTo(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS)
        }

    @Test
    fun savePreferredUpdateInterval_savesPreferredIntervalSuccessfully() =
        runTest {
            val interval = 6L
            preferencesManager.savePreferredUpdateInterval(interval)
            val preferredInterval = preferencesManager.preferredUpdateInterval.first()
            assertThat(preferredInterval).isEqualTo(interval)
        }
}
