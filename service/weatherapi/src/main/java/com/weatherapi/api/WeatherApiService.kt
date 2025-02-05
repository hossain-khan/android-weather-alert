package com.weatherapi.api

import com.slack.eithernet.ApiResult
import com.weatherapi.api.model.ForecastWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    /**
     * Fetches the weather forecast data.
     *
     * @param apiKey The API key for authentication.
     * @param location Pass US Zipcode, UK Postcode, Canada Postalcode, IP address, Latitude/Longitude (decimal degree) or city name. Latitude and Longitude (Decimal degree) e.g: q=48.8567,2.3508
     * @param days Number of days of weather forecast. Value ranges from 1 to 14.
     * @param date Date should be between today and next 14 day in yyyy-MM-dd format. e.g. '2015-01-01'.
     * @param unixDate Please either pass 'dt' or 'unixdt' and not both in same request. unixdt should be between today and next 14 day in Unix format. e.g. 1490227200.
     * @param hour Must be in 24 hour. For example 5 pm should be hour=17, 6 am as hour=6.
     * @param language Returns 'condition:text' field in API in the desired language.
     * @param alerts Enable/Disable alerts in forecast API output. Example, alerts=yes or alerts=no.
     * @param airQuality Enable/Disable Air Quality data in forecast API output. Example, aqi=yes or aqi=no.
     * @return A [ApiResult] object with [ForecastWeatherResponse].
     */
    @GET("v1/forecast.json")
    suspend fun getForecastWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 2,
        @Query("dt") date: String? = null,
        @Query("unixdt") unixDate: Long? = null,
        @Query("hour") hour: Int? = null,
        @Query("lang") language: String? = null,
        @Query("alerts") alerts: String? = "no",
        @Query("aqi") airQuality: String? = "no",
    ): ApiResult<ForecastWeatherResponse, Unit>
}
