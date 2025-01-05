package dev.hossain.weatheralert.data
import com.slack.eithernet.ApiResult
import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject

/**
 * Repository for weather data.
 */
interface WeatherRepository {
    suspend fun getDailyForecast(
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
    ) : WeatherRepository {
        override suspend fun getDailyForecast(
            latitude: Double,
            longitude: Double,
        ): ApiResult<ForecastData, Unit> {
            val apiResult =
                api.getDailyForecast(
                    apiKey = apiKey.key,
                    latitude = latitude,
                    longitude = longitude,
                )
            return when (apiResult) {
                is ApiResult.Success -> {
                    ApiResult.success(convertToForecastData(apiResult.value))
                }

                is ApiResult.Failure -> {
                    apiResult
                }
            }
        }

        private fun convertToForecastData(weatherForecast: WeatherForecast): ForecastData {
            // Convert `WeatherForecast` to `ForecastData`
            return ForecastData(
                cityName = "TBD",
                latitude = weatherForecast.lat,
                longitude = weatherForecast.lon,
                snow =
                    Snow(
                        dailyCumulativeSnow = weatherForecast.totalSnowVolume,
                        nextDaySnow = weatherForecast.daily.firstOrNull()?.snowVolume ?: 0.0,
                        weeklyCumulativeSnow = 0.0,
                    ),
                rain =
                    Rain(
                        dailyCumulativeRain = 0.0,
                        nextDayRain = weatherForecast.daily.firstOrNull()?.rainVolume ?: 0.0,
                        weeklyCumulativeRain = 0.0,
                    ),
            )
        }
    }
