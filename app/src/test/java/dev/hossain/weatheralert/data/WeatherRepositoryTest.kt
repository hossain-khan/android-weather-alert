package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.slack.eithernet.ApiResult
import dev.hossain.weatheralert.di.DaggerTestAppComponent
import dev.hossain.weatheralert.di.NetworkModule
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

/**
 * Tests [WeatherRepository] using [MockWebServer].
 */
@RunWith(RobolectricTestRunner::class)
class WeatherRepositoryTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var weatherRepository: WeatherRepository
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Inject
    lateinit var weatherApi: WeatherApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        NetworkModule.baseUrl = mockWebServer.url("/")

        val testAppComponent = DaggerTestAppComponent.factory().create(context)
        testAppComponent.inject(this)

        weatherRepository = WeatherRepositoryImpl(weatherApi)
    }

    @Test
    fun testGetDailyForecast() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        {
                          "lat": 43.9319,
                          "lon": -78.851,
                          "timezone": "America/Toronto",
                          "timezone_offset": -18000,
                          "daily": []
                        }
                        """.trimIndent(),
                    ),
            )

            val result =
                weatherRepository.getDailyForecast(
                    latitude = 46.8570237,
                    longitude = -71.5097202,
                    apiKey = "test_api_key",
                )
            assert(result is ApiResult.Success)
            val forecast = (result as ApiResult.Success).value
            assertEquals(0, forecast.daily.size)
        }
}
