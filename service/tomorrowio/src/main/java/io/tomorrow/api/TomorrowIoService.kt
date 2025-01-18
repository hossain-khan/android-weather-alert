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
    companion object {
        /**
         * 401 UNAUTHORIZED:
         * Code	    Type	            Description
         * ------   -----------         ------------
         * 401001	Invalid Key	        The method requires authentication, but it was not presented or is invalid.
         */
        const val ERROR_HTTP_UNAUTHORIZED = 401

        /**
         * 402 PAYMENT REQUIRED:
         * Code	    Type	            Description
         * ------   -----------         ------------
         * 402001	Insufficient Tokens	Adding additional tokens is required
         */
        const val ERROR_HTTP_PAYMENT_REQUIRED = 402

        /**
         * 403 FORBIDDEN:
         *
         * Code	    Type	            Description
         * ------   -----------         ------------
         * 403001	Access Denied	    The authentication token in use is restricted and cannot access the requested resource.
         * 403002	Account Limit	    The plan limit for a resource has been reached.
         * 403003	Forbidden Action	The plan is restricted and cannot perform this action.
         */
        const val ERROR_HTTP_FORBIDDEN = 403

        /**
         * 404 NOT FOUND:
         *
         * Code	    Type	            Description
         * ------   -----------         ------------
         * 404001	Not Found	        A resource id was not found.
         */
        const val ERROR_HTTP_NOT_FOUND = 404
    }

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
