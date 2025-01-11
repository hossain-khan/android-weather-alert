package io.tomorrow.api

import com.slack.eithernet.ApiResult
import io.tomorrow.api.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for Tomorrow.io weather data.
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
}
