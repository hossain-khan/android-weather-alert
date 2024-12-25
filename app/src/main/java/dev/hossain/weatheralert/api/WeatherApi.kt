package dev.hossain.weatheralert.api

import dev.hossain.weatheralert.data.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/onecall")
    suspend fun getDailyForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String = "current,minutely,hourly",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherForecast
}