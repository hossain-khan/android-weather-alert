package com.weatherapi.api.di

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import com.weatherapi.api.WeatherApiService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Metro DI module for WeatherAPI service.
 * Uses @ContributesTo to automatically contribute bindings to the app graph.
 */
@ContributesTo(AppScope::class)
interface WeatherApiModule {
    companion object {
        private const val NAMED_SERVICE_WEATHERAPI = "WeatherApi"

        // Unit test backdoor to allow setting base URL using mock server
        // By default, it's set to the weather service base URL.
        // Made public for test access from app module
        var weatherApiBaseUrl: HttpUrl = "https://api.weatherapi.com/".toHttpUrl()

        @Provides
        @SingleIn(AppScope::class)
        @Named(NAMED_SERVICE_WEATHERAPI)
        fun provideWeatherApiRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit
                .Builder()
                .baseUrl(weatherApiBaseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        @Provides
        @SingleIn(AppScope::class)
        fun provideWeatherApiService(
            @Named(NAMED_SERVICE_WEATHERAPI) retrofit: Retrofit,
        ): WeatherApiService =
            retrofit
                .create(WeatherApiService::class.java)
    }
}
