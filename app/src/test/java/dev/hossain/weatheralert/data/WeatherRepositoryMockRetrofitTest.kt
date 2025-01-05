package dev.hossain.weatheralert.data

import com.slack.eithernet.ApiResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit

/**
 * Tests [WeatherRepository] using [MockRetrofit].
 *
 * See [WeatherRepositoryTest] for real server test using [MockWebServer].
 */
class WeatherRepositoryMockRetrofitTest {
    private lateinit var mockRetrofit: MockRetrofit
    private lateinit var behaviorDelegate: BehaviorDelegate<WeatherApi>
    private lateinit var weatherRepository: WeatherRepository

    @Before
    fun setUp() {
        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        val retrofit =
            Retrofit
                .Builder()
                .baseUrl("http://localhost/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        val networkBehavior =
            NetworkBehavior.create().apply {
                setDelay(0, TimeUnit.MILLISECONDS)
                setFailurePercent(0)
                setVariancePercent(0)
            }

        mockRetrofit =
            MockRetrofit
                .Builder(retrofit)
                .networkBehavior(networkBehavior)
                .build()

        behaviorDelegate = mockRetrofit.create(WeatherApi::class.java)
        val mockWeatherApi = MockWeatherApi(behaviorDelegate)
        weatherRepository = WeatherRepositoryImpl(ApiKeyImpl(), mockWeatherApi)
    }

    @Test
    fun testGetDailyForecast() =
        runBlocking {
            val result =
                weatherRepository.getDailyForecast(
                    latitude = 0.0,
                    longitude = -0.0,
                )
            assert(result is ApiResult.Success)
            val forecast = (result as ApiResult.Success).value
            assertEquals(0, forecast.daily.size)
        }

    class MockWeatherApi(
        private val delegate: BehaviorDelegate<WeatherApi>,
    ) : WeatherApi {
        override suspend fun getDailyForecast(
            apiKey: String,
            latitude: Double,
            longitude: Double,
            exclude: String,
            units: String,
        ): ApiResult<WeatherForecast, Unit> {
            val result =
                ApiResult.success(
                    WeatherForecast(
                        lat = latitude,
                        lon = longitude,
                        timezone = "America/Toronto",
                        timezoneOffset = -18000,
                        daily = emptyList(),
                    ),
                )

            return delegate.returningResponse(result).getDailyForecast("key", latitude, longitude)
        }
    }
}
