package dev.hossain.weatheralert.di

import android.content.Context
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

/**
 * Network-related dependency bindings.
 * Service-specific bindings have been moved to their respective modules using @ContributesTo.
 */
@BindingContainer
object NetworkBindings {
    @Provides
    fun provideOkHttpClient(context: Context): OkHttpClient {
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
}
