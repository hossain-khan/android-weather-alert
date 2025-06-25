package dev.hossain.weatheralert.di

import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

/**
 * F-Droid specific app module that excludes Firebase dependencies.
 * This ensures F-Droid builds work without proprietary Google services.
 */
@ContributesTo(AppScope::class)
@Module
class FDroidAppModule {
    // Empty module - Analytics is provided by NoOpAnalytics binding
    // No Firebase dependencies for F-Droid builds
}