package dev.hossain.weatheralert.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.AlertHistory
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.formatTimestampToDateTime
import dev.hossain.weatheralert.util.formatUnit
import dev.zacsweers.metro.AppScope
import kotlinx.parcelize.Parcelize

@Parcelize
data object AlertHistoryScreen : Screen {
    data class State(
        val historyItems: List<AlertHistory>?,
        val isLoading: Boolean,
        val errorMessage: String?,
        val showClearConfirmDialog: Boolean,
        val showFilterSheet: Boolean,
        val selectedAlertType: WeatherAlertCategory?,
        val selectedLocation: String?,
        val uniqueLocations: List<String>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()

        data object ExportHistory : Event()

        data object ShowFilterOptions : Event()

        data object ClearAllHistory : Event()

        data object ConfirmClearHistory : Event()

        data object DismissClearDialog : Event()

        data class FilterByAlertType(
            val alertType: WeatherAlertCategory?,
        ) : Event()

        data class FilterByLocation(
            val location: String?,
        ) : Event()

        data object ClearFilters : Event()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AlertHistoryScreen::class, AppScope::class)
@Composable
fun AlertHistoryScreen(
    state: AlertHistoryScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert History") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AlertHistoryScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        state.eventSink(AlertHistoryScreen.Event.ClearAllHistory)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear all history",
                        )
                    }
                    IconButton(onClick = {
                        state.eventSink(AlertHistoryScreen.Event.ExportHistory)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.download_24dp),
                            contentDescription = "Export history",
                        )
                    }
                    IconButton(onClick = {
                        state.eventSink(AlertHistoryScreen.Event.ShowFilterOptions)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.filter_list_24dp),
                            contentDescription = "Filter options",
                        )
                    }
                },
            )
        },
    ) { contentPaddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(contentPaddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(contentPaddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            state.historyItems.isNullOrEmpty() -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(contentPaddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "No alert history",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "When alerts are triggered, they will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier =
                        modifier
                            .fillMaxSize()
                            .padding(contentPaddingValues)
                            .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(state.historyItems) { historyItem ->
                        AlertHistoryItem(historyItem = historyItem)
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Clear all confirmation dialog
    if (state.showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                state.eventSink(AlertHistoryScreen.Event.DismissClearDialog)
            },
            title = {
                Text("Clear All History?")
            },
            text = {
                Text("This will permanently delete all alert history. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.eventSink(AlertHistoryScreen.Event.ConfirmClearHistory)
                    },
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        state.eventSink(AlertHistoryScreen.Event.DismissClearDialog)
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    // Filter bottom sheet
    if (state.showFilterSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                state.eventSink(AlertHistoryScreen.Event.ShowFilterOptions)
            },
            sheetState = sheetState,
        ) {
            FilterSheet(
                selectedAlertType = state.selectedAlertType,
                selectedLocation = state.selectedLocation,
                uniqueLocations = state.uniqueLocations,
                onAlertTypeSelected = { alertType ->
                    state.eventSink(AlertHistoryScreen.Event.FilterByAlertType(alertType))
                },
                onLocationSelected = { location ->
                    state.eventSink(AlertHistoryScreen.Event.FilterByLocation(location))
                },
                onClearFilters = {
                    state.eventSink(AlertHistoryScreen.Event.ClearFilters)
                },
            )
        }
    }
}

@Composable
fun AlertHistoryItem(
    historyItem: AlertHistory,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Category icon
            Icon(
                painter = painterResource(historyItem.alertCategory.iconRes()),
                contentDescription =
                    when (historyItem.alertCategory) {
                        WeatherAlertCategory.SNOW_FALL -> "Snowfall alert icon"
                        WeatherAlertCategory.RAIN_FALL -> "Rainfall alert icon"
                    },
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            // Alert details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = historyItem.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text =
                        "${historyItem.alertCategory.label}: ${
                            historyItem.weatherValue.formatUnit(historyItem.alertCategory.unit)
                        } (threshold: ${historyItem.thresholdValue.toDouble().formatUnit(historyItem.alertCategory.unit)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatTimestampToDateTime(historyItem.triggeredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    selectedAlertType: WeatherAlertCategory?,
    selectedLocation: String?,
    uniqueLocations: List<String>,
    onAlertTypeSelected: (WeatherAlertCategory?) -> Unit,
    onLocationSelected: (String?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Filter Options",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Alert Type Filter
        Text(
            text = "Alert Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedAlertType == null,
                onClick = { onAlertTypeSelected(null) },
                label = { Text("All") },
            )
            FilterChip(
                selected = selectedAlertType == WeatherAlertCategory.SNOW_FALL,
                onClick = { onAlertTypeSelected(WeatherAlertCategory.SNOW_FALL) },
                label = { Text("Snow") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(WeatherAlertCategory.SNOW_FALL.iconRes()),
                        contentDescription = "Snow",
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            FilterChip(
                selected = selectedAlertType == WeatherAlertCategory.RAIN_FALL,
                onClick = { onAlertTypeSelected(WeatherAlertCategory.RAIN_FALL) },
                label = { Text("Rain") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(WeatherAlertCategory.RAIN_FALL.iconRes()),
                        contentDescription = "Rain",
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

        // Location Filter
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        // All Locations chip
        FilterChip(
            selected = selectedLocation == null,
            onClick = { onLocationSelected(null) },
            label = { Text("All Locations") },
            modifier =
                Modifier.semantics {
                    contentDescription =
                        if (selectedLocation == null) {
                            "All locations filter selected"
                        } else {
                            "Filter by all locations"
                        }
                },
        )

        // Individual location chips
        uniqueLocations.forEachIndexed { index, location ->
            FilterChip(
                selected = selectedLocation == location,
                onClick = { onLocationSelected(location) },
                label = { Text(location) },
                modifier =
                    Modifier.semantics {
                        contentDescription =
                            if (selectedLocation == location) {
                                "Filter by $location selected"
                            } else {
                                "Filter by $location"
                            }
                    },
            )
        }

        // Clear filters button
        if (selectedAlertType != null || selectedLocation != null) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Clear All Filters")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
fun AlertHistoryScreenPreview() {
    val sampleHistory =
        listOf(
            AlertHistory(
                id = 1,
                alertId = 100,
                triggeredAt = System.currentTimeMillis() - 86400000, // 1 day ago
                weatherValue = 15.5,
                thresholdValue = 10.0f,
                cityName = "Toronto",
                alertCategory = WeatherAlertCategory.SNOW_FALL,
            ),
            AlertHistory(
                id = 2,
                alertId = 101,
                triggeredAt = System.currentTimeMillis() - 172800000, // 2 days ago
                weatherValue = 25.0,
                thresholdValue = 20.0f,
                cityName = "Vancouver",
                alertCategory = WeatherAlertCategory.RAIN_FALL,
            ),
        )

    val sampleState =
        AlertHistoryScreen.State(
            historyItems = sampleHistory,
            isLoading = false,
            errorMessage = null,
            showClearConfirmDialog = false,
            showFilterSheet = false,
            selectedAlertType = null,
            selectedLocation = null,
            uniqueLocations = listOf("Toronto", "Vancouver"),
            eventSink = {},
        )

    WeatherAlertAppTheme {
        AlertHistoryScreen(state = sampleState)
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun AlertHistoryScreenEmptyPreview() {
    val sampleState =
        AlertHistoryScreen.State(
            historyItems = emptyList(),
            isLoading = false,
            errorMessage = null,
            showClearConfirmDialog = false,
            showFilterSheet = false,
            selectedAlertType = null,
            selectedLocation = null,
            uniqueLocations = emptyList(),
            eventSink = {},
        )

    WeatherAlertAppTheme {
        AlertHistoryScreen(state = sampleState)
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun AlertHistoryScreenLoadingPreview() {
    val sampleState =
        AlertHistoryScreen.State(
            historyItems = null,
            isLoading = true,
            errorMessage = null,
            showClearConfirmDialog = false,
            showFilterSheet = false,
            selectedAlertType = null,
            selectedLocation = null,
            uniqueLocations = emptyList(),
            eventSink = {},
        )

    WeatherAlertAppTheme {
        AlertHistoryScreen(state = sampleState)
    }
}
