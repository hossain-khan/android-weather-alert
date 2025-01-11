package org.openweathermap.api

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Builder for [OpenWeatherService] for testing.
 */
object OpenWeatherServiceBuilder {
    private val okHttpClient = OkHttpClient.Builder().build()

    fun provideService(baseUrl: HttpUrl): OpenWeatherService {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        return retrofit.create(OpenWeatherService::class.java)
    }
}
