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
            var historyItems by remember { mutableStateOf<List<AlertHistory>?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            LaunchedImpressionEffect {
                analytics.logScreenView(AlertHistoryScreen::class)
            }

            LaunchedEffect(Unit) {
                try {
                    // Get alert history from last 30 days
                    val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                    historyItems = alertHistoryDao.getHistorySince(thirtyDaysAgo)
                    isLoading = false
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load alert history")
                    errorMessage = "Failed to load alert history: ${e.message}"
                    isLoading = false
                }
            }

            return AlertHistoryScreen.State(
                historyItems = historyItems,
                isLoading = isLoading,
                errorMessage = errorMessage,
            ) { event ->
                when (event) {
                    AlertHistoryScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    AlertHistoryScreen.Event.ExportHistory -> {
                        scope.launch {
                            exportHistoryToCsv(context, historyItems)
                        }
                    }

                    AlertHistoryScreen.Event.ShowFilterOptions -> {
                        // TODO: Implement filter options in future iteration
                        Toast.makeText(context, "Filter options coming soon", Toast.LENGTH_SHORT).show()
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

                    val file = File(context.cacheDir, "alert_history_${System.currentTimeMillis()}.csv")
                    FileWriter(file).use { writer ->
                        // Write CSV header
                        writer.append("Date,City,Alert Type,Weather Value,Threshold,Unit\n")

                        // Write data rows
                        historyItems.forEach { item ->
                            writer.append("\"${formatTimestampToDateTime(item.triggeredAt)}\",")
                            writer.append("\"${item.cityName}\",")
                            writer.append("\"${item.alertCategory.label}\",")
                            writer.append("${item.weatherValue},")
                            writer.append("${item.thresholdValue},")
                            writer.append("\"${item.alertCategory.unit}\"\n")
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

        @CircuitInject(AlertHistoryScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): AlertHistoryPresenter
        }
    }
