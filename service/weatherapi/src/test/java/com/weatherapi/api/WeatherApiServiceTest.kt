package com.weatherapi.api

import com.google.common.truth.Truth.assertThat
import com.slack.eithernet.ApiResult
import com.weatherapi.api.model.ForecastWeatherResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests [WeatherApiService] using [MockWebServer].
 */
@RunWith(RobolectricTestRunner::class)
class WeatherApiServiceTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var weatherApiService: WeatherApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        weatherApiService =
            WeatherApiServiceBuilder
                .provideWeatherApiService(baseUrl = mockWebServer.url("/"))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given buffalo forecast response - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("weatherapi-buffalo-2025-02-05.json")),
            )

            val result =
                weatherApiService.getForecastWeather(
                    location = "42.8864,-78.8786",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: ForecastWeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.lat).isEqualTo(42.8864)
            assertThat(forecast.location.lon).isEqualTo(-78.8786)
        }

    @Test
    fun `given regina forecast response - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("weatherapi-regina-2025-02-05.json")),
            )

            val result =
                weatherApiService.getForecastWeather(
                    location = "50.45,-104.6167",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: ForecastWeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.lat).isEqualTo(50.45)
            assertThat(forecast.location.lon).isEqualTo(-104.6167)
        }

    @Test
    fun `given maringa forecast response - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("weatherapi-maringa-brazil-2025-02-05-rain.json")),
            )

            val result =
                weatherApiService.getForecastWeather(
                    location = "-23.4167,-51.9167",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: ForecastWeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.lat).isEqualTo(-23.4167)
            assertThat(forecast.location.lon).isEqualTo(-51.9167)
        }

    @Test
    fun `given uozo forecast response - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("weatherapi-uozo-japan-2025-02-05-snow.json")),
            )

            val result =
                weatherApiService.getForecastWeather(
                    location = "36.8,137.4",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: ForecastWeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.lat).isEqualTo(36.8)
            assertThat(forecast.location.lon).isEqualTo(137.4)
        }

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
