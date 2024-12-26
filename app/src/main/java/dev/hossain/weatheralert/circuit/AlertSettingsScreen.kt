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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(preferencesManager: PreferencesManager) {
    val scope = rememberCoroutineScope()

    // State variables to hold threshold values
    val snowThreshold by preferencesManager.snowThreshold.collectAsState(initial = 5.0f)
    val rainThreshold by preferencesManager.rainThreshold.collectAsState(initial = 10.0f)

    var updatedSnowThreshold by remember { mutableStateOf(snowThreshold) }
    var updatedRainThreshold by remember { mutableStateOf(rainThreshold) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configure Alerts") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Snow Threshold Slider
            Text(text = "Snowfall Threshold: ${"%.1f".format(updatedSnowThreshold)} cm")
            Slider(
                value = updatedSnowThreshold,
                onValueChange = { updatedSnowThreshold = it },
                valueRange = 0f..50f,
                steps = 10
            )

            // Rain Threshold Slider
            Text(text = "Rainfall Threshold: ${"%.1f".format(updatedRainThreshold)} mm")
            Slider(
                value = updatedRainThreshold,
                onValueChange = { updatedRainThreshold = it },
                valueRange = 0f..50f,
                steps = 10
            )

            Button(
                onClick = {
                    scope.launch {
                        preferencesManager.updateSnowThreshold(updatedSnowThreshold)
                        preferencesManager.updateRainThreshold(updatedRainThreshold)
                    }
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
    SettingsScreen(preferencesManager = PreferencesManager(context = LocalContext.current))
}