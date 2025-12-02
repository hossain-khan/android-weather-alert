package io.tomorrow.api.di

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.tomorrow.api.TomorrowIoService
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Metro DI module for Tomorrow.io service.
 * Uses @ContributesTo to automatically contribute bindings to the app graph.
 */
@ContributesTo(AppScope::class)
interface TomorrowIoModule {
    companion object {
        private const val NAMED_SERVICE_TOMORROW_IO = "TomorrowIo"

        // Unit test backdoor to allow setting base URL using mock server
        // By default, it's set to the weather service base URL.
        // Made public for test access from app module
        var tomorrowIoBaseUrl: HttpUrl = "https://api.tomorrow.io/".toHttpUrl()

        @Provides
        @SingleIn(AppScope::class)
        @Named(NAMED_SERVICE_TOMORROW_IO)
        fun provideTomorrowIoRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit
                .Builder()
                .baseUrl(tomorrowIoBaseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        @Provides
        @SingleIn(AppScope::class)
        fun provideTomorrowIoService(
            @Named(NAMED_SERVICE_TOMORROW_IO) retrofit: Retrofit,
        ): TomorrowIoService =
            retrofit
                .create(TomorrowIoService::class.java)
    }
}
