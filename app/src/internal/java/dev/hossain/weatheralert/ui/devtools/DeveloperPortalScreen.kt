package dev.hossain.weatheralert.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data object DeveloperPortalScreen : Screen {
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()

        data object OpenAlertSimulator : Event()

        data object OpenHistorySimulator : Event()

        data object OpenNotificationTester : Event()

        data object OpenWorkerTester : Event()
    }
}

@AssistedInject
class DeveloperPortalPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
    ) : Presenter<DeveloperPortalScreen.State> {
        @Composable
        override fun present(): DeveloperPortalScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(DeveloperPortalScreen::class)
                Timber.tag("DevPortal").d("Developer Portal opened")
            }

            return DeveloperPortalScreen.State { event ->
                when (event) {
                    DeveloperPortalScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    DeveloperPortalScreen.Event.OpenAlertSimulator -> {
                        navigator.goTo(AlertSimulatorScreen)
                    }

                    DeveloperPortalScreen.Event.OpenHistorySimulator -> {
                        // TODO: Navigate to History Simulator (Phase 2)
                        Timber.tag("DevPortal").d("History Simulator - Coming Soon")
                    }

                    DeveloperPortalScreen.Event.OpenNotificationTester -> {
                        navigator.goTo(NotificationTesterScreen)
                    }

                    DeveloperPortalScreen.Event.OpenWorkerTester -> {
                        navigator.goTo(WorkerTesterScreen)
                    }
                }
            }
        }

        @CircuitInject(DeveloperPortalScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): DeveloperPortalPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DeveloperPortalScreen::class, AppScope::class)
@Composable
fun DeveloperPortalScreen(
    state: DeveloperPortalScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔧 Developer Portal") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(DeveloperPortalScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
            )
        },
    ) { contentPaddingValues ->
        DeveloperPortalContent(
            state = state,
            modifier = modifier.padding(contentPaddingValues),
        )
    }
}

@Composable
private fun DeveloperPortalContent(
    state: DeveloperPortalScreen.State,
    modifier: Modifier = Modifier,
) {
    val tools =
        listOf(
            DevTool(
                icon = "📍",
                title = "Alert Simulator",
                description = "Create test alerts for different cities and conditions",
                onClick = { state.eventSink(DeveloperPortalScreen.Event.OpenAlertSimulator) },
            ),
            DevTool(
                icon = "📜",
                title = "History Simulator",
                description = "Populate alert history with test data",
                onClick = { state.eventSink(DeveloperPortalScreen.Event.OpenHistorySimulator) },
            ),
            DevTool(
                icon = "🔔",
                title = "Notification Tester",
                description = "Test notification appearance and actions",
                onClick = { state.eventSink(DeveloperPortalScreen.Event.OpenNotificationTester) },
            ),
            DevTool(
                icon = "⚙️",
                title = "Worker Testing",
                description = "Trigger and monitor background workers",
                onClick = { state.eventSink(DeveloperPortalScreen.Event.OpenWorkerTester) },
            ),
        )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(MaterialTheme.dimensions.horizontalScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(tools) { tool ->
            DevToolCard(tool = tool)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevToolCard(
    tool: DevTool,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = tool.onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = tool.icon,
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class DevTool(
    val icon: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun DeveloperPortalScreenPreview() {
    val sampleState =
        DeveloperPortalScreen.State(
            eventSink = {},
        )
    WeatherAlertAppTheme {
        DeveloperPortalScreen(state = sampleState)
    }
}
