package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.data.ActiveWeatherService
import dev.hossain.weatheralert.data.ActiveWeatherServiceImpl
import dev.hossain.weatheralert.data.ApiKey
import dev.hossain.weatheralert.data.ApiKeyImpl
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherRepositoryImpl
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.AnalyticsImpl
import dev.hossain.weatheralert.util.TimeUtil
import dev.hossain.weatheralert.util.TimeUtilImpl
import org.mockito.Mockito

@Module
@ContributesTo(AppScope::class)
interface TestModule {
    @Binds
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    fun bindApiKey(impl: ApiKeyImpl): ApiKey

    @Binds
    fun bindActiveWeatherService(impl: ActiveWeatherServiceImpl): ActiveWeatherService

    @Binds
    fun bindTimeUtil(impl: TimeUtilImpl): TimeUtil

    @Binds
    fun bindAnalytics(impl: AnalyticsImpl): Analytics

    companion object {
        @Provides
        fun providePreferencesManager(
            @ApplicationContext context: Context,
        ): PreferencesManager = PreferencesManager(context)

        @Provides
        fun provideFirebaseAnalytics(): FirebaseAnalytics = Mockito.mock(FirebaseAnalytics::class.java)
    }
}
