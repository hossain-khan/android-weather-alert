package dev.hossain.weatheralert.ui.devtools

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.work.scheduleOneTimeWeatherAlertWorkerDebug
import dev.hossain.weatheralert.work.scheduleWeatherAlertsWork
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Parcelize
data object WorkerTesterScreen : Screen {
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

@AssistedInject
class WorkerTesterPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
    ) : Presenter<WorkerTesterScreen.State> {
        @Composable
        override fun present(): WorkerTesterScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(WorkerTesterScreen::class)
                Timber.tag("DevPortal").d("Worker Tester opened")
            }

            return WorkerTesterScreen.State { event ->
                when (event) {
                    WorkerTesterScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(WorkerTesterScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): WorkerTesterPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(WorkerTesterScreen::class, AppScope::class)
@Composable
fun WorkerTesterScreen(
    state: WorkerTesterScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ WorkManager Tester") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(WorkerTesterScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPaddingValues ->
        WorkerTesterContent(
            snackbarHostState = snackbarHostState,
            modifier =
                modifier
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        )
    }
}

@Composable
private fun WorkerTesterContent(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe worker status
    var periodicWorkInfo by remember { mutableStateOf<WorkInfo?>(null) }
    var debugWorkInfo by remember { mutableStateOf<WorkInfo?>(null) }
    var shouldRefresh by remember { mutableStateOf(true) }

    // Poll for work status updates every 2 seconds
    LaunchedEffect(shouldRefresh) {
        while (isActive) {
            val workManager = WorkManager.getInstance(context)

            // Get periodic worker status
            val periodicWorks =
                workManager
                    .getWorkInfosForUniqueWork("WeatherAlertWork")
                    .get()
            periodicWorkInfo = periodicWorks.firstOrNull()

            // Get debug worker status
            val debugWorks =
                workManager
                    .getWorkInfosForUniqueWork("WeatherAlertWork_DEBUG")
                    .get()
            debugWorkInfo = debugWorks.firstOrNull()

            delay(2000) // Refresh every 2 seconds
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Worker Status Card
        WorkerStatusCard(
            context = context,
            periodicWorkInfo = periodicWorkInfo,
            debugWorkInfo = debugWorkInfo,
        )

        // Action Buttons Card
        ActionButtonsCard(
            context = context,
            snackbarHostState = snackbarHostState,
            onRefreshRequested = { shouldRefresh = !shouldRefresh },
        )

        // Worker Details Card
        WorkerDetailsCard(
            periodicWorkInfo = periodicWorkInfo,
            debugWorkInfo = debugWorkInfo,
        )
    }
}

@Composable
private fun WorkerStatusCard(
    context: Context,
    periodicWorkInfo: WorkInfo?,
    debugWorkInfo: WorkInfo?,
    modifier: Modifier = Modifier,
) {
    // Inject PreferencesManager through composable
    val preferencesManager = remember { PreferencesManager(context) }
    val updateInterval by preferencesManager.preferredUpdateInterval.collectAsState(initial = 12L)
    val lastCheckTime by preferencesManager.lastWeatherCheckTime.collectAsState(initial = 0L)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Worker Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            InfoRow(
                label = "Schedule",
                value = "Every $updateInterval hours",
            )

            InfoRow(
                label = "Last Execution",
                value =
                    if (lastCheckTime > 0) {
                        formatTimestamp(lastCheckTime)
                    } else {
                        "Never"
                    },
            )

            InfoRow(
                label = "Periodic Worker",
                value = periodicWorkInfo?.state?.name ?: "Not Scheduled",
            )

            InfoRow(
                label = "Debug Worker",
                value = debugWorkInfo?.state?.name ?: "Not Running",
            )

            if (periodicWorkInfo != null && periodicWorkInfo.state == WorkInfo.State.ENQUEUED) {
                val nextRunTime = calculateNextRunTime(lastCheckTime, updateInterval)
                InfoRow(
                    label = "Next Run (approx)",
                    value = formatTimestamp(nextRunTime),
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsCard(
    context: Context,
    snackbarHostState: SnackbarHostState,
    onRefreshRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    val updateInterval by preferencesManager.preferredUpdateInterval.collectAsState(initial = 12L)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Button(
                onClick = {
                    Timber.tag("DevPortal").d("Running one-time weather check worker")
                    scheduleOneTimeWeatherAlertWorkerDebug(context)
                    onRefreshRequested()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "One-time worker scheduled! Check worker status.",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Run Weather Check Now")
            }

            OutlinedButton(
                onClick = {
                    Timber.tag("DevPortal").d("Rescheduling periodic weather check worker")
                    scheduleWeatherAlertsWork(context, updateInterval)
                    onRefreshRequested()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Periodic worker rescheduled with $updateInterval hour interval",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reschedule Periodic Worker")
            }

            OutlinedButton(
                onClick = {
                    Timber.tag("DevPortal").d("Cancelling all weather check workers")
                    val workManager = WorkManager.getInstance(context)
                    workManager.cancelUniqueWork("WeatherAlertWork")
                    workManager.cancelUniqueWork("WeatherAlertWork_DEBUG")
                    onRefreshRequested()
                    scope.launch {
                        snackbarHostState.showSnackbar("All weather check workers cancelled")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel All Workers")
            }

            OutlinedButton(
                onClick = {
                    onRefreshRequested()
                    scope.launch {
                        snackbarHostState.showSnackbar("Status refreshed")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Refresh Status")
            }
        }
    }
}

@Composable
private fun WorkerDetailsCard(
    periodicWorkInfo: WorkInfo?,
    debugWorkInfo: WorkInfo?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Worker Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // Periodic Worker Details
            Text(
                text = "Periodic Worker",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (periodicWorkInfo != null) {
                InfoRow(
                    label = "ID",
                    value = periodicWorkInfo.id.toString().take(16) + "...",
                )
                InfoRow(
                    label = "State",
                    value = periodicWorkInfo.state.name,
                )
                InfoRow(
                    label = "Run Attempt",
                    value = periodicWorkInfo.runAttemptCount.toString(),
                )
                InfoRow(
                    label = "Tags",
                    value = periodicWorkInfo.tags.take(2).joinToString(", "),
                )
            } else {
                Text(
                    text = "Not scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Debug Worker Details
            Text(
                text = "Debug Worker",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (debugWorkInfo != null) {
                InfoRow(
                    label = "ID",
                    value = debugWorkInfo.id.toString().take(16) + "...",
                )
                InfoRow(
                    label = "State",
                    value = debugWorkInfo.state.name,
                )
                InfoRow(
                    label = "Run Attempt",
                    value = debugWorkInfo.runAttemptCount.toString(),
                )

                if (debugWorkInfo.state == WorkInfo.State.FAILED) {
                    InfoRow(
                        label = "Stop Reason",
                        value = debugWorkInfo.stopReason.toString(),
                    )
                }
            } else {
                Text(
                    text = "Not running",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0) return "Never"

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> {
            "Just now"
        }

        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        }

        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours hour${if (hours != 1L) "s" else ""} ago"
        }

        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

private fun calculateNextRunTime(
    lastCheckTime: Long,
    intervalHours: Long,
): Long =
    if (lastCheckTime > 0) {
        lastCheckTime + TimeUnit.HOURS.toMillis(intervalHours)
    } else {
        System.currentTimeMillis() + TimeUnit.HOURS.toMillis(intervalHours)
    }

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
fun WorkerTesterScreenPreview() {
    val sampleState =
        WorkerTesterScreen.State(
            eventSink = {},
        )
    WeatherAlertAppTheme {
        WorkerTesterScreen(state = sampleState)
    }
}
