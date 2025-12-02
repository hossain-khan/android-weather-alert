package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dev.hossain.weatheralert.data.ActiveWeatherService
import dev.hossain.weatheralert.data.ActiveWeatherServiceImpl
import dev.hossain.weatheralert.data.ApiKeyProvider
import dev.hossain.weatheralert.data.ApiKeyProviderImpl
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherRepositoryImpl
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.AnalyticsImpl
import dev.hossain.weatheralert.util.ClockProvider
import dev.hossain.weatheralert.util.DefaultClockProvider
import dev.hossain.weatheralert.util.TimeUtil
import dev.hossain.weatheralert.util.TimeUtilImpl
import dev.zacsweers.metro.Provides

interface TestModule {
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    fun bindApiKey(impl: ApiKeyProviderImpl): ApiKeyProvider

    fun bindActiveWeatherService(impl: ActiveWeatherServiceImpl): ActiveWeatherService

    fun bindTimeUtil(impl: TimeUtilImpl): TimeUtil

    fun bindClockProvider(impl: DefaultClockProvider): ClockProvider

    fun bindAnalytics(impl: AnalyticsImpl): Analytics

    companion object {
        @Provides
        fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
            FirebaseApp.initializeApp(context)
            return Firebase.analytics
        }
    }
}
