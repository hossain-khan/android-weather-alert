package io.tomorrow.api

import com.google.common.truth.Truth.assertThat
import com.slack.eithernet.ApiResult
import io.tomorrow.api.model.WeatherResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests [TomorrowIoService] using [MockWebServer].
 */
@RunWith(RobolectricTestRunner::class)
class TomorrowIoServiceTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private lateinit var tomorrowIoService: TomorrowIoService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)

        tomorrowIoService =
            TomorrowIoServiceBuilder
                .provideTomorrowIoService(baseUrl = mockWebServer.url("/"))
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
                    .setBody(loadJsonFromResources("tomorrow-io-smaller-dataset.json")),
            )

            val result =
                tomorrowIoService.getWeatherForecast(
                    location = "43.9,-78.85",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.latitude).isEqualTo(43.9)
            assertThat(forecast.location.longitude).isEqualTo(-78.85)
            assertThat(forecast.timelines.minutely).hasSize(2)
            assertThat(forecast.timelines.hourly).hasSize(2)
            assertThat(forecast.timelines.daily).hasSize(2)
        }

    @Test
    fun `given response for oshawa city - parses data properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("tomorrow-io-oshawa-forecast-2025-01-10.json")),
            )

            val result =
                tomorrowIoService.getWeatherForecast(
                    location = "43.9,-78.85",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.latitude).isEqualTo(43.9)
            assertThat(forecast.location.longitude).isEqualTo(-78.85)
            assertThat(forecast.timelines.minutely).hasSize(60)
            assertThat(forecast.timelines.hourly).hasSize(120)
            assertThat(forecast.timelines.daily).hasSize(7)
        }

    // Helper method to load JSON from resources
    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader?.getResourceAsStream(fileName)
        return inputStream?.bufferedReader().use { it?.readText() } ?: throw IllegalArgumentException("File not found: $fileName")
    }
}
