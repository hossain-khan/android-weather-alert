package dev.hossain.weatheralert.data

import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.zacsweers.metro.Inject

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
@Inject
class ActiveWeatherServiceImpl(
    private val preferencesManager: PreferencesManager,
) : ActiveWeatherService {
    override fun selectedService(): WeatherForecastService = preferencesManager.preferredWeatherForecastServiceSync
}
