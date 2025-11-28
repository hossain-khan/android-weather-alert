package io.tomorrow.api

import com.google.common.truth.Truth.assertThat
import com.slack.eithernet.ApiResult
import io.tomorrow.api.model.RealTimeWeatherResponse
import io.tomorrow.api.model.TomorrowIoApiErrorResponse
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
        mockWebServer.start()

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

    @Test
    fun `given realtime response for toronto city - parses data properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("tomorrow-io-realtime-data-toronto.json")),
            )

            val result =
                tomorrowIoService.getRealTimeWeather(
                    location = "toronto",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val realtimeForecast: RealTimeWeatherResponse = (result as ApiResult.Success).value
            assertThat(realtimeForecast.location.latitude).isEqualTo(43.653480529785156)
            assertThat(realtimeForecast.location.longitude).isEqualTo(-79.3839340209961)
            assertThat(realtimeForecast.data.time).isEqualTo("2025-01-12T01:40:00Z")
        }

    @Test
    fun `given forecast response for lac mann - parses data properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(loadJsonFromResources("tomorrow-io-lac-mann-snowing-2025-01-20.json")),
            )

            val result =
                tomorrowIoService.getWeatherForecast(
                    location = "49.587967,-75.16987",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            val forecast: WeatherResponse = (result as ApiResult.Success).value
            assertThat(forecast.location.latitude).isEqualTo(49.587967)
            assertThat(forecast.location.longitude).isEqualTo(-75.16987)
            assertThat(forecast.timelines.minutely).hasSize(60)
            assertThat(forecast.timelines.hourly).hasSize(120)
            assertThat(forecast.timelines.daily).hasSize(7)

            // verify total hourly snowfall
            val totalHourlySnowfall = forecast.timelines.hourly.sumOf { it.values.snowAccumulation ?: 0.0 }
            assertThat(totalHourlySnowfall).isEqualTo(341.3000000000002)

            // verify total hourly rain
            val totalHourlyRain = forecast.timelines.hourly.sumOf { it.values.rainAccumulation ?: 0.0 }
            assertThat(totalHourlyRain).isEqualTo(0.0)

            // Verify total daily snowfall
            val totalDailySnowfall = forecast.timelines.daily.sumOf { it.values.snowAccumulation ?: 0.0 }
            assertThat(totalDailySnowfall).isEqualTo(0.0)

            // Verify total daily rain
            val totalDailyRain = forecast.timelines.daily.sumOf { it.values.rainAccumulation ?: 0.0 }
            assertThat(totalDailyRain).isEqualTo(0.0)
        }

    @Test
    fun `given realtime response that errors out - parses error body properly`() =
        runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(429)
                    .setBody(
                        """
                        {
                          "code": 429001,
                          "type": "Too Many Calls",
                          "message": "The request limit for this resource has been reached."
                        }
                        """.trimIndent(),
                    ),
            )

            val result =
                tomorrowIoService.getRealTimeWeather(
                    location = "toronto",
                    apiKey = "fake-api-key",
                )

            assertThat(result).isInstanceOf(ApiResult.Failure::class.java)
            val apiErrorResponse: TomorrowIoApiErrorResponse = (result as ApiResult.Failure.HttpFailure).error!!
            assertThat(apiErrorResponse.code).isEqualTo(429001)
            assertThat(apiErrorResponse.type).isEqualTo("Too Many Calls")
            assertThat(apiErrorResponse.message).isEqualTo("The request limit for this resource has been reached.")
        }

    // Helper method to load JSON from test resources
    private fun loadJsonFromResources(fileName: String): String {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("File not found: $fileName")
        return inputStream.bufferedReader().use { it.readText() }
    }
}
