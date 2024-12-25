package dev.hossain.weatheralert.feature.alerts

import dev.hossain.weatheralert.core.model.AlertConfig
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
object AlertListScreen : Screen {
    data class State(
        val alerts: List<AlertConfig>,
        val isLoading: Boolean,
        val forecastData: Map<AlertConfig, Double?>,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class Delete(val alert: AlertConfig) : Event
    }
}