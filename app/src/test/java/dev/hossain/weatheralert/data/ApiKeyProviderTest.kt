package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [ApiKeyProviderImpl].
 */
@RunWith(RobolectricTestRunner::class)
class ApiKeyProviderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val preferencesManager = PreferencesManager(context)
    private val apiKeyImpl = ApiKeyProviderImpl(preferencesManager)

    // OpenWeatherMap key validation tests
    @Test
    fun isValidKey_returnsTrueForValid32CharHexOpenWeatherMapKey() {
        val validKey = "dccaeb06dd9f2cf5f8204df0f1428049"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_WEATHER_MAP, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsFalseForInvalidOpenWeatherMapKey() {
        val invalidKey = "invalidKey"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_WEATHER_MAP, invalidKey)
        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidKey_returnsFalseForOpenWeatherMapKeyWithUppercase() {
        val invalidKey = "DCCAEB06DD9F2CF5F8204DF0F1428049"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_WEATHER_MAP, invalidKey)
        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidKey_returnsFalseForOpenWeatherMapKeyWithWrongLength() {
        val invalidKey = "dccaeb06dd9f2cf5f8204df0f142804" // 31 chars
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_WEATHER_MAP, invalidKey)
        assertThat(isValid).isFalse()
    }

    // Tomorrow.io key validation tests
    @Test
    fun isValidKey_returnsTrueForValid32CharAlphanumericTomorrowIoKey() {
        val validKey = "8G7QBldynlLCmfSOUDBbwKckajIKwfum"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.TOMORROW_IO, validKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsFalseForInvalidTomorrowIoKey() {
        val invalidKey = "invalidKey"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.TOMORROW_IO, invalidKey)
        assertThat(isValid).isFalse()
    }

    @Test
    fun isValidKey_returnsFalseForTomorrowIoKeyWithSpecialChars() {
        val invalidKey = "8G7QBldynlLCmfSOUDBbwKckajIK!@#$"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.TOMORROW_IO, invalidKey)
        assertThat(isValid).isFalse()
    }

    // OpenMeteo key validation tests (no API key required)
    @Test
    fun isValidKey_returnsTrueForOpenMeteoWithAnyKey() {
        val anyKey = "anyKey"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_METEO, anyKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsTrueForOpenMeteoWithEmptyKey() {
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.OPEN_METEO, "")
        assertThat(isValid).isTrue()
    }

    // WeatherAPI key validation tests (no API key required)
    @Test
    fun isValidKey_returnsTrueForWeatherApiWithAnyKey() {
        val anyKey = "anyKey"
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.WEATHER_API, anyKey)
        assertThat(isValid).isTrue()
    }

    @Test
    fun isValidKey_returnsTrueForWeatherApiWithEmptyKey() {
        val isValid = apiKeyImpl.isValidKey(WeatherForecastService.WEATHER_API, "")
        assertThat(isValid).isTrue()
    }

    // hasUserProvidedApiKey tests
    @Test
    fun hasUserProvidedApiKey_returnsTrueWhenUserProvidedOpenWeatherMapKey() =
        runTest {
            preferencesManager.saveUserApiKey(WeatherForecastService.OPEN_WEATHER_MAP, "userProvidedKey")

            val hasUserProvidedKey = apiKeyImpl.hasUserProvidedApiKey(WeatherForecastService.OPEN_WEATHER_MAP)
            assertThat(hasUserProvidedKey).isTrue()
        }

    @Test
    fun hasUserProvidedApiKey_returnsFalseWhenUsingEmptyOpenWeatherMapKey() =
        runTest {
            preferencesManager.saveUserApiKey(WeatherForecastService.OPEN_WEATHER_MAP, "")

            val hasUserProvidedKey = apiKeyImpl.hasUserProvidedApiKey(WeatherForecastService.OPEN_WEATHER_MAP)
            assertThat(hasUserProvidedKey).isFalse()
        }

    @Test
    fun hasUserProvidedApiKey_returnsTrueWhenUserProvidedTomorrowIoKey() =
        runTest {
            preferencesManager.saveUserApiKey(WeatherForecastService.TOMORROW_IO, "userProvidedTomorrowKey")

            val hasUserProvidedKey = apiKeyImpl.hasUserProvidedApiKey(WeatherForecastService.TOMORROW_IO)
            assertThat(hasUserProvidedKey).isTrue()
        }

    @Test
    fun hasUserProvidedApiKey_returnsFalseForOpenMeteo() =
        runTest {
            val hasUserProvidedKey = apiKeyImpl.hasUserProvidedApiKey(WeatherForecastService.OPEN_METEO)
            assertThat(hasUserProvidedKey).isFalse()
        }

    @Test
    fun hasUserProvidedApiKey_returnsFalseForWeatherApi() =
        runTest {
            val hasUserProvidedKey = apiKeyImpl.hasUserProvidedApiKey(WeatherForecastService.WEATHER_API)
            assertThat(hasUserProvidedKey).isFalse()
        }

    // apiKey tests
    @Test
    fun apiKey_returnsEmptyStringForOpenMeteo() =
        runTest {
            val apiKey = apiKeyImpl.apiKey(WeatherForecastService.OPEN_METEO)
            assertThat(apiKey).isEmpty()
        }

    @Test
    fun apiKey_returnsEmptyStringForWeatherApi() =
        runTest {
            val apiKey = apiKeyImpl.apiKey(WeatherForecastService.WEATHER_API)
            assertThat(apiKey).isEmpty()
        }
}
