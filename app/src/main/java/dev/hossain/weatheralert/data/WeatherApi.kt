package dev.hossain.weatheralert.data

import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/3.0/onecall")
    suspend fun getDailyForecast(
        @Query("appid") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String = "current,minutely",
        @Query("units") units: String = "metric",
    ): ApiResult<WeatherForecast, Unit>
}
