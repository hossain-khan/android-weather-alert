package dev.hossain.weatheralert.data
import com.slack.eithernet.ApiResult
import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.db.CityForecast
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.util.TimeUtil
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for weather data.
 */
interface WeatherRepository {
    suspend fun getDailyForecast(
        cityId: Int,
        latitude: Double,
        longitude: Double,
    ): ApiResult<ForecastData, Unit>
}

/**
 * Implementation of [WeatherRepository] that uses [WeatherApi] to fetch weather data.
 */
@ContributesBinding(AppScope::class)
class WeatherRepositoryImpl
    @Inject
    constructor(
        private val apiKey: ApiKey,
        private val api: WeatherApi,
        private val cityForecastDao: CityForecastDao,
        private val timeUtil: TimeUtil,
    ) : WeatherRepository {
        override suspend fun getDailyForecast(
            cityId: Int,
            latitude: Double,
            longitude: Double,
        ): ApiResult<ForecastData, Unit> {
            val cityForecast = cityForecastDao.getCityForecastsByCityId(cityId).firstOrNull()

            return if (cityForecast != null && !timeUtil.isOlderThan24Hours(cityForecast.createdAt)) {
                Timber.d("Using cached forecast data for cityId: $cityId")
                ApiResult.success(convertToForecastData(cityForecast))
            } else {
                Timber.d("Fetching forecast data from network for cityId: $cityId")
                loadForecastFromNetwork(latitude, longitude, cityId)
            }
        }

        private suspend fun loadForecastFromNetwork(
            latitude: Double,
            longitude: Double,
            cityId: Int,
        ): ApiResult<ForecastData, Unit> {
            val apiResult =
                api.getDailyForecast(
                    apiKey = apiKey.key,
                    latitude = latitude,
                    longitude = longitude,
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    val convertToForecastData = convertToForecastData(apiResult.value)
                    cityForecastDao.insertCityForecast(
                        CityForecast(
                            cityId = cityId,
                            latitude = convertToForecastData.latitude,
                            longitude = convertToForecastData.longitude,
                            dailyCumulativeSnow = convertToForecastData.snow.dailyCumulativeSnow,
                            nextDaySnow = convertToForecastData.snow.nextDaySnow,
                            dailyCumulativeRain = convertToForecastData.rain.dailyCumulativeRain,
                            nextDayRain = convertToForecastData.rain.nextDayRain,
                        ),
                    )
                    ApiResult.success(convertToForecastData)
                }

                is ApiResult.Failure -> {
                    apiResult
                }
            }
        }

        private fun convertToForecastData(weatherForecast: WeatherForecast): ForecastData {
            // Convert `WeatherForecast` to `ForecastData`
            return ForecastData(
                latitude = weatherForecast.lat,
                longitude = weatherForecast.lon,
                snow =
                    Snow(
                        dailyCumulativeSnow = weatherForecast.hourly.sumOf { it.snow?.snowVolumeInAnHour ?: 0.0 },
                        nextDaySnow = weatherForecast.daily.firstOrNull()?.snowVolume ?: 0.0,
                        weeklyCumulativeSnow = 0.0,
                    ),
                rain =
                    Rain(
                        dailyCumulativeRain = weatherForecast.hourly.sumOf { it.rain?.rainVolumeInAnHour ?: 0.0 },
                        nextDayRain = weatherForecast.daily.firstOrNull()?.rainVolume ?: 0.0,
                        weeklyCumulativeRain = 0.0,
                    ),
            )
        }

        private fun convertToForecastData(cityForecast: CityForecast) =
            ForecastData(
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
            )
    }
