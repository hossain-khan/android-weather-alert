package dev.hossain.weatheralert.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): WeatherForecastResponse
}