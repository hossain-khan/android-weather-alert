package dev.hossain.weatheralert.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.openmeteo.api.OpenMeteoServiceImpl
import com.slack.eithernet.ApiResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.di.DaggerTestAppComponent
import dev.hossain.weatheralert.util.TimeUtil
import io.tomorrow.api.TomorrowIoService
import io.tomorrow.api.model.RealTimeWeatherResponse
import io.tomorrow.api.model.TomorrowIoApiErrorResponse
import io.tomorrow.api.model.WeatherResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openweathermap.api.OpenWeatherService
import org.openweathermap.api.model.ErrorResponse
import org.openweathermap.api.model.WeatherForecast
import org.openweathermap.api.model.WeatherOverview
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Tests [WeatherRepository] using [MockRetrofit].
 *
 * See [WeatherRepositoryTest] for real server test using [MockWebServer].
 */
@RunWith(RobolectricTestRunner::class)
class WeatherRepositoryMockRetrofitTest {
    private lateinit var mockRetrofit: MockRetrofit
    private lateinit var openWeatherServiceDelegate: BehaviorDelegate<OpenWeatherService>
    private lateinit var tomorrowIoServiceDelegate: BehaviorDelegate<TomorrowIoService>
    private lateinit var weatherRepository: WeatherRepository

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Inject
    lateinit var cityForecastDao: CityForecastDao

    @Inject
    lateinit var timeUtil: TimeUtil

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var activeWeatherService: ActiveWeatherService

    @Before
    fun setUp() {
        val testAppComponent = DaggerTestAppComponent.factory().create(context)
        testAppComponent.inject(this)

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

        openWeatherServiceDelegate = mockRetrofit.create(OpenWeatherService::class.java)
        tomorrowIoServiceDelegate = mockRetrofit.create(TomorrowIoService::class.java)
        val mockWeatherApi = MockOpenWeatherService(openWeatherServiceDelegate)
        val mockTomorrowIoAi = MockTomorrowIoService(tomorrowIoServiceDelegate)
        weatherRepository =
            WeatherRepositoryImpl(
                apiKeyProvider = ApiKeyProviderImpl(preferencesManager),
                openWeatherService = mockWeatherApi,
                tomorrowIoService = mockTomorrowIoAi,
                openMeteoService = OpenMeteoServiceImpl(),
                cityForecastDao = cityForecastDao,
                timeUtil = timeUtil,
                activeWeatherService = activeWeatherService,
            )
    }

    @Test
    fun testGetDailyForecast() =
        runBlocking {
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

    class MockOpenWeatherService(
        private val delegate: BehaviorDelegate<OpenWeatherService>,
    ) : OpenWeatherService {
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

        override suspend fun getWeatherOverview(
            apiKey: String,
            latitude: Double,
            longitude: Double,
        ): ApiResult<WeatherOverview, ErrorResponse> =
            delegate
                .returningResponse(
                    ApiResult.success(
                        WeatherOverview(
                            latitude = latitude,
                            longitude = longitude,
                            timezone = "America/Toronto",
                            date = "2021-09-01",
                            units = "metric",
                            weatherOverview = "Weather overview for location",
                        ),
                    ),
                ).getWeatherOverview("key", latitude, longitude)
    }

    class MockTomorrowIoService(
        private val delegate: BehaviorDelegate<TomorrowIoService>,
    ) : TomorrowIoService {
        override suspend fun getWeatherForecast(
            location: String,
            apiKey: String,
        ): ApiResult<WeatherResponse, Unit> {
            val result =
                ApiResult.success(
                    WeatherResponse(
                        location =
                            io.tomorrow.api.model.Location(
                                latitude = 0.0,
                                longitude = 0.0,
                            ),
                        timelines =
                            io.tomorrow.api.model.Timelines(
                                minutely = emptyList(),
                                hourly = emptyList(),
                                daily = emptyList(),
                            ),
                    ),
                )

            return delegate.returningResponse(result).getWeatherForecast("0.0,0.0", "key")
        }

        override suspend fun getRealTimeWeather(
            location: String,
            apiKey: String,
        ): ApiResult<RealTimeWeatherResponse, TomorrowIoApiErrorResponse> {
            val result =
                ApiResult.success(
                    RealTimeWeatherResponse(
                        location =
                            io.tomorrow.api.model.Location(
                                latitude = 0.0,
                                longitude = 0.0,
                            ),
                        data =
                            io.tomorrow.api.model.TimelineData(
                                time = "2021-09-01T00:00:00Z",
                                values =
                                    io.tomorrow.api.model.WeatherValues(
                                        cloudBase = 1.0,
                                        cloudCeiling = 2.0,
                                        cloudCover = 50.0,
                                        dewPoint = 10.0,
                                        evapotranspiration = 0.5,
                                        freezingRainIntensity = 0.0,
                                        hailProbability = 20.0,
                                        hailSize = 1.0,
                                        humidity = 80.0,
                                        iceAccumulation = 0.0,
                                        iceAccumulationLwe = 0.0,
                                        precipitationProbability = 30,
                                        pressureSurfaceLevel = 1013.0,
                                        rainAccumulation = 5.0,
                                        rainAccumulationLwe = 5.0,
                                        rainIntensity = 0.1,
                                        sleetAccumulation = 0.0,
                                        sleetAccumulationLwe = 0.0,
                                        sleetIntensity = 0.0,
                                        snowAccumulation = 0.0,
                                        snowAccumulationLwe = 0.0,
                                        snowDepth = 0.0,
                                        snowIntensity = 0.0,
                                        temperature = 25.0,
                                        temperatureApparent = 27.0,
                                        uvHealthConcern = 1,
                                        uvIndex = 5,
                                        visibility = 10.0,
                                        weatherCode = 1000,
                                        windDirection = 180.0,
                                        windGust = 15.0,
                                        windSpeed = 10.0,
                                    ),
                            ),
                    ),
                )
            return delegate.returningResponse(result).getRealTimeWeather("0.0,0.0", "key")
        }
    }
}
