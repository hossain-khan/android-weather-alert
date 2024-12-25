package dev.hossain.weatheralert.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.ui.Ui

@Composable
fun Settings(state: SettingsScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier
    ) { paddingValues ->
        SettingsContent(
            snowThreshold = state.snowThreshold,
            rainThreshold = state.rainThreshold,
            eventSink = state.eventSink,
            paddingValues = paddingValues
        )
    }
}

@Composable
fun SettingsContent(
    snowThreshold: String,
    rainThreshold: String,
    eventSink: (SettingsScreen.Event) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
        OutlinedTextField(
            value = snowThreshold,
            onValueChange = { eventSink(SettingsScreen.Event.UpdateSnowThreshold(it)) },
            label = { Text("Snow Threshold (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { eventSink(SettingsScreen.Event.SaveSnowThreshold(snowThreshold)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Snow Threshold")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = rainThreshold,
            onValueChange = { eventSink(SettingsScreen.Event.UpdateRainThreshold(it)) },
            label = { Text("Rain Threshold (mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { eventSink(SettingsScreen.Event.SaveRainThreshold(rainThreshold)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Rain Threshold")
        }
    }
}

class SettingsUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is SettingsScreen -> ui<SettingsScreen.State> { state, modifier ->
                Settings(state, modifier)
            }

            else -> null
        }
    }
}