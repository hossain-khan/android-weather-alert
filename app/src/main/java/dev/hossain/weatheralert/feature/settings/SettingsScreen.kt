package dev.hossain.weatheralert.feature.settings

import dev.hossain.weatheralert.core.model.AlertConfig
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
object SettingsScreen : Screen {
    data class State(
        val snowThreshold: String,
        val rainThreshold: String,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class UpdateSnowThreshold(val value: String) : Event
        data class UpdateRainThreshold(val value: String) : Event
        data class SaveSnowThreshold(val value: String) : Event
        data class SaveRainThreshold(val value: String) : Event
    }
}