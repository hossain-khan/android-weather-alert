package dev.hossain.weatheralert.data

import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/3.0/onecall")
    suspend fun getDailyForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String = "current,minutely,hourly",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String,
    ): ApiResult<WeatherForecast, Unit>
}
