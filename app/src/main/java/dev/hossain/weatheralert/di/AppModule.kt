package dev.hossain.weatheralert.di

import com.squareup.anvil.annotations.ContributesTo
import dev.hossain.weatheralert.data.RetrofitClient
import dev.hossain.weatheralert.data.WeatherApi

@ContributesTo(AppScope::class)
@dagger.Module
class AppModule {
    fun providesWeatherApi(): WeatherApi = RetrofitClient.weatherApi
}
