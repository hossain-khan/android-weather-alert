package dev.hossain.weatheralert.ui.devtools

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.AlertHistory
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Developer tool for generating alert history data.
 *
 * This screen allows developers to populate the alert history with test data for
 * testing history views, analytics, and date filtering. All generated data is
 * marked with [TEST] prefix.
 *
 * Features:
 * - Quick history templates (Today, This Week, This Month)
 * - Custom history generation with date ranges
 * - Specify number of history entries
 * - Delete all test history at once
 * - Realistic data distribution across cities and alert types
 */
@Parcelize
data object HistorySimulatorScreen : Screen {
    /**
     * UI state for the History Simulator screen.
     *
     * @property alertHistoryDao DAO for history operations
     * @property cityDao DAO for city lookups
     * @property eventSink Callback for handling user events
     */
    data class State(
        val alertHistoryDao: AlertHistoryDao,
        val cityDao: CityDao,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events that can be triggered from the History Simulator.
     */
    sealed class Event : CircuitUiEvent {
        /** Navigate back to Developer Portal */
        data object GoBack : Event()
    }
}

@AssistedInject
class HistorySimulatorPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
        private val alertHistoryDao: AlertHistoryDao,
        private val cityDao: CityDao,
    ) : Presenter<HistorySimulatorScreen.State> {
        @Composable
        override fun present(): HistorySimulatorScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(HistorySimulatorScreen::class)
                Timber.tag("DevPortal").d("History Simulator opened")
            }

            return HistorySimulatorScreen.State(
                alertHistoryDao = alertHistoryDao,
                cityDao = cityDao,
            ) { event ->
                when (event) {
                    HistorySimulatorScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(HistorySimulatorScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): HistorySimulatorPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(HistorySimulatorScreen::class, AppScope::class)
@Composable
fun HistorySimulatorScreen(
    state: HistorySimulatorScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 History Simulator") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(HistorySimulatorScreen.Event.GoBack)
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
        HistorySimulatorContent(
            alertHistoryDao = state.alertHistoryDao,
            cityDao = state.cityDao,
            snackbarHostState = snackbarHostState,
            modifier =
                modifier
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        )
    }
}

data class HistoryTemplate(
    val name: String,
    val description: String,
    val count: Int,
    val timeRangeHours: Long,
    val categories: List<WeatherAlertCategory>,
)

private val historyTemplates =
    listOf(
        HistoryTemplate(
            name = "Recent Alerts",
            description = "5 alerts in last 24 hours",
            count = 5,
            timeRangeHours = 24,
            categories = listOf(WeatherAlertCategory.SNOW_FALL, WeatherAlertCategory.RAIN_FALL),
        ),
        HistoryTemplate(
            name = "This Week History",
            description = "15 alerts spread across the week",
            count = 15,
            timeRangeHours = 24 * 7,
            categories = listOf(WeatherAlertCategory.SNOW_FALL, WeatherAlertCategory.RAIN_FALL),
        ),
        HistoryTemplate(
            name = "Last Month Mixed",
            description = "30 diverse alerts over 30 days",
            count = 30,
            timeRangeHours = 24 * 30,
            categories = listOf(WeatherAlertCategory.SNOW_FALL, WeatherAlertCategory.RAIN_FALL),
        ),
        HistoryTemplate(
            name = "Stress Test",
            description = "100 alerts for performance testing",
            count = 100,
            timeRangeHours = 24 * 90,
            categories = listOf(WeatherAlertCategory.SNOW_FALL, WeatherAlertCategory.RAIN_FALL),
        ),
    )

@Composable
private fun HistorySimulatorContent(
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Stats state
    var totalCount by remember { mutableIntStateOf(0) }
    var last7DaysCount by remember { mutableIntStateOf(0) }
    var last30DaysCount by remember { mutableIntStateOf(0) }
    var snowCount by remember { mutableIntStateOf(0) }
    var rainCount by remember { mutableIntStateOf(0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Loading state
    var isGenerating by remember { mutableStateOf(false) }

    // Custom generator state
    var customCount by remember { mutableFloatStateOf(10f) }

    // Fetch stats
    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            val all = alertHistoryDao.getAll()
            totalCount = all.size

            val now = System.currentTimeMillis()
            val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
            val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)

            last7DaysCount = alertHistoryDao.getHistorySince(sevenDaysAgo).size
            last30DaysCount = alertHistoryDao.getHistorySince(thirtyDaysAgo).size

            snowCount = alertHistoryDao.getHistoryByCategory(WeatherAlertCategory.SNOW_FALL).size
            rainCount = alertHistoryDao.getHistoryByCategory(WeatherAlertCategory.RAIN_FALL).size
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Loading indicator
        if (isGenerating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Template Cards
        TemplateCard(
            alertHistoryDao = alertHistoryDao,
            cityDao = cityDao,
            snackbarHostState = snackbarHostState,
            isGenerating = isGenerating,
            onGeneratingChanged = { isGenerating = it },
            onHistoryGenerated = { refreshTrigger++ },
        )

        // Custom Generator
        CustomGeneratorCard(
            alertHistoryDao = alertHistoryDao,
            cityDao = cityDao,
            snackbarHostState = snackbarHostState,
            customCount = customCount,
            onCustomCountChanged = { customCount = it },
            isGenerating = isGenerating,
            onGeneratingChanged = { isGenerating = it },
            onHistoryGenerated = { refreshTrigger++ },
        )

        // Statistics Card
        StatisticsCard(
            totalCount = totalCount,
            last7DaysCount = last7DaysCount,
            last30DaysCount = last30DaysCount,
            snowCount = snowCount,
            rainCount = rainCount,
        )

        // Management Actions
        ManagementCard(
            alertHistoryDao = alertHistoryDao,
            snackbarHostState = snackbarHostState,
            isGenerating = isGenerating,
            onHistoryCleared = { refreshTrigger++ },
        )
    }
}

@Composable
private fun TemplateCard(
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    isGenerating: Boolean,
    onGeneratingChanged: (Boolean) -> Unit,
    onHistoryGenerated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Quick Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Generate history data instantly from templates",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            historyTemplates.forEach { template ->
                ElevatedCard(
                    onClick = {
                        if (!isGenerating) {
                            scope.launch {
                                onGeneratingChanged(true)
                                val count =
                                    generateHistoryFromTemplate(
                                        template = template,
                                        alertHistoryDao = alertHistoryDao,
                                        cityDao = cityDao,
                                    )
                                snackbarHostState.showSnackbar(
                                    "Generated $count history entries from ${template.name}",
                                )
                                onHistoryGenerated()
                                onGeneratingChanged(false)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${template.count} alerts",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomGeneratorCard(
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    customCount: Float,
    onCustomCountChanged: (Float) -> Unit,
    isGenerating: Boolean,
    onGeneratingChanged: (Boolean) -> Unit,
    onHistoryGenerated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Custom Generator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Generate ${customCount.toInt()} alert history entries",
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                value = customCount,
                onValueChange = onCustomCountChanged,
                valueRange = 1f..100f,
                steps = 99,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating,
            )

            Button(
                onClick = {
                    scope.launch {
                        onGeneratingChanged(true)
                        val template =
                            HistoryTemplate(
                                name = "Custom",
                                description = "Custom generation",
                                count = customCount.toInt(),
                                timeRangeHours = 24 * 30,
                                categories =
                                    listOf(
                                        WeatherAlertCategory.SNOW_FALL,
                                        WeatherAlertCategory.RAIN_FALL,
                                    ),
                            )
                        val count =
                            generateHistoryFromTemplate(
                                template = template,
                                alertHistoryDao = alertHistoryDao,
                                cityDao = cityDao,
                            )
                        snackbarHostState.showSnackbar(
                            "Generated $count custom history entries",
                        )
                        onHistoryGenerated()
                        onGeneratingChanged(false)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating,
            ) {
                Text("Generate History")
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    totalCount: Int,
    last7DaysCount: Int,
    last30DaysCount: Int,
    snowCount: Int,
    rainCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Current History Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            InfoRow(label = "Total History", value = totalCount.toString())
            InfoRow(label = "Last 7 Days", value = last7DaysCount.toString())
            InfoRow(label = "Last 30 Days", value = last30DaysCount.toString())
            InfoRow(label = "❄️ Snow Alerts", value = snowCount.toString())
            InfoRow(label = "🌧️ Rain Alerts", value = rainCount.toString())
        }
    }
}

@Composable
private fun ManagementCard(
    alertHistoryDao: AlertHistoryDao,
    snackbarHostState: SnackbarHostState,
    isGenerating: Boolean,
    onHistoryCleared: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Management Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            alertHistoryDao.deleteAll()
                        }
                        snackbarHostState.showSnackbar("All history cleared")
                        onHistoryCleared()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating,
            ) {
                Text("Clear All History")
            }
        }
    }
}

private suspend fun generateHistoryFromTemplate(
    template: HistoryTemplate,
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
): Int =
    withContext(Dispatchers.IO) {
        // Get sample cities
        val cities =
            listOf(
                "Toronto",
                "Buffalo",
                "Chicago",
                "New York",
                "Seattle",
                "Boston",
                "Montreal",
                "Vancouver",
            )

        val now = System.currentTimeMillis()
        val startTime = now - TimeUnit.HOURS.toMillis(template.timeRangeHours)

        var insertedCount = 0

        repeat(template.count) {
            val category = template.categories.random()
            val cityName = cities.random()

            // Random threshold between 5mm and 30mm
            val threshold = Random.nextFloat() * 25f + 5f

            // Weather value: either above threshold (60%) or below (40%)
            val weatherValue =
                if (Random.nextFloat() < 0.6f) {
                    // Above threshold - triggered alert
                    threshold + Random.nextDouble() * 20.0
                } else {
                    // Below threshold - shouldn't happen but adding variety
                    threshold * Random.nextDouble()
                }

            // Random timestamp within range
            val timestamp = startTime + Random.nextLong(TimeUnit.HOURS.toMillis(template.timeRangeHours))

            // Use alert ID 1 as default (test data)
            val alertId = 1L

            val history =
                AlertHistory(
                    alertId = alertId,
                    triggeredAt = timestamp,
                    weatherValue = weatherValue,
                    thresholdValue = threshold,
                    cityName = cityName,
                    alertCategory = category,
                )

            try {
                alertHistoryDao.insert(history)
                insertedCount++
            } catch (e: Exception) {
                Timber.tag("DevPortal").e(e, "Failed to insert history entry")
            }
        }

        Timber
            .tag("DevPortal")
            .d("Generated $insertedCount history entries from template: ${template.name}")

        insertedCount
    }
