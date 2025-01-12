package dev.hossain.weatheralert.data

import androidx.annotation.Keep
import com.squareup.anvil.annotations.ContributesBinding
import dev.hossain.weatheralert.di.AppScope
import io.tomorrow.api.TomorrowIoService
import org.openweathermap.api.OpenWeatherService
import javax.inject.Inject

/**
 * List of supported weather services.
 */
@Keep
enum class WeatherService {
    /**
     * OpenWeatherMap API for weather forecast.
     * - https://openweathermap.org/api
     *
     * @see OpenWeatherService
     */
    OPEN_WEATHER_MAP,

    /**
     * Tomorrow.io API for weather forecast.
     * - https://app.tomorrow.io/home
     *
     * @see TomorrowIoService
     */
    TOMORROW_IO,
}

interface ActiveWeatherService {
    fun selectedService(): WeatherService
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
        override fun selectedService(): WeatherService = preferencesManager.preferredWeatherServiceSync
    }
