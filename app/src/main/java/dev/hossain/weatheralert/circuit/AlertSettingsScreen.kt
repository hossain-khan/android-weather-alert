package dev.hossain.weatheralert.circuit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.di.AppScope
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlertSettingsScreen(
    val requestId: String,
) : Screen {
    data class State(
        val snowThreshold: Float,
        val rainThreshold: Float,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class SnowThresholdChanged(val value: Float) : Event()
        data class RainThresholdChanged(val value: Float) : Event()
        data object SaveSettingsClicked : Event()
    }
}

class AlertSettingsPresenter
@AssistedInject
constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: AlertSettingsScreen,
    private val preferencesManager: PreferencesManager,
) : Presenter<AlertSettingsScreen.State> {
    @Composable
    override fun present(): AlertSettingsScreen.State {
        // State variables to hold threshold values
        val snowThreshold by preferencesManager.snowThreshold.collectAsState(initial = 5.0f)
        val rainThreshold by preferencesManager.rainThreshold.collectAsState(initial = 2.0f)

        var updatedSnowThreshold by remember { mutableStateOf(snowThreshold) }
        var updatedRainThreshold by remember { mutableStateOf(rainThreshold) }

        return AlertSettingsScreen.State(updatedSnowThreshold, updatedRainThreshold) { event ->
            when (event) {
                AlertSettingsScreen.Event.SaveSettingsClicked -> TODO()
                is AlertSettingsScreen.Event.RainThresholdChanged -> {
                    updatedRainThreshold = event.value
                }
                is AlertSettingsScreen.Event.SnowThresholdChanged -> {
                    updatedSnowThreshold = event.value
                }
            }
        }
    }

    @CircuitInject(AlertSettingsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: Navigator,
            screen: AlertSettingsScreen,
        ): AlertSettingsPresenter
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AlertSettingsScreen::class, AppScope::class)
@Composable
fun AlertSettingsScreen(state: AlertSettingsScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configure Alerts") })
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Snow Threshold Slider
            Text(text = "Snowfall Threshold: ${"%.1f".format(state.snowThreshold)} cm")
            Slider(
                value = state.snowThreshold,
                onValueChange = {
                    state.eventSink(AlertSettingsScreen.Event.SnowThresholdChanged(it))
                },
                valueRange = 1f..20f,
                steps = 20
            )

            // Rain Threshold Slider
            Text(text = "Rainfall Threshold: ${"%.1f".format(state.rainThreshold)} mm")
            Slider(
                value = state.rainThreshold,
                onValueChange = {
                    state.eventSink(AlertSettingsScreen.Event.RainThresholdChanged(it))
                },
                valueRange = 1f..20f,
                steps = 20
            )

            Button(
                onClick = {
                    state.eventSink(AlertSettingsScreen.Event.SaveSettingsClicked)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}

/**
 * Interactive Alert Threshold Adjustments
 *
 *     Purpose: Allow users to adjust alert thresholds in a fun and intuitive way.
 *     Implementation: Use a slider or a rotary dial for thresholds.
 *         Add haptic feedback for user interaction.
 *         Animate the slider's color based on the value range (e.g., blue for low, red for high).
 */
@Composable
fun ThresholdSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    max: Float
) {
    val color by animateColorAsState(
        if (value < max / 2) Color.Blue else Color.Red
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$label: ${value.toInt()} cm", color = color)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..max,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
    }
}



@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AlertSettingsScreen(
        AlertSettingsScreen.State(snowThreshold = 5.0f, rainThreshold = 10.0f) {}
    )
}