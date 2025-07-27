package dev.hossain.weatheralert.di

import dev.hossain.weatheralert.data.ApiKeyProvider
import dev.hossain.weatheralert.data.ApiKeyProviderImpl
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherRepositoryImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object WeatherRepositoryBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideApiKeyProvider(impl: ApiKeyProviderImpl): ApiKeyProvider = impl
}
