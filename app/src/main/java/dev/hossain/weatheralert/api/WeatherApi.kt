package dev.hossain.weatheralert.api

import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap API for weather forecast.
 *
 * Free tier:
 * - 60 calls/minute
 * - 1,000 API calls/day
 * - 1,000,000 calls/month
 *
 * See:
 * - [Weather units](https://openweathermap.org/weather-data)
 * - [One Call API](https://openweathermap.org/api/one-call-3)
 */
interface WeatherApi {
    companion object {
        // Units of measurement - `standard`, `metric`, and `imperial` units are available.
        // However, `metric` as baseline and conversion will be used later if needed.
        const val UNIT_METRIC = "metric"
    }

    @GET("data/3.0/onecall")
    suspend fun getDailyForecast(
        @Query("appid") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String = "current,minutely",
        @Query("units") units: String = UNIT_METRIC,
    ): ApiResult<WeatherForecast, Unit>
}
