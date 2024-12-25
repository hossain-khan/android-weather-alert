package dev.hossain.weatheralert.feature.alerts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.model.AlertConfig
import dev.hossain.weatheralert.core.network.WeatherRepository
import dev.hossain.weatheralert.feature.alerts.AlertListScreen.Event
import dev.hossain.weatheralert.feature.alerts.AlertListScreen.State
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.di.AppScope
import kotlinx.coroutines.launch


class AlertListPresenter @AssistedInject constructor(
    private val weatherRepository: WeatherRepository,
    private val dataStore: AlertConfigDataStore
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()
        val alertConfigs by dataStore.getAlertConfigs().collectAsRetainedState(emptyList())
        var isLoading by remember { mutableStateOf(false) }
        val forecastData = remember { mutableStateMapOf<AlertConfig, Double?>() }

        LaunchedEffect(alertConfigs) {
            if (alertConfigs.isNotEmpty()) {
                isLoading = true
                alertConfigs.forEach { config ->
                    launch {
                        // Replace with actual lat, lon, and API key
                        val lat = 37.7749
                        val lon = -122.4194
                        val apiKey = "YOUR_API_KEY"

                        try {
                            val weatherData = weatherRepository.getWeatherData(lat, lon, apiKey)
                            val forecast = when (config.category) {
                                is AlertCategory.Snow -> weatherData.daily.firstOrNull()?.snow
                                is AlertCategory.Rain -> weatherData.daily.firstOrNull()?.rain
                            }
                            forecastData[config] = forecast
                        } catch (e: Exception) {
                            // Handle errors, maybe update the state with an error message
                            forecastData[config] = null
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        }

        return State(
            alerts = alertConfigs,
            isLoading = isLoading,
            forecastData = forecastData,
            eventSink = { event ->
                when (event) {
                    is Event.Delete -> {
                        coroutineScope.launch {
                            dataStore.deleteAlertConfig(event.alert.category)
                        }
                    }
                }
            }
        )
    }

    @CircuitInject(AlertListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(): AlertListPresenter
    }
}