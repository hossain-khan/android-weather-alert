package dev.hossain.weatheralert.util

import com.slack.circuit.runtime.screen.Screen
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.SingleIn
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * No-op implementation of [Analytics] for F-Droid builds.
 * This ensures the app works without Firebase Analytics.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NoOpAnalytics @Inject constructor() : Analytics {
    
    override suspend fun logScreenView(screen: KClass<out Screen>) {
        // No-op for F-Droid builds
    }

    override suspend fun logWorkStarted(weatherForecastService: WeatherForecastService) {
        // No-op for F-Droid builds
    }

    override suspend fun logWorkCompleted(
        weatherForecastService: WeatherForecastService,
        notificationShown: Boolean,
    ) {
        // No-op for F-Droid builds
    }

    override suspend fun logWorkFailed(
        weatherForecastService: WeatherForecastService,
        errorCode: Long,
    ) {
        // No-op for F-Droid builds
    }

    override suspend fun logCityDetails(
        cityId: Long,
        cityName: String,
    ) {
        // No-op for F-Droid builds
    }

    override suspend fun logAddServiceApiKey(
        weatherForecastService: WeatherForecastService,
        isApiKeyAdded: Boolean,
        initiatedFromApiError: Boolean,
    ) {
        // No-op for F-Droid builds
    }

    override fun logSendFeedback() {
        // No-op for F-Droid builds
    }

    override fun logViewTutorial(isComplete: Boolean) {
        // No-op for F-Droid builds
    }

    override fun logViewServiceExternalUrl(weatherForecastService: WeatherForecastService) {
        // No-op for F-Droid builds
    }
}