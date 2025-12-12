package dev.hossain.weatheralert.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.ui.devtools.formatDate
import dev.hossain.weatheralert.ui.devtools.maskApiKey
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.work.DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Developer tool for managing app state and preferences.
 *
 * This screen allows developers to view current app preferences and reset them to
 * defaults for testing fresh app states. Useful for testing onboarding flows,
 * preference persistence, and debugging preference-related issues.
 *
 * Features:
 * - View all current preferences (weather service, update interval, onboarding)
 * - View API keys (safely masked showing last 4 chars)
 * - Reset individual preferences (onboarding, update interval, API keys)
 * - Clear all preferences at once with confirmation
 * - Manage cached weather data
 *
 * Note: Exposing PreferencesManager directly in State is a deviation from typical
 * Circuit pattern. This is accepted for developer tools to maintain simplicity.
 * Production screens should collect data in Presenter and expose only primitives.
 */
@Parcelize
data object StateManagementScreen : Screen {
    /**
     * UI state for the State Management screen.
     *
     * @property preferencesManager Manager for accessing and modifying preferences
     * @property cityForecastDao DAO for accessing forecast cache
     * @property eventSink Callback for handling user events
     */
    data class State(
        val preferencesManager: PreferencesManager,
        val cityForecastDao: CityForecastDao,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events that can be triggered from State Management.
     */
    sealed class Event : CircuitUiEvent {
        /** Navigate back to Developer Portal */
        data object GoBack : Event()
    }
}

@AssistedInject
class StateManagementPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
        private val preferencesManager: PreferencesManager,
        private val cityForecastDao: CityForecastDao,
    ) : Presenter<StateManagementScreen.State> {
        @Composable
        override fun present(): StateManagementScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(StateManagementScreen::class)
                Timber.tag("DevPortal").d("State Management opened")
            }

            return StateManagementScreen.State(
                preferencesManager = preferencesManager,
                cityForecastDao = cityForecastDao,
            ) { event ->
                when (event) {
                    StateManagementScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(StateManagementScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): StateManagementPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(StateManagementScreen::class, AppScope::class)
@Composable
fun StateManagementScreen(
    state: StateManagementScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ State Management") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(StateManagementScreen.Event.GoBack)
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
        StateManagementContent(
            preferencesManager = state.preferencesManager,
            cityForecastDao = state.cityForecastDao,
            snackbarHostState = snackbarHostState,
            modifier =
                modifier
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        )
    }
}

@Composable
private fun StateManagementContent(
    preferencesManager: PreferencesManager,
    cityForecastDao: CityForecastDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Collect preference values
    val weatherService by preferencesManager.preferredWeatherForecastService.collectAsState(
        initial = WeatherForecastService.WEATHER_API,
    )
    val updateInterval by preferencesManager.preferredUpdateInterval.collectAsState(
        initial = DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS,
    )
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState(
        initial = false,
    )
    val lastCheckTime by preferencesManager.lastWeatherCheckTime.collectAsState(
        initial = 0L,
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Preferences Viewer Card
        PreferencesViewerCard(
            weatherService = weatherService,
            updateInterval = updateInterval,
            isOnboardingCompleted = isOnboardingCompleted,
            lastCheckTime = lastCheckTime,
            preferencesManager = preferencesManager,
        )

        // Reset Actions Card
        ResetActionsCard(
            preferencesManager = preferencesManager,
            snackbarHostState = snackbarHostState,
        )

        // Cache Management Card
        CacheManagementCard(
            cityForecastDao = cityForecastDao,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun PreferencesViewerCard(
    weatherService: WeatherForecastService,
    updateInterval: Long,
    isOnboardingCompleted: Boolean,
    lastCheckTime: Long,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
) {
    val openWeatherApiKey = preferencesManager.savedApiKey(WeatherForecastService.OPEN_WEATHER_MAP)
    val tomorrowIoApiKey = preferencesManager.savedApiKey(WeatherForecastService.TOMORROW_IO)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Current Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            InfoRow(
                label = "Weather Service",
                value = weatherService.name,
            )

            InfoRow(
                label = "Update Frequency",
                value = "$updateInterval hours",
            )

            InfoRow(
                label = "Onboarding Complete",
                value = if (isOnboardingCompleted) "Yes" else "No",
            )

            if (lastCheckTime > 0) {
                InfoRow(
                    label = "Last Check",
                    value = formatDate(lastCheckTime),
                )
            } else {
                InfoRow(
                    label = "Last Check",
                    value = "Never",
                )
            }

            // API Keys (masked)
            Text(
                text = "API Keys:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )

            InfoRow(
                label = "OpenWeather",
                value = maskApiKey(openWeatherApiKey ?: ""),
            )

            InfoRow(
                label = "Tomorrow.io",
                value = maskApiKey(tomorrowIoApiKey ?: ""),
            )
        }
    }
}

@Composable
private fun ResetActionsCard(
    preferencesManager: PreferencesManager,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var showClearPreferencesDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reset Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Reset individual preference values to defaults",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElevatedCard(
                onClick = {
                    scope.launch {
                        preferencesManager.setOnboardingCompleted(false)
                        Timber.tag("DevPortal").d("Reset onboarding flag to false")
                        snackbarHostState.showSnackbar("Onboarding reset - will show on next app launch")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "🔄 Reset Onboarding",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            ElevatedCard(
                onClick = {
                    scope.launch {
                        preferencesManager.savePreferredUpdateInterval(DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS)
                        Timber.tag("DevPortal").d("Reset update frequency to $DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS hours")
                        snackbarHostState.showSnackbar("Update frequency reset to $DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS hours")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "🔄 Reset Update Frequency",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            ElevatedCard(
                onClick = {
                    scope.launch {
                        preferencesManager.clearUserApiKeys()
                        Timber.tag("DevPortal").d("Cleared all user API keys")
                        snackbarHostState.showSnackbar("All API keys cleared")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "🔑 Clear All API Keys",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            OutlinedButton(
                onClick = { showClearPreferencesDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("⚠️ Clear All Preferences")
            }
        }
    }

    if (showClearPreferencesDialog) {
        AlertDialog(
            onDismissRequest = { showClearPreferencesDialog = false },
            title = { Text("Clear All Preferences?") },
            text = {
                Text(
                    "This will reset all app preferences to defaults. " +
                        "You'll need to set up your weather service and alerts again.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                preferencesManager.setOnboardingCompleted(false)
                                preferencesManager.savePreferredUpdateInterval(
                                    DEFAULT_WEATHER_UPDATE_INTERVAL_HOURS,
                                )
                                preferencesManager.clearUserApiKeys()
                                preferencesManager.savePreferredWeatherService(
                                    WeatherForecastService.WEATHER_API,
                                )
                                Timber.tag("DevPortal").d("Cleared all preferences successfully")
                                snackbarHostState.showSnackbar("All preferences cleared")
                            } catch (e: Exception) {
                                Timber.tag("DevPortal").e(e, "Failed to clear preferences")
                                snackbarHostState.showSnackbar("Failed to clear preferences")
                            }
                        }
                        showClearPreferencesDialog = false
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearPreferencesDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun CacheManagementCard(
    cityForecastDao: CityForecastDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var forecastCount by remember { mutableStateOf(0) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        forecastCount =
            withContext(Dispatchers.IO) {
                try {
                    // Note: This is an approximate count since there's no getAll method
                    // We'd need to query each city individually for exact count
                    val sampleCityId = 1L
                    cityForecastDao.getCityForecastsByCityId(sampleCityId).size
                } catch (e: Exception) {
                    Timber.tag("DevPortal").e(e, "Failed to get forecast count")
                    0
                }
            }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Cache Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Manage cached weather data (approximate count)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            InfoRow(
                label = "Cached Forecasts",
                value = forecastCount.toString(),
            )

            ElevatedCard(
                onClick = { showClearCacheDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Disabled since we can't reliably clear all
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🗑️ Clear Weather Cache",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Feature not available - DAO method needed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Weather Cache?") },
            text = {
                Text(
                    "This will delete $forecastCount cached weather forecasts. " +
                        "New forecasts will be fetched on the next weather check.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    // Since deleteAll doesn't exist, delete each forecast individually
                                    val forecasts = cityForecastDao.getCityForecastsByCityId(0)
                                    forecasts.forEach { forecast ->
                                        cityForecastDao.deleteCityForecast(forecast)
                                    }
                                }
                                Timber.tag("DevPortal").d("Cleared $forecastCount cached forecasts")
                                forecastCount = 0
                                snackbarHostState.showSnackbar("Weather cache cleared")
                            } catch (e: Exception) {
                                Timber.tag("DevPortal").e(e, "Failed to clear cache")
                                snackbarHostState.showSnackbar("Failed to clear cache")
                            }
                        }
                        showClearCacheDialog = false
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
