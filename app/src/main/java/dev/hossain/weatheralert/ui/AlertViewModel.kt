package dev.hossain.weatheralert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.data.AlertTileData
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AlertViewModel(
    private val preferencesManager: PreferencesManager,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _tiles = MutableStateFlow<List<AlertTileData>>(emptyList())
    val tiles: StateFlow<List<AlertTileData>> = _tiles

    init {
        viewModelScope.launch {
            combine(
                preferencesManager.snowThreshold,
                preferencesManager.rainThreshold,
                weatherRepository.getWeatherForecastFlow(
                    // Use Toronto coordinates for now
                    latitude = 43.7,
                    longitude = -79.42,
                    apiKey = BuildConfig.WEATHER_API_KEY
                ) // Live updates from API
            ) { snowThreshold, rainThreshold, forecast ->
                val snowStatus = forecast.daily[1].snowVolume ?: 0.0
                val rainStatus = forecast.daily[1].rainVolume ?: 0.0

                listOf(
                    AlertTileData(
                        category = "Snowfall",
                        threshold = "$snowThreshold cm",
                        currentStatus = "Tomorrow: $snowStatus cm"
                    ),
                    AlertTileData(
                        category = "Rainfall",
                        threshold = "$rainThreshold mm",
                        currentStatus = "Tomorrow: $rainStatus mm"
                    )
                )
            }.collect {
                _tiles.value = it
            }
        }
    }
}
