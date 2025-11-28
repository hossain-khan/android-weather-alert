package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [ActiveWeatherServiceImpl].
 *
 * Note: ActiveWeatherService uses the synchronous preferredWeatherForecastServiceSync property
 * which is evaluated at construction time. To test changes to the preference, a new
 * ActiveWeatherServiceImpl must be created after the preference is saved.
 */
@RunWith(RobolectricTestRunner::class)
class ActiveWeatherServiceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        preferencesManager = PreferencesManager(context)
    }

    @Test
    fun selectedService_returnsOpenWeatherMapWhenSetBeforeConstruction() =
        runTest {
            // First save the preference
            preferencesManager.savePreferredWeatherService(WeatherForecastService.OPEN_WEATHER_MAP)

            // Then create ActiveWeatherService which will read the preference at construction time
            val activeWeatherService = ActiveWeatherServiceImpl(PreferencesManager(context))

            val result = activeWeatherService.selectedService()

            assertThat(result).isEqualTo(WeatherForecastService.OPEN_WEATHER_MAP)
        }

    @Test
    fun selectedService_returnsTomorrowIoWhenSetBeforeConstruction() =
        runTest {
            preferencesManager.savePreferredWeatherService(WeatherForecastService.TOMORROW_IO)

            val activeWeatherService = ActiveWeatherServiceImpl(PreferencesManager(context))

            val result = activeWeatherService.selectedService()

            assertThat(result).isEqualTo(WeatherForecastService.TOMORROW_IO)
        }

    @Test
    fun selectedService_returnsOpenMeteoWhenSetBeforeConstruction() =
        runTest {
            preferencesManager.savePreferredWeatherService(WeatherForecastService.OPEN_METEO)

            val activeWeatherService = ActiveWeatherServiceImpl(PreferencesManager(context))

            val result = activeWeatherService.selectedService()

            assertThat(result).isEqualTo(WeatherForecastService.OPEN_METEO)
        }

    @Test
    fun selectedService_returnsWeatherApiWhenSetBeforeConstruction() =
        runTest {
            preferencesManager.savePreferredWeatherService(WeatherForecastService.WEATHER_API)

            val activeWeatherService = ActiveWeatherServiceImpl(PreferencesManager(context))

            val result = activeWeatherService.selectedService()

            assertThat(result).isEqualTo(WeatherForecastService.WEATHER_API)
        }

    @Test
    fun selectedService_returnsDefaultWeatherApiServiceWhenNoPreferenceSet() =
        runTest {
            // Clear any existing preference by creating fresh PreferencesManager context
            // The default is WEATHER_API as defined in PreferencesManager
            val activeWeatherService = ActiveWeatherServiceImpl(PreferencesManager(context))

            // When no preference is set (or default), should return WEATHER_API
            val result = activeWeatherService.selectedService()

            assertThat(result).isEqualTo(WeatherForecastService.WEATHER_API)
        }

    @Test
    fun preferredWeatherForecastService_flowReflectsChangesAfterSave() =
        runTest {
            // Initially should be the default
            val initial = preferencesManager.preferredWeatherForecastService.first()
            assertThat(initial).isEqualTo(WeatherForecastService.WEATHER_API)

            // Save a new preference
            preferencesManager.savePreferredWeatherService(WeatherForecastService.OPEN_WEATHER_MAP)

            // The flow should reflect the new value
            val updated = preferencesManager.preferredWeatherForecastService.first()
            assertThat(updated).isEqualTo(WeatherForecastService.OPEN_WEATHER_MAP)
        }
}
