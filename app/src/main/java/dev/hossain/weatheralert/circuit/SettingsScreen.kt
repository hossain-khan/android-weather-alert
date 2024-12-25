package dev.hossain.weatheralert.circuit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(preferencesManager = PreferencesManager(context = LocalContext.current))
}