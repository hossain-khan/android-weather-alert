package org.openweathermap.api.di

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.openweathermap.api.OpenWeatherService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Metro DI module for OpenWeather service.
 * Uses @ContributesTo to automatically contribute bindings to the app graph.
 */
@ContributesTo(AppScope::class)
interface OpenWeatherModule {
    companion object {
        private const val NAMED_SERVICE_OPEN_WEATHER = "OpenWeather"

        // Unit test backdoor to allow setting base URL using mock server
        // By default, it's set to the weather service base URL.
        // Made public for test access from app module
        var openWeatherBaseUrl: HttpUrl = "https://api.openweathermap.org/".toHttpUrl()

        @Provides
        @SingleIn(AppScope::class)
        @Named(NAMED_SERVICE_OPEN_WEATHER)
        fun provideOpenWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit
                .Builder()
                .baseUrl(openWeatherBaseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        @Provides
        @SingleIn(AppScope::class)
        fun provideOpenWeatherService(
            @Named(NAMED_SERVICE_OPEN_WEATHER) retrofit: Retrofit,
        ): OpenWeatherService =
            retrofit
                .create(OpenWeatherService::class.java)
    }
}
