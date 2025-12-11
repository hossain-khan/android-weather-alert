package dev.hossain.weatheralert.ui.history

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.AlertHistory
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.formatTimestampToDateTime
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit

@AssistedInject
class AlertHistoryPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val alertHistoryDao: AlertHistoryDao,
        private val analytics: Analytics,
    ) : Presenter<AlertHistoryScreen.State> {
        @Composable
        override fun present(): AlertHistoryScreen.State {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var allHistoryItems by remember { mutableStateOf<List<AlertHistory>?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var showClearConfirmDialog by remember { mutableStateOf(false) }
            var showFilterSheet by remember { mutableStateOf(false) }
            var selectedAlertType by remember { mutableStateOf<WeatherAlertCategory?>(null) }
            var selectedLocation by remember { mutableStateOf<String?>(null) }

            LaunchedImpressionEffect {
                analytics.logScreenView(AlertHistoryScreen::class)
            }

            LaunchedEffect(Unit) {
                try {
                    // Get alert history from last 30 days
                    val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                    allHistoryItems = alertHistoryDao.getHistorySince(thirtyDaysAgo)
                    isLoading = false
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load alert history")
                    errorMessage = "Failed to load alert history: ${e.message}"
                    isLoading = false
                }
            }

            // Apply filters
            val filteredHistoryItems =
                allHistoryItems?.filter { item ->
                    val matchesType = selectedAlertType == null || item.alertCategory == selectedAlertType
                    val matchesLocation = selectedLocation == null || item.cityName == selectedLocation
                    matchesType && matchesLocation
                }

            // Get unique locations
            val uniqueLocations = allHistoryItems?.map { it.cityName }?.distinct()?.sorted() ?: emptyList()

            return AlertHistoryScreen.State(
                historyItems = filteredHistoryItems,
                isLoading = isLoading,
                errorMessage = errorMessage,
                showClearConfirmDialog = showClearConfirmDialog,
                showFilterSheet = showFilterSheet,
                selectedAlertType = selectedAlertType,
                selectedLocation = selectedLocation,
                uniqueLocations = uniqueLocations,
            ) { event ->
                when (event) {
                    AlertHistoryScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    AlertHistoryScreen.Event.ExportHistory -> {
                        scope.launch {
                            exportHistoryToCsv(context, filteredHistoryItems)
                        }
                    }

                    AlertHistoryScreen.Event.ShowFilterOptions -> {
                        showFilterSheet = !showFilterSheet
                    }

                    AlertHistoryScreen.Event.ClearAllHistory -> {
                        showClearConfirmDialog = true
                    }

                    AlertHistoryScreen.Event.ConfirmClearHistory -> {
                        scope.launch {
                            try {
                                alertHistoryDao.deleteAll()
                                allHistoryItems = emptyList()
                                showClearConfirmDialog = false
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "All history cleared", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to clear history")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to clear history: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    AlertHistoryScreen.Event.DismissClearDialog -> {
                        showClearConfirmDialog = false
                    }

                    is AlertHistoryScreen.Event.FilterByAlertType -> {
                        selectedAlertType = event.alertType
                    }

                    is AlertHistoryScreen.Event.FilterByLocation -> {
                        selectedLocation = event.location
                    }

                    AlertHistoryScreen.Event.ClearFilters -> {
                        selectedAlertType = null
                        selectedLocation = null
                    }
                }
            }
        }

        private suspend fun exportHistoryToCsv(
            context: Context,
            historyItems: List<AlertHistory>?,
        ) {
            withContext(Dispatchers.IO) {
                try {
                    if (historyItems.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "No history to export", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext
                    }

                    val timestamp =
                        java.text
                            .SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.ENGLISH)
                            .format(java.util.Date())
                    val exportsDir = File(context.cacheDir, "exports")
                    exportsDir.mkdirs()
                    val file = File(exportsDir, "alert_history_$timestamp.csv")
                    FileWriter(file).use { writer ->
                        // Write CSV header
                        writer.append("Date,City,Alert Type,Weather Value,Threshold,Unit\n")

                        // Write data rows with proper CSV escaping
                        historyItems.forEach { item ->
                            writer.append("\"${escapeCsv(formatTimestampToDateTime(item.triggeredAt))}\",")
                            writer.append("\"${escapeCsv(item.cityName)}\",")
                            writer.append("\"${escapeCsv(item.alertCategory.label)}\",")
                            writer.append("${item.weatherValue},")
                            writer.append("${item.thresholdValue},")
                            writer.append("\"${escapeCsv(item.alertCategory.unit)}\"\n")
                        }
                    }

                    // Share the file
                    val uri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )

                    val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                    withContext(Dispatchers.Main) {
                        context.startActivity(Intent.createChooser(shareIntent, "Export Alert History"))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to export alert history")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        /**
         * Escapes special characters in CSV fields to prevent CSV injection attacks.
         * Doubles quotes and ensures the field is properly quoted if it contains special characters.
         */
        private fun escapeCsv(value: String): String {
            // Replace double quotes with two double quotes for CSV escaping
            return value.replace("\"", "\"\"")
        }

        @CircuitInject(AlertHistoryScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): AlertHistoryPresenter
        }
    }
