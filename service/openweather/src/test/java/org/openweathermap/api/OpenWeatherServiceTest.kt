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
class OpenWeatherServiceTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var tomorrowIoService: OpenWeatherService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)

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

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
