package dev.hossain.weatheralert.di

import dev.hossain.weatheralert.data.ActiveWeatherService
import dev.hossain.weatheralert.data.ActiveWeatherServiceImpl
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.AnalyticsImpl
import dev.hossain.weatheralert.util.ClockProvider
import dev.hossain.weatheralert.util.DefaultClockProvider
import dev.hossain.weatheralert.util.TimeUtil
import dev.hossain.weatheralert.util.TimeUtilImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object UtilBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideActiveWeatherService(impl: ActiveWeatherServiceImpl): ActiveWeatherService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideAnalytics(impl: AnalyticsImpl): Analytics = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideClockProvider(impl: DefaultClockProvider): ClockProvider = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideTimeUtil(impl: TimeUtilImpl): TimeUtil = impl
}
