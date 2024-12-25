package dev.hossain.weatheralert.core.network

import dev.hossain.weatheralert.core.model.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {
    @GET("data/3.0/onecall")
    suspend fun getOneCallWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,hourly,current",
        @Query("appid") appId: String,
        @Query("units") units: String = "metric" // Use metric for cm and mm
    ): WeatherData
}