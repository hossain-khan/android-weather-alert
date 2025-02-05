package dev.hossain.weatheralert.data

import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject

/**
 * Interface for providing the active weather service based on user preference.
 */
interface ActiveWeatherService {
    fun selectedService(): WeatherForecastService
}

/**
 * Implementation of the [ActiveWeatherService] interface.
 * This class provides the selected weather service based on user preference.
 */
@ContributesBinding(AppScope::class)
class ActiveWeatherServiceImpl
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ActiveWeatherService {
        override fun selectedService(): WeatherForecastService = preferencesManager.preferredWeatherForecastServiceSync
    }
