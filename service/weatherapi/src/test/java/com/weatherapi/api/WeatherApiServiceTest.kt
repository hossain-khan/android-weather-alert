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
    fun `given simplified response - parses data`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("weatherapi-buffalo-2025-02-04.json")),
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

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
