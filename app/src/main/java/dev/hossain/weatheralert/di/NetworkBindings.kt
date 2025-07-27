package dev.hossain.weatheralert.di

import android.content.Context
import com.openmeteo.api.OpenMeteoService
import com.openmeteo.api.OpenMeteoServiceImpl
import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import com.weatherapi.api.WeatherApiService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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

@BindingContainer
object NetworkBindings {
    private const val NAMED_SERVICE_OPEN_WEATHER = "OpenWeather"
    private const val NAMED_SERVICE_TOMORROW_IO = "TomorrowIo"
    private const val NAMED_SERVICE_WEATHERAPI = "WeatherApi"

    // Unit test backdoor to allow setting base URL using mock server
    // By default, it's set weather service base URL.
    internal var openWeatherBaseUrl: HttpUrl = "https://api.openweathermap.org/".toHttpUrl()
    internal var tomorrowIoBaseUrl: HttpUrl = "https://api.tomorrow.io/".toHttpUrl()
    internal var weatherApiBaseUrl: HttpUrl = "https://api.weatherapi.com/".toHttpUrl()

    @Provides
    fun provideOkHttpClient(
        context: Context,
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
            // Added to simulate and test with static forecast response
            // .addInterceptor(SimulatedResponseInterceptor(context))
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

    @Provides
    @SingleIn(AppScope::class)
    fun provideWeatherApiService(
        @Named(NAMED_SERVICE_WEATHERAPI) retrofit: Retrofit,
    ): WeatherApiService =
        retrofit
            .create(WeatherApiService::class.java)

    @Provides
    @SingleIn(AppScope::class)
    fun provideOpenMeteoService(): OpenMeteoService = OpenMeteoServiceImpl()
}
