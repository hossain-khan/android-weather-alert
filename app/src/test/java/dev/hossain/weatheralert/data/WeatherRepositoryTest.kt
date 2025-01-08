package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.slack.eithernet.ApiResult
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.di.DaggerTestAppComponent
import dev.hossain.weatheralert.di.NetworkModule
import dev.hossain.weatheralert.util.TimeUtil
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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

    @Inject
    lateinit var cityForecastDao: CityForecastDao

    @Inject
    lateinit var timeUtil: TimeUtil

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        NetworkModule.baseUrl = mockWebServer.url("/")

        val testAppComponent = DaggerTestAppComponent.factory().create(context)
        testAppComponent.inject(this)

        weatherRepository = WeatherRepositoryImpl(ApiKeyImpl(), weatherApi, cityForecastDao, timeUtil)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
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
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast = (result as ApiResult.Success).value
            assertThat(forecast.snow.dailyCumulativeSnow).isEqualTo(0.0)
        }

    @Test
    fun `given weather response for chicago - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-chicago.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(33.44)
            assertThat(forecast.longitude).isEqualTo(-94.04)
        }

    @Test
    fun `given weather response for cancun - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-cancun.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(21.1619)
            assertThat(forecast.longitude).isEqualTo(-86.8515)
        }

    @Test
    fun `given weather response for toronto - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-toronto-warning.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(43.7)
            assertThat(forecast.longitude).isEqualTo(-79.42)
        }

    @Test
    fun `given weather response for oshawa with hourly data - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-hourly-snow-oshawa.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(43.9319)
            assertThat(forecast.longitude).isEqualTo(-78.851)
        }

    @Test
    fun `given weather response for kansas - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-kansas-snowing.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(38.4685)
            assertThat(forecast.longitude).isEqualTo(-100.9596)
            assertThat(forecast.snow.dailyCumulativeSnow).isEqualTo(1.2499999999999998)
        }

    @Test
    fun `given weather response for St Louis Missouri - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-st-louis-missouri-heavy-snow.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(38.6289)
            assertThat(forecast.longitude).isEqualTo(-90.2546)
            assertThat(forecast.snow.dailyCumulativeSnow).isEqualTo(27.23)
        }

    @Test
    fun `given weather response for yazoo city mississippi - provides success response with parsed data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-yazoo-city-mississippi-raining.json")),
            )

            val result =
                weatherRepository.getDailyForecast(
                    cityId = 1,
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast: ForecastData = (result as ApiResult.Success).value
            assertThat(forecast.latitude).isEqualTo(32.864)
            assertThat(forecast.longitude).isEqualTo(-90.43)
            assertThat(forecast.rain.dailyCumulativeRain).isEqualTo(8.85)
        }

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
