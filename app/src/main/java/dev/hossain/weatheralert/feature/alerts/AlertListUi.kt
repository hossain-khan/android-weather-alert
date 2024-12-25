package dev.hossain.weatheralert.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.feature.alerts.components.AlertTile
import com.slack.circuit.runtime.ui.Ui

@Composable
fun AlertList(state: AlertListScreen.State, modifier: Modifier = Modifier) {

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Alerts") })
        },
        modifier = modifier
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AlertListContent(
                alerts = state.alerts,
                forecastData = state.forecastData,
                eventSink = state.eventSink,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
fun AlertListContent(
    alerts: List<AlertConfig>,
    forecastData: Map<AlertConfig, Double?>,
    eventSink: (AlertListScreen.Event) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(alerts) { alertConfig ->
            AlertTile(
                alertConfig = alertConfig,
                forecast = forecastData[alertConfig],
                onDelete = { eventSink(AlertListScreen.Event.Delete(alertConfig)) }
            )
        }
    }
}

class AlertListUiFactory @Inject constructor() : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is AlertListScreen -> ui<AlertListScreen.State> { state, modifier ->
                AlertList(state, modifier)
            }

            else -> null
        }
    }
}