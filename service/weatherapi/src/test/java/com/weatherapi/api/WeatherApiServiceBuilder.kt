package com.weatherapi.api

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Builder for [WeatherApiService] for testing.
 */
object WeatherApiServiceBuilder {
    private val okHttpClient = OkHttpClient.Builder().build()

    fun provideWeatherApiService(baseUrl: HttpUrl): WeatherApiService {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        return retrofit.create(WeatherApiService::class.java)
    }
}
