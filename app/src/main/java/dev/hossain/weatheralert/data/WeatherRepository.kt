package dev.hossain.weatheralert.data
import com.openmeteo.api.OpenMeteoService
import com.openmeteo.api.model.OpenMeteoForecastResponse
import com.slack.eithernet.ApiResult
import com.slack.eithernet.exceptionOrNull
import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.db.CityForecast
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.util.TimeUtil
import io.tomorrow.api.TomorrowIoService
import org.openweathermap.api.OpenWeatherService
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for weather data that caches and provides weather forecast data.
 */
interface WeatherRepository {
    /**
     * Provides the daily weather forecast for a given city either from
     * cache or network based on data freshness or [skipCache] value.
     *
     * **NOTE:** The weather data is loaded using [ActiveWeatherService] automatically.
     *
     * @param cityId The ID of the city from the database.
     * @param latitude The latitude of the city.
     * @param longitude The longitude of the city.
     * @param skipCache Whether to skip the cache and fetch fresh data.
     * @return An [ApiResult] containing the [AppForecastData] or an error.
     */
    suspend fun getDailyForecast(
        cityId: Long,
        latitude: Double,
        longitude: Double,
        skipCache: Boolean = false,
    ): ApiResult<AppForecastData, Unit>

    /**
     * Validates the given API key by sending a basic API request for given [weatherForecastService].
     */
    suspend fun isValidApiKey(
        weatherForecastService: WeatherForecastService,
        apiKey: String,
    ): ApiResult<Boolean, String>
}

/**
 * Implementation of [WeatherRepository] that uses [OpenWeatherService] to fetch weather data.
 */
@ContributesBinding(AppScope::class)
class WeatherRepositoryImpl
    @Inject
    constructor(
        private val apiKeyProvider: ApiKeyProvider,
        private val openWeatherService: OpenWeatherService,
        private val tomorrowIoService: TomorrowIoService,
        private val openMeteoService: OpenMeteoService,
        private val cityForecastDao: CityForecastDao,
        private val timeUtil: TimeUtil,
        private val activeWeatherService: ActiveWeatherService,
    ) : WeatherRepository {
        override suspend fun getDailyForecast(
            cityId: Long,
            latitude: Double,
            longitude: Double,
            skipCache: Boolean,
        ): ApiResult<AppForecastData, Unit> {
            val cityForecast = cityForecastDao.getCityForecastsByCityId(cityId).firstOrNull()

            return if (skipCache.not() &&
                cityForecast != null &&
                !timeUtil.isOlderThan24Hours(
                    cityForecast.createdAt,
                )
            ) {
                Timber.d("Using cached forecast data for cityId: %s, skipCache: %s", cityId, skipCache)
                ApiResult.success(convertToForecastData(cityForecast))
            } else {
                Timber.d(
                    "Fetching forecast data from network for cityId %s, skipCache: %s",
                    cityId,
                    skipCache,
                )
                loadForecastFromNetwork(latitude, longitude, cityId)
            }
        }

        override suspend fun isValidApiKey(
            weatherForecastService: WeatherForecastService,
            apiKey: String,
        ): ApiResult<Boolean, String> {
            when (weatherForecastService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> {
                    openWeatherService
                        .getWeatherOverview(
                            apiKey = apiKey,
                            // Use New York City coordinates for basic API key validation.
                            latitude = 40.7235827,
                            longitude = -73.985626,
                        ).let { apiResult ->
                            return when (apiResult) {
                                is ApiResult.Success -> ApiResult.success(true)
                                is ApiResult.Failure.ApiFailure -> {
                                    ApiResult.apiFailure(apiResult.error?.message ?: "Unknown API error")
                                }
                                is ApiResult.Failure.HttpFailure ->
                                    ApiResult.httpFailure(
                                        apiResult.code,
                                        apiResult.error?.message,
                                    )

                                is ApiResult.Failure.NetworkFailure -> ApiResult.networkFailure(apiResult.error)
                                is ApiResult.Failure.UnknownFailure -> ApiResult.unknownFailure(apiResult.error)
                            }
                        }
                }
                WeatherForecastService.TOMORROW_IO -> {
                    tomorrowIoService
                        .getRealTimeWeather(
                            apiKey = apiKey,
                            // Use Chicago City for basic API key validation.
                            location = "chicago",
                        ).let { apiResult ->
                            return when (apiResult) {
                                is ApiResult.Success -> ApiResult.success(true)
                                is ApiResult.Failure.ApiFailure -> ApiResult.apiFailure(apiResult.error?.message ?: "Unknown API error")
                                is ApiResult.Failure.HttpFailure ->
                                    ApiResult.httpFailure(
                                        apiResult.code,
                                        apiResult.error?.message,
                                    )

                                is ApiResult.Failure.NetworkFailure -> ApiResult.networkFailure(apiResult.error)
                                is ApiResult.Failure.UnknownFailure -> ApiResult.unknownFailure(apiResult.error)
                            }
                        }
                }

                WeatherForecastService.OPEN_METEO -> throw IllegalStateException("No API key needed for Open-Meteo")
            }
        }

        private suspend fun loadForecastFromNetwork(
            latitude: Double,
            longitude: Double,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val selectedService = activeWeatherService.selectedService()
            return when (selectedService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> {
                    loadForecastUseOpenWeather(
                        weatherForecastService = selectedService,
                        latitude = latitude,
                        longitude = longitude,
                        cityId = cityId,
                    )
                }

                WeatherForecastService.TOMORROW_IO -> {
                    loadForecastUseTomorrowIo(
                        weatherForecastService = selectedService,
                        latitude = latitude,
                        longitude = longitude,
                        cityId = cityId,
                    )
                }

                WeatherForecastService.OPEN_METEO -> {
                    loadForecastUseOpenMeteo(
                        weatherForecastService = selectedService,
                        latitude = latitude,
                        longitude = longitude,
                        cityId = cityId,
                    )
                }
            }
        }

        private suspend fun WeatherRepositoryImpl.loadForecastUseOpenWeather(
            weatherForecastService: WeatherForecastService,
            latitude: Double,
            longitude: Double,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val apiResult =
                openWeatherService.getDailyForecast(
                    apiKey = apiKeyProvider.activeServiceApiKey,
                    latitude = latitude,
                    longitude = longitude,
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = (apiResult.value as WeatherApiServiceResponse).convertToForecastData()
                    cacheCityForecastData(weatherForecastService, cityId, convertToForecastData)
                    ApiResult.success(convertToForecastData)
                }

                is ApiResult.Failure -> {
                    Timber.e(apiResult.exceptionOrNull(), "Failed to fetch OpenWeather forecast data")
                    apiResult
                }
            }
        }

        private suspend fun WeatherRepositoryImpl.loadForecastUseTomorrowIo(
            weatherForecastService: WeatherForecastService,
            latitude: Double,
            longitude: Double,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val apiResult =
                tomorrowIoService.getWeatherForecast(
                    location = "$latitude,$longitude",
                    apiKey = apiKeyProvider.activeServiceApiKey,
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = (apiResult.value as WeatherApiServiceResponse).convertToForecastData()
                    cacheCityForecastData(weatherForecastService, cityId, convertToForecastData)
                    ApiResult.success(convertToForecastData)
                }

                is ApiResult.Failure -> {
                    Timber.e(apiResult.exceptionOrNull(), "Failed to fetch Tomorrow.io forecast data")
                    apiResult
                }
            }
        }

        private suspend fun WeatherRepositoryImpl.loadForecastUseOpenMeteo(
            weatherForecastService: WeatherForecastService,
            latitude: Double,
            longitude: Double,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> =
            runCatching {
                openMeteoService.getWeatherForecast(
                    latitude = latitude.toFloat(),
                    longitude = longitude.toFloat(),
                )
            }.onSuccess {
                cacheCityForecastData(weatherForecastService, cityId, it.appForecastData)
            }.onFailure {
                Timber.e(it, "Failed to fetch Open-Meteo forecast data")
            }.fold(
                onSuccess = { response: OpenMeteoForecastResponse ->
                    ApiResult.success(response.appForecastData)
                },
                onFailure = { error ->
                    ApiResult.unknownFailure(error)
                },
            )

        private suspend fun cacheCityForecastData(
            weatherForecastService: WeatherForecastService,
            cityId: Long,
            convertToAppForecastData: AppForecastData,
        ) {
            cityForecastDao.insertCityForecast(
                CityForecast(
                    cityId = cityId,
                    latitude = convertToAppForecastData.latitude,
                    longitude = convertToAppForecastData.longitude,
                    dailyCumulativeSnow = convertToAppForecastData.snow.dailyCumulativeSnow,
                    nextDaySnow = convertToAppForecastData.snow.nextDaySnow,
                    dailyCumulativeRain = convertToAppForecastData.rain.dailyCumulativeRain,
                    nextDayRain = convertToAppForecastData.rain.nextDayRain,
                    forecastSourceService = weatherForecastService,
                    hourlyPrecipitation = convertToAppForecastData.hourlyPrecipitation,
                ),
            )
        }

        private fun convertToForecastData(cityForecast: CityForecast) =
            AppForecastData(
                latitude = cityForecast.latitude,
                longitude = cityForecast.longitude,
                snow =
                    Snow(
                        dailyCumulativeSnow = cityForecast.dailyCumulativeSnow,
                        nextDaySnow = cityForecast.nextDaySnow,
                        weeklyCumulativeSnow = 0.0,
                    ),
                rain =
                    Rain(
                        dailyCumulativeRain = cityForecast.dailyCumulativeRain,
                        nextDayRain = cityForecast.nextDayRain,
                        weeklyCumulativeRain = 0.0,
                    ),
                hourlyPrecipitation = cityForecast.hourlyPrecipitation,
            )
    }
