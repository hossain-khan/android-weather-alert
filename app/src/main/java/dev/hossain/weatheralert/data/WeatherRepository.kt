package dev.hossain.weatheralert.data
import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.di.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository for weather data.
 */
interface WeatherRepository {
    suspend fun getDailyForecast(
        latitude: Double,
        longitude: Double,
        apiKey: String,
    ): WeatherForecast

    fun getWeatherForecastFlow(
        latitude: Double,
        longitude: Double,
        apiKey: String,
    ): Flow<WeatherForecast>
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
        ): WeatherForecast = api.getDailyForecast(latitude = latitude, longitude = longitude, apiKey = apiKey)

        override fun getWeatherForecastFlow(
            latitude: Double,
            longitude: Double,
            apiKey: String,
        ): Flow<WeatherForecast> =
            flow {
                emit(getDailyForecast(latitude, longitude, apiKey))
            }
    }
