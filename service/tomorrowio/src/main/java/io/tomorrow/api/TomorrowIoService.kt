package io.tomorrow.api

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import io.tomorrow.api.model.RealTimeWeatherResponse
import io.tomorrow.api.model.TomorrowIoApiErrorResponse
import io.tomorrow.api.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for Tomorrow.io weather data.
 *
 * Free Tier Limits
 * - Requests per second: 3
 * - Requests per hour: 25
 * - Requests per day: 500
 */
interface TomorrowIoService {
    /**
     * Fetch weather forecast data based on location.
     *
     * @param location The location coordinates in "latitude,longitude" format.
     * @param apiKey The API key for authentication.
     * @return A [Call] object with [WeatherResponse].
     */
    @GET("v4/weather/forecast")
    suspend fun getWeatherForecast(
        @Query("location") location: String,
        @Query("apikey") apiKey: String,
    ): ApiResult<WeatherResponse, Unit>

    /**
     * Fetch real-time weather data based on location.
     *
     * @param location The location coordinates in "latitude,longitude" format.
     * @param apiKey The API key for authentication.
     * @return A [ApiResult] object with [WeatherResponse].
     */
    @DecodeErrorBody
    @GET("v4/weather/realtime")
    suspend fun getRealTimeWeather(
        @Query("location") location: String,
        @Query("apikey") apiKey: String,
    ): ApiResult<RealTimeWeatherResponse, TomorrowIoApiErrorResponse>
}
