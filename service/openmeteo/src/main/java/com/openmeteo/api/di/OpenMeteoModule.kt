package com.openmeteo.api.di

import com.openmeteo.api.OpenMeteoService
import com.openmeteo.api.OpenMeteoServiceImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Metro DI module for OpenMeteo service.
 * Uses @ContributesTo to automatically contribute bindings to the app graph.
 */
@ContributesTo(AppScope::class)
interface OpenMeteoModule {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideOpenMeteoService(): OpenMeteoService = OpenMeteoServiceImpl()
    }
}
