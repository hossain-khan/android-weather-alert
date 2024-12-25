package dev.hossain.weatheralert.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.hossain.weatheralert.feature.alerts.components.AlertTile
import com.slack.circuit.runtime.ui.Ui
import dev.hossain.weatheralert.circuit.DetailScreen
import dev.hossain.weatheralert.core.model.AlertConfig
import dev.hossain.weatheralert.di.AppScope

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AlertListScreen::class, AppScope::class)
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