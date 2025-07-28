package dev.hossain.weatheralert.data

import com.openmeteo.api.OpenMeteoService
import com.openmeteo.api.model.OpenMeteoForecastResponse
import com.slack.eithernet.ApiResult
import com.slack.eithernet.exceptionOrNull
import com.weatherapi.api.WeatherApiService
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.db.CityForecast
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.util.TimeUtil
import dev.zacsweers.metro.Inject
import io.tomorrow.api.TomorrowIoService
import org.openweathermap.api.OpenWeatherService
import timber.log.Timber

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
     * @param alertId The ID of the alert from the database.
     * @param cityId The ID of the city from the database.
     * @param latitude The latitude of the city.
     * @param longitude The longitude of the city.
     * @param skipCache Whether to skip the cache and fetch fresh data.
     * @return An [ApiResult] containing the [AppForecastData] or an error.
     */
    suspend fun getDailyForecast(
        alertId: Long,
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
@Inject
class WeatherRepositoryImpl
    constructor(
        private val apiKeyProvider: ApiKeyProvider,
        private val openWeatherService: OpenWeatherService,
        private val tomorrowIoService: TomorrowIoService,
        private val openMeteoService: OpenMeteoService,
        private val weatherApiService: WeatherApiService,
        private val cityForecastDao: CityForecastDao,
        private val timeUtil: TimeUtil,
        private val activeWeatherService: ActiveWeatherService,
    ) : WeatherRepository {
        override suspend fun getDailyForecast(
            alertId: Long,
            cityId: Long,
            latitude: Double,
            longitude: Double,
            skipCache: Boolean,
        ): ApiResult<AppForecastData, Unit> {
            val cityForecast = cityForecastDao.getCityForecastByAlertIdAndCityId(alertId, cityId)

            return if (skipCache.not() &&
                cityForecast != null &&
                !timeUtil.isOlderThan24Hours(
                    timeInMillis = cityForecast.createdAt,
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
                loadForecastFromNetwork(cityForecast?.forecastSourceService, latitude, longitude, alertId, cityId)
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

                else -> throw IllegalStateException("No API key needed for $weatherForecastService")
            }
        }

        private suspend fun loadForecastFromNetwork(
            weatherForecastService: WeatherForecastService?,
            latitude: Double,
            longitude: Double,
            alertId: Long,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val selectedForecastService = weatherForecastService ?: activeWeatherService.selectedService()
            return when (selectedForecastService) {
                WeatherForecastService.OPEN_WEATHER_MAP -> {
                    loadForecastUseOpenWeather(
                        weatherForecastService = selectedForecastService,
                        latitude = latitude,
                        longitude = longitude,
                        alertId = alertId,
                        cityId = cityId,
                    )
                }

                WeatherForecastService.TOMORROW_IO -> {
                    loadForecastUseTomorrowIo(
                        weatherForecastService = selectedForecastService,
                        latitude = latitude,
                        longitude = longitude,
                        alertId = alertId,
                        cityId = cityId,
                    )
                }

                WeatherForecastService.OPEN_METEO -> {
                    loadForecastUseOpenMeteo(
                        weatherForecastService = selectedForecastService,
                        latitude = latitude,
                        longitude = longitude,
                        alertId = alertId,
                        cityId = cityId,
                    )
                }

                WeatherForecastService.WEATHER_API -> {
                    loadForecastUseWeatherApi(
                        weatherForecastService = selectedForecastService,
                        latitude = latitude,
                        longitude = longitude,
                        alertId = alertId,
                        cityId = cityId,
                    )
                }
            }
        }

        private suspend fun WeatherRepositoryImpl.loadForecastUseOpenWeather(
            weatherForecastService: WeatherForecastService,
            latitude: Double,
            longitude: Double,
            alertId: Long,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val apiResult =
                openWeatherService.getDailyForecast(
                    apiKey = apiKeyProvider.apiKey(WeatherForecastService.OPEN_WEATHER_MAP),
                    latitude = latitude,
                    longitude = longitude,
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = (apiResult.value as WeatherApiServiceResponse).convertToForecastData()
                    cacheCityForecastData(alertId, cityId, weatherForecastService, convertToForecastData)
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
            alertId: Long,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val apiResult =
                tomorrowIoService.getWeatherForecast(
                    location = "$latitude,$longitude",
                    apiKey = apiKeyProvider.apiKey(WeatherForecastService.TOMORROW_IO),
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = (apiResult.value as WeatherApiServiceResponse).convertToForecastData()
                    cacheCityForecastData(alertId, cityId, weatherForecastService, convertToForecastData)
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
            alertId: Long,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> =
            runCatching {
                openMeteoService.getWeatherForecast(
                    latitude = latitude.toFloat(),
                    longitude = longitude.toFloat(),
                )
            }.onSuccess {
                cacheCityForecastData(alertId, cityId, weatherForecastService, it.appForecastData)
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

        private suspend fun WeatherRepositoryImpl.loadForecastUseWeatherApi(
            weatherForecastService: WeatherForecastService,
            latitude: Double,
            longitude: Double,
            alertId: Long,
            cityId: Long,
        ): ApiResult<AppForecastData, Unit> {
            val apiResult =
                weatherApiService.getForecastWeather(
                    apiKey = BuildConfig.WEATHERAPI_API_KEY,
                    location = "$latitude,$longitude",
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = (apiResult.value as WeatherApiServiceResponse).convertToForecastData()
                    cacheCityForecastData(alertId, cityId, weatherForecastService, convertToForecastData)
                    ApiResult.success(convertToForecastData)
                }

                is ApiResult.Failure -> {
                    Timber.e(apiResult.exceptionOrNull(), "Failed to fetch WeatherAPI forecast data")
                    apiResult
                }
            }
        }

        private suspend fun cacheCityForecastData(
            alertId: Long,
            cityId: Long,
            weatherForecastService: WeatherForecastService,
            convertToAppForecastData: AppForecastData,
        ) {
            cityForecastDao.insertCityForecast(
                CityForecast(
                    alertId = alertId,
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
