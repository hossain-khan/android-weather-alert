package dev.hossain.weatheralert.ui.devtools

import android.content.Context
import android.os.Environment
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data object DatabaseInspectorScreen : Screen {
    data class State(
        val context: Context,
        val alertDao: AlertDao,
        val alertHistoryDao: AlertHistoryDao,
        val cityDao: CityDao,
        val cityForecastDao: CityForecastDao,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

@AssistedInject
class DatabaseInspectorPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
        private val context: Context,
        private val alertDao: AlertDao,
        private val alertHistoryDao: AlertHistoryDao,
        private val cityDao: CityDao,
        private val cityForecastDao: CityForecastDao,
    ) : Presenter<DatabaseInspectorScreen.State> {
        @Composable
        override fun present(): DatabaseInspectorScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(DatabaseInspectorScreen::class)
                Timber.tag("DevPortal").d("Database Inspector opened")
            }

            return DatabaseInspectorScreen.State(
                context = context,
                alertDao = alertDao,
                alertHistoryDao = alertHistoryDao,
                cityDao = cityDao,
                cityForecastDao = cityForecastDao,
            ) { event ->
                when (event) {
                    DatabaseInspectorScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(DatabaseInspectorScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): DatabaseInspectorPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DatabaseInspectorScreen::class, AppScope::class)
@Composable
fun DatabaseInspectorScreen(
    state: DatabaseInspectorScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗄️ Database Inspector") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(DatabaseInspectorScreen.Event.GoBack)
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
        DatabaseInspectorContent(
            context = state.context,
            alertDao = state.alertDao,
            alertHistoryDao = state.alertHistoryDao,
            cityDao = state.cityDao,
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
private fun DatabaseInspectorContent(
    context: Context,
    alertDao: AlertDao,
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
    cityForecastDao: CityForecastDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Statistics state
    var totalCities by remember { mutableIntStateOf(0) }
    var totalAlerts by remember { mutableIntStateOf(0) }
    var totalHistory by remember { mutableIntStateOf(0) }
    var totalForecasts by remember { mutableIntStateOf(0) }
    var dbSizeBytes by remember { mutableLongStateOf(0L) }
    var dbLastModified by remember { mutableLongStateOf(0L) }
    var dbPath by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    // Load database stats
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                totalCities = cityDao.getAllCities().first().size
                totalAlerts = alertDao.getAll().size
                totalHistory = alertHistoryDao.getAll().size
                // Count all forecasts across all cities (approximate)
                totalForecasts =
                    try {
                        // Try to get count from any city forecast table
                        val sampleCityId = 1L
                        cityForecastDao.getCityForecastsByCityId(sampleCityId).size
                    } catch (e: Exception) {
                        0
                    }

                val dbFile = context.getDatabasePath("weather_alert.db")
                if (dbFile.exists()) {
                    dbSizeBytes = dbFile.length()
                    dbLastModified = dbFile.lastModified()
                    dbPath = dbFile.absolutePath
                }
                isLoading = false
            } catch (e: Exception) {
                Timber.tag("DevPortal").e(e, "Failed to load database stats")
                isLoading = false
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Statistics Card
        StatisticsCard(
            totalCities = totalCities,
            totalAlerts = totalAlerts,
            totalHistory = totalHistory,
            totalForecasts = totalForecasts,
            dbSizeBytes = dbSizeBytes,
            dbLastModified = dbLastModified,
        )

        // Database Info Card
        DatabaseInfoCard(
            dbPath = dbPath,
            dbSizeBytes = dbSizeBytes,
        )

        // Quick Views Card
        QuickViewsCard(
            alertDao = alertDao,
            alertHistoryDao = alertHistoryDao,
            cityDao = cityDao,
            snackbarHostState = snackbarHostState,
        )

        // Export Actions Card
        ExportActionsCard(
            context = context,
            dbPath = dbPath,
            snackbarHostState = snackbarHostState,
            isExporting = isExporting,
            onExportingChanged = { isExporting = it },
        )
    }
}

@Composable
private fun StatisticsCard(
    totalCities: Int,
    totalAlerts: Int,
    totalHistory: Int,
    totalForecasts: Int,
    dbSizeBytes: Long,
    dbLastModified: Long,
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
                text = "Database Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            InfoRow(label = "Cities", value = totalCities.toString())
            InfoRow(label = "Alerts", value = totalAlerts.toString())
            InfoRow(label = "History Entries", value = totalHistory.toString())
            InfoRow(label = "Cached Forecasts", value = totalForecasts.toString())
            InfoRow(label = "Database Size", value = formatBytes(dbSizeBytes))
            if (dbLastModified > 0) {
                InfoRow(
                    label = "Last Modified",
                    value = formatDate(dbLastModified),
                )
            }
        }
    }
}

@Composable
private fun DatabaseInfoCard(
    dbPath: String,
    dbSizeBytes: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Database Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Location:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = dbPath.ifEmpty { "Not found" },
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp),
            )

            OutlinedButton(
                onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as
                            android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("DB Path", dbPath)
                    clipboard.setPrimaryClip(clip)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = dbPath.isNotEmpty(),
            ) {
                Text("Copy Database Path")
            }
        }
    }
}

@Composable
private fun QuickViewsCard(
    alertDao: AlertDao,
    alertHistoryDao: AlertHistoryDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
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
                text = "Quick Data Views",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "View sample data from database tables",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElevatedCard(
                onClick = {
                    scope.launch {
                        val alerts =
                            withContext(Dispatchers.IO) {
                                alertDao.getAll()
                            }
                        val message =
                            if (alerts.isEmpty()) {
                                "No alerts found"
                            } else {
                                "Found ${alerts.size} alerts:\n" +
                                    alerts
                                        .take(3)
                                        .joinToString("\n") { alert ->
                                            "Alert ID ${alert.id} - ${alert.alertCategory.name} (${alert.threshold}mm)"
                                        }
                            }
                        snackbarHostState.showSnackbar(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "📋 View All Alerts",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            ElevatedCard(
                onClick = {
                    scope.launch {
                        val history =
                            withContext(Dispatchers.IO) {
                                alertHistoryDao.getAll().take(10)
                            }
                        val message =
                            if (history.isEmpty()) {
                                "No history found"
                            } else {
                                "Recent ${history.size} entries:\n" +
                                    history
                                        .take(3)
                                        .joinToString("\n") { entry ->
                                            "${entry.cityName} - ${formatDate(entry.triggeredAt)}"
                                        }
                            }
                        snackbarHostState.showSnackbar(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "📜 View Recent History (10)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            ElevatedCard(
                onClick = {
                    scope.launch {
                        val cities =
                            withContext(Dispatchers.IO) {
                                cityDao.searchCitiesByNameStartingWith("", 20).first()
                            }
                        val message =
                            if (cities.isEmpty()) {
                                "No cities found"
                            } else {
                                "Sample ${cities.size} cities:\n" +
                                    cities
                                        .take(5)
                                        .joinToString(", ") { it.cityName }
                            }
                        snackbarHostState.showSnackbar(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "🌍 View Cities (Sample 20)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun ExportActionsCard(
    context: Context,
    dbPath: String,
    snackbarHostState: SnackbarHostState,
    isExporting: Boolean,
    onExportingChanged: (Boolean) -> Unit,
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
                text = "Export Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (isExporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    scope.launch {
                        onExportingChanged(true)
                        try {
                            val result =
                                withContext(Dispatchers.IO) {
                                    exportDatabase(context, dbPath)
                                }
                            if (result != null) {
                                snackbarHostState.showSnackbar(
                                    "Database exported to: $result",
                                )
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Failed to export database",
                                )
                            }
                        } catch (e: Exception) {
                            Timber.tag("DevPortal").e(e, "Export failed")
                            snackbarHostState.showSnackbar(
                                "Export failed: ${e.message}",
                            )
                        } finally {
                            onExportingChanged(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting && dbPath.isNotEmpty(),
            ) {
                Text("Export Database to Downloads")
            }

            Text(
                text = "Note: Database will be exported to Downloads folder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

private fun formatBytes(bytes: Long): String =
    when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private suspend fun exportDatabase(
    context: Context,
    dbPath: String,
): String? =
    withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(dbPath)
            if (!sourceFile.exists()) {
                Timber.tag("DevPortal").w("Source database file not found: $dbPath")
                return@withContext null
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destFile = File(downloadsDir, "weather_alert_db_$timestamp.db")

            sourceFile.copyTo(destFile, overwrite = true)

            Timber
                .tag("DevPortal")
                .d("Database exported successfully to: ${destFile.absolutePath}")

            destFile.absolutePath
        } catch (e: Exception) {
            Timber.tag("DevPortal").e(e, "Failed to export database")
            null
        }
    }
