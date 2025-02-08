package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
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

@Module
@ContributesTo(AppScope::class)
interface TestModule {
    @Binds
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    fun bindApiKey(impl: ApiKeyProviderImpl): ApiKeyProvider

    @Binds
    fun bindActiveWeatherService(impl: ActiveWeatherServiceImpl): ActiveWeatherService

    @Binds
    fun bindTimeUtil(impl: TimeUtilImpl): TimeUtil

    @Binds
    fun bindClockProvider(impl: DefaultClockProvider): ClockProvider

    @Binds
    fun bindAnalytics(impl: AnalyticsImpl): Analytics

    companion object {
        @Provides
        fun providePreferencesManager(
            @ApplicationContext context: Context,
        ): PreferencesManager = PreferencesManager(context)

        @Provides
        fun provideFirebaseAnalytics(
            @ApplicationContext context: Context,
        ): FirebaseAnalytics {
            FirebaseApp.initializeApp(context)
            return Firebase.analytics
        }
    }
}
