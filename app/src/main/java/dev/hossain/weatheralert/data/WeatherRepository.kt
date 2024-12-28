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
        apiKey: String,
    ): ApiResult<WeatherForecast, Unit>
}

/**
 * Implementation of [WeatherRepository] that uses [WeatherApi] to fetch weather data.
 */
@ContributesBinding(AppScope::class)
class WeatherRepositoryImpl
    @Inject
    constructor(
        private val api: WeatherApi,
    ) : WeatherRepository {
        override suspend fun getDailyForecast(
            latitude: Double,
            longitude: Double,
            apiKey: String,
        ): ApiResult<WeatherForecast, Unit> = api.getDailyForecast(latitude = latitude, longitude = longitude, apiKey = apiKey)
    }
