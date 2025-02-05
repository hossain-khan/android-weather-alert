package org.openweathermap.api

import com.google.common.truth.Truth.assertThat
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openweathermap.api.model.WeatherForecast
import org.robolectric.RobolectricTestRunner

/**
 * Tests [OpenWeatherService] using [MockWebServer].
 */
@RunWith(RobolectricTestRunner::class)
class OpenForecastServiceSourceTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var tomorrowIoService: OpenWeatherService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tomorrowIoService =
            OpenWeatherServiceBuilder
                .provideService(baseUrl = mockWebServer.url("/"))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given weather forecast response for oshawa city - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-hourly-snow-oshawa.json")),
            )

            val result =
                tomorrowIoService.getDailyForecast(
                    apiKey = "fake-api-key",
                    latitude = 43.9319,
                    longitude = -78.851,
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherForecast = (result as ApiResult.Success).value
            assertThat(forecast.lat).isEqualTo(43.9319)
            assertThat(forecast.lon).isEqualTo(-78.851)
        }

    @Test
    fun `given weather forecast response for yazoo city - parses data properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-yazoo-city-mississippi-raining.json")),
            )

            val result =
                tomorrowIoService.getDailyForecast(
                    apiKey = "fake-api-key",
                    latitude = 32.864,
                    longitude = -90.43,
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherForecast = (result as ApiResult.Success).value
            assertThat(forecast.lat).isEqualTo(32.864)
            assertThat(forecast.lon).isEqualTo(-90.43)
        }

    @Test
    fun `given weather forecast response for lac mann - parses data properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("open-weather-lac-mann-snowing-2025-01-20.json")),
            )

            val result =
                tomorrowIoService.getDailyForecast(
                    apiKey = "fake-api-key",
                    latitude = 49.588,
                    longitude = -75.1699,
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherForecast = (result as ApiResult.Success).value
            assertThat(forecast.lat).isEqualTo(49.588)
            assertThat(forecast.lon).isEqualTo(-75.1699)
            assertThat(forecast.daily.size).isEqualTo(8)
            assertThat(forecast.hourly.size).isEqualTo(48)

            // Verify total hourly snowfall
            val totalHourlySnowfall = forecast.hourly.sumOf { it.snow?.snowVolumeInAnHour ?: 0.0 }
            assertThat(totalHourlySnowfall).isEqualTo(0.0)

            // Verify total hourly rain volume
            val totalHourlyRainVolume = forecast.hourly.sumOf { it.rain?.rainVolumeInAnHour ?: 0.0 }
            assertThat(totalHourlyRainVolume).isEqualTo(0.0)
        }

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
