package dev.hossain.weatheralert.di

import android.content.Context
import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Module
import dagger.Provides
import io.tomorrow.api.TomorrowIoService
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.openweathermap.api.OpenWeatherService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Named

@Module
@ContributesTo(AppScope::class)
object NetworkModule {
    private const val NAMED_SERVICE_OPEN_WEATHER = "OpenWeather"
    private const val NAMED_SERVICE_TOMORROW_IO = "TomorrowIo"

    // Unit test backdoor to allow setting base URL using mock server
    // By default, it's set weather service base URL.
    internal var openWeatherBaseUrl: HttpUrl = "https://api.openweathermap.org/".toHttpUrl()
    internal var tomorrowIoBaseUrl: HttpUrl = "https://api.tomorrow.io/".toHttpUrl()

    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, cacheSize.toLong())

        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        return OkHttpClient
            .Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
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
    fun provideOpenWeatherService(
        @Named(NAMED_SERVICE_OPEN_WEATHER) retrofit: Retrofit,
    ): OpenWeatherService =
        retrofit
            .create(OpenWeatherService::class.java)

    @Provides
    @SingleIn(AppScope::class)
    fun provideTomorrowIoService(
        @Named(NAMED_SERVICE_TOMORROW_IO) retrofit: Retrofit,
    ): TomorrowIoService =
        retrofit
            .create(TomorrowIoService::class.java)
}
