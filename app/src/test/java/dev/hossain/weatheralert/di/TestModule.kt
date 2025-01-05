package dev.hossain.weatheralert.di

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherRepositoryImpl
import dev.hossain.weatheralert.di.AppScope

@Module
@ContributesTo(AppScope::class)
interface TestModule {
    @Binds
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    companion object {
        @Provides
        fun providePreferencesManager(
            @ApplicationContext context: Context,
        ): PreferencesManager = PreferencesManager(context)
    }
}
