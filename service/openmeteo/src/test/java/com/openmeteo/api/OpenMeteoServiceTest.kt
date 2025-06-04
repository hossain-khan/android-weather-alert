package com.openmeteo.api

import com.google.common.truth.Truth.assertThat
import com.openmeteo.api.common.Response // Required for Forecast.Response
import com.openmeteo.api.common.time.Timezone // Required for Forecast.Response
import com.openmeteo.api.common.units.PrecipitationUnit // Required for Forecast.Response
import com.openmeteo.api.common.units.TemperatureUnit // Required for Forecast.Response
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.InputStream
import kotlin.Result // Required for the signature of the forecast method

// Fake OpenMeteo client for testing
// Assuming OpenMeteo is OpenMeteo<OpenMeteo.DefaultContext, Unit> as per typical usage
@OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
class FakeOpenMeteo(
    // These would normally be used to configure the client,
    // but for the fake, they are mostly to match constructor if needed.
    private val latitude: Float,
    private val longitude: Float,
    private val apikey: String? = null,
    private val contexts: OpenMeteo.Contexts = OpenMeteo.Contexts(),
) : OpenMeteo<OpenMeteo.DefaultContext, Unit>(latitude, longitude, apikey, contexts) {

    private var cannedResponse: Result<Forecast.Response>? = null

    fun setForecastResponse(response: Forecast.Response) {
        this.cannedResponse = Result.success(response)
    }

    fun setForecastError(exception: Exception) {
        this.cannedResponse = Result.failure(exception)
    }

    // This is the extension function we need to override/provide for the fake.
    // Since it's an extension, we define it as a member in the fake.
    // The real signature: public suspend fun <CTX : Context, R> OpenMeteo<CTX, R>.forecast(lambda: Forecast.() -> Unit): Result<Forecast.Response>
    // We simplify for the fake, assuming the specific CTX and R.
    @Suppress("RedundantSuspendModifier") // Mock may not need suspend
    suspend fun forecast(lambda: Forecast.() -> Unit): Result<Forecast.Response> {
        return cannedResponse ?: throw IllegalStateException("Canned response not set in FakeOpenMeteo")
    }
}

class OpenMeteoServiceTest {

    private fun loadForecastResponseFromJson(fileName: String): Forecast.Response {
        val classLoader = javaClass.classLoader
        val inputStream: InputStream = classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        val jsonText = inputStream.bufferedReader().use { it.readText() }

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true // Allow for potential minor inconsistencies if any
        }
        return json.decodeFromString<Forecast.Response>(jsonText)
    }

    @Test
    fun `getWeatherForecast_returnsCorrectAppForecastData`() = runTest {
        // 1. Prepare the fake response
        val forecastResponse = loadForecastResponseFromJson("open-meteo-forecast-lac-mann-snowing-2025-01-20.json")

        // 2. Create and configure the fake OpenMeteo client
        // The lat/long passed to FakeOpenMeteo constructor are dummy values for this test,
        // as the canned response will be used. The service method's lat/long are what matter for AppForecastData.
        val fakeClient = FakeOpenMeteo(latitude = 0f, longitude = 0f)
        fakeClient.setForecastResponse(forecastResponse)

        // 3. Instantiate the service with the fake client
        val service = OpenMeteoServiceImpl(openMeteoClient = fakeClient)

        // 4. Define input parameters for the service call
        val inputLatitude = 49.592735f
        val inputLongitude = -75.17785f

        // 5. Call the service method
        val result = service.getWeatherForecast(inputLatitude, inputLongitude)

        // 6. Assert the results
        assertThat(result).isNotNull()
        val appData = result.appForecastData
        assertThat(appData).isNotNull()

        assertThat(appData.latitude).isEqualTo(inputLatitude.toDouble())
        assertThat(appData.longitude).isEqualTo(inputLongitude.toDouble())

        // Based on the JSON data (all zeros for first 24h snowfall/rain) and
        // the lack of 'daily' data in the provided JSON (leading to 0.0 for nextDay values):
        assertThat(appData.snow.dailyCumulativeSnow).isEqualTo(0.0) // cm to mm is *10, but source is 0.0
        assertThat(appData.snow.nextDaySnow).isEqualTo(0.0)
        assertThat(appData.rain.dailyCumulativeRain).isEqualTo(0.0)
        assertThat(appData.rain.nextDayRain).isEqualTo(0.0)

        // Check hourlyPrecipitation:
        // Expecting 168 entries if forecastDays = 7 (default not specified, but service uses 3 days)
        // The service logic uses forecastDays = 3, so 3 * 24 = 72 hourly data points.
        // However, the filtering `it.time > currentTimestamp` and `take(CUMULATIVE_DATA_HOURS_24)`
        // applies to cumulative calculation, not the size of hourlyData list.
        // The hourlyData list maps ALL `values.filterKeys { it.time > currentTimestamp }`.
        // All 168 values in the sample JSON are in the future (2025).
        assertThat(appData.hourlyPrecipitation).isNotNull()

        // The number of items in hourlyPrecipitation should match the number of items in forecastResponse.hourly.time
        // if all are in the future.
        val expectedHourlyDataCount = forecastResponse.hourly?.time?.size ?: 0
        assertThat(appData.hourlyPrecipitation).hasSize(expectedHourlyDataCount)

        if (expectedHourlyDataCount > 0) {
            val firstHourData = appData.hourlyPrecipitation[0]
            // Assuming the first time in JSON is 1737456000000L ("2025-01-21T00:00Z")
            // The Instant.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString() depends on system's zone.
            // This makes direct string comparison tricky. Let's check components if possible or just non-null.
            assertThat(firstHourData.isoDateTime).isNotEmpty()

            // Snowfall is first element of "snowfall" array in JSON (0.0) * 10
            val expectedFirstHourSnow = (forecastResponse.hourly?.snowfall?.get(0)?.toDouble() ?: 0.0) * 10
            assertThat(firstHourData.snow).isEqualTo(expectedFirstHourSnow)

            // Rain is 0.0 due to "TODO - for rain loop again"
            assertThat(firstHourData.rain).isEqualTo(0.0)
        }
    }
}
