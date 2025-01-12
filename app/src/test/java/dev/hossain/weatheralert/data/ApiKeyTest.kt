package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [ApiKeyImpl].
 */
@RunWith(RobolectricTestRunner::class)
class ApiKeyTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val apiKeyImpl = ApiKeyImpl(PreferencesManager(context))

    @Test
    fun isValidKey_returnsTrueForValidOpenWeatherMap_key1() {
        val validKey = "dccaeb06dd9f2cf5f8204df0f1428049"
        val isValid = apiKeyImpl.isValidKey(WeatherService.OPEN_WEATHER_MAP, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsTrueForValidOpenWeatherMap_key2() {
        val validKey = "78cba4e92d6a4c1fa73f9e14ba1abfae"
        val isValid = apiKeyImpl.isValidKey(WeatherService.OPEN_WEATHER_MAP, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsFalseForInvalidOpenWeatherMapKey() {
        val invalidKey = "invalidKey"
        val isValid = apiKeyImpl.isValidKey(WeatherService.OPEN_WEATHER_MAP, invalidKey)
        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidKey_returnsTrueForValidTomorrowIoKey_key1() {
        val validKey = "8G7QBldynlLCmfSOUDBbwKckajIKwfum"
        val isValid = apiKeyImpl.isValidKey(WeatherService.TOMORROW_IO, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsTrueForValidTomorrowIoKey_key2() {
        val validKey = "DB9QvPZYE0HqDhGga14J3AUnGDPzxW6b"
        val isValid = apiKeyImpl.isValidKey(WeatherService.TOMORROW_IO, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsFalseForInvalidTomorrowIoKey() {
        val invalidKey = "invalidKey"
        val isValid = apiKeyImpl.isValidKey(WeatherService.TOMORROW_IO, invalidKey)
        assertThat(isValid).isFalse()
    }
}
