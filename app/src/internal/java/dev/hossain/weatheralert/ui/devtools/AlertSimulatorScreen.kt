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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber

/**
 * Developer tool for simulating weather alerts.
 *
 * This screen allows developers to create test weather alerts quickly without
 * configuring them manually in the main app. All test data is marked with [TEST]
 * prefix for easy identification and cleanup.
 *
 * Features:
 * - Quick preset alerts (Snow, Rain, Multiple cities)
 * - Custom alert creation with specific parameters
 * - Delete all test alerts at once
 * - Test data marked with [TEST] prefix
 */
@Parcelize
data object AlertSimulatorScreen : Screen {
    /**
     * UI state for the Alert Simulator screen.
     *
     * @property alertDao DAO for alert operations
     * @property cityDao DAO for city lookups
     * @property eventSink Callback for handling user events
     */
    data class State(
        val alertDao: AlertDao,
        val cityDao: CityDao,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events that can be triggered from the Alert Simulator.
     */
    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

@AssistedInject
class AlertSimulatorPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
        private val alertDao: AlertDao,
        private val cityDao: CityDao,
    ) : Presenter<AlertSimulatorScreen.State> {
        @Composable
        override fun present(): AlertSimulatorScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(AlertSimulatorScreen::class)
                Timber.tag("DevPortal").d("Alert Simulator opened")
            }

            return AlertSimulatorScreen.State(
                alertDao = alertDao,
                cityDao = cityDao,
            ) { event ->
                when (event) {
                    AlertSimulatorScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(AlertSimulatorScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): AlertSimulatorPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AlertSimulatorScreen::class, AppScope::class)
@Composable
fun AlertSimulatorScreen(
    state: AlertSimulatorScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 Alert Simulator") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AlertSimulatorScreen.Event.GoBack)
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
        AlertSimulatorContent(
            alertDao = state.alertDao,
            cityDao = state.cityDao,
            snackbarHostState = snackbarHostState,
            modifier =
                modifier
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        )
    }
}

data class AlertPreset(
    val name: String,
    val cityName: String,
    val category: WeatherAlertCategory,
    val threshold: Float,
    val notes: String,
)

private val alertPresets =
    listOf(
        AlertPreset(
            name = "Toronto Snow - Low",
            cityName = "Toronto",
            category = WeatherAlertCategory.SNOW_FALL,
            threshold = 1f,
            notes = "[TEST] Low threshold snow alert for testing",
        ),
        AlertPreset(
            name = "NYC Rain - High",
            cityName = "New York",
            category = WeatherAlertCategory.RAIN_FALL,
            threshold = 25f,
            notes = "[TEST] High threshold rain alert for testing",
        ),
        AlertPreset(
            name = "Buffalo Snow - Medium",
            cityName = "Buffalo",
            category = WeatherAlertCategory.SNOW_FALL,
            threshold = 10f,
            notes = "[TEST] Medium threshold snow alert for testing",
        ),
        AlertPreset(
            name = "Chicago Rain - Custom",
            cityName = "Chicago",
            category = WeatherAlertCategory.RAIN_FALL,
            threshold = 15f,
            notes = "[TEST] Custom rain alert for Chicago testing",
        ),
    )

@Composable
private fun AlertSimulatorContent(
    alertDao: AlertDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe test alerts
    var testAlerts by remember { mutableStateOf<List<UserCityAlert>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            val allAlerts = alertDao.getAllAlertsWithCities()
            testAlerts = allAlerts.filter { it.alert.notes.startsWith("[TEST]") }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Quick Presets Section
        QuickPresetsCard(
            alertDao = alertDao,
            cityDao = cityDao,
            snackbarHostState = snackbarHostState,
            onAlertCreated = { refreshTrigger++ },
        )

        // Custom Alert Builder Section
        CustomAlertBuilderCard(
            alertDao = alertDao,
            cityDao = cityDao,
            snackbarHostState = snackbarHostState,
            onAlertCreated = { refreshTrigger++ },
        )

        // Existing Test Alerts Section
        ExistingTestAlertsCard(
            testAlerts = testAlerts,
            alertDao = alertDao,
            snackbarHostState = snackbarHostState,
            onAlertDeleted = { refreshTrigger++ },
        )
    }
}

@Composable
private fun QuickPresetsCard(
    alertDao: AlertDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    onAlertCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
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
                text = "Quick Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Tap a preset to create a test alert instantly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            alertPresets.forEach { preset ->
                ElevatedCard(
                    onClick = {
                        scope.launch {
                            val alertId =
                                createAlertFromPreset(
                                    context = context,
                                    preset = preset,
                                    alertDao = alertDao,
                                    cityDao = cityDao,
                                )
                            if (alertId > 0) {
                                snackbarHostState.showSnackbar(
                                    "${preset.name} created! Alert ID: $alertId",
                                )
                                onAlertCreated()
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Failed to create ${preset.name} - city not found",
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text =
                                    "${preset.cityName} • ${preset.category.name} • ${preset.threshold}mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text =
                                when (preset.category) {
                                    WeatherAlertCategory.SNOW_FALL -> "❄️"
                                    WeatherAlertCategory.RAIN_FALL -> "🌧️"
                                },
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAlertBuilderCard(
    alertDao: AlertDao,
    cityDao: CityDao,
    snackbarHostState: SnackbarHostState,
    onAlertCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form state
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var alertCategory by remember { mutableStateOf(WeatherAlertCategory.SNOW_FALL) }
    var threshold by remember { mutableStateOf("10.0") }
    var notes by remember { mutableStateOf("[TEST] ") }

    // City search
    var citySearchQuery by remember { mutableStateOf("") }
    var cityExpanded by remember { mutableStateOf(false) }
    val searchedCities by
        if (citySearchQuery.isNotEmpty()) {
            cityDao.searchCitiesByNameStartingWith(citySearchQuery, limit = 20).collectAsState(initial = emptyList())
        } else {
            remember { flowOf(emptyList<City>()) }.collectAsState(initial = emptyList())
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
                text = "Custom Alert Builder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // City Selector
            ExposedDropdownMenuBox(
                expanded = cityExpanded && searchedCities.isNotEmpty(),
                onExpandedChange = { cityExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = selectedCity?.cityName ?: citySearchQuery,
                    onValueChange = {
                        citySearchQuery = it
                        selectedCity = null
                        cityExpanded = true
                    },
                    label = { Text("City") },
                    placeholder = { Text("Start typing city name...") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                    singleLine = true,
                )

                ExposedDropdownMenu(
                    expanded = cityExpanded && searchedCities.isNotEmpty(),
                    onDismissRequest = { cityExpanded = false },
                ) {
                    searchedCities.forEach { city ->
                        DropdownMenuItem(
                            text = {
                                Text("${city.cityName}, ${city.provStateName ?: city.country}")
                            },
                            onClick = {
                                selectedCity = city
                                citySearchQuery = city.cityName
                                cityExpanded = false
                            },
                        )
                    }
                }
            }

            // Alert Category Dropdown
            AlertCategoryDropdown(
                selectedCategory = alertCategory,
                onCategorySelected = { alertCategory = it },
            )

            // Threshold Input
            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("Threshold (mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Add [TEST] prefix for easy identification") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            // Create Button
            Button(
                onClick = {
                    scope.launch {
                        val city = selectedCity
                        val thresholdValue = threshold.toFloatOrNull()

                        when {
                            city == null -> {
                                snackbarHostState.showSnackbar("Please select a city")
                            }

                            thresholdValue == null || thresholdValue <= 0 -> {
                                snackbarHostState.showSnackbar("Please enter a valid threshold > 0")
                            }

                            else -> {
                                val alertId =
                                    withContext(Dispatchers.IO) {
                                        val alert =
                                            Alert(
                                                cityId = city.id,
                                                alertCategory = alertCategory,
                                                threshold = thresholdValue,
                                                notes = notes,
                                            )
                                        alertDao.insertAlert(alert)
                                    }
                                Timber
                                    .tag("DevPortal")
                                    .d(
                                        "Created custom alert: ID=$alertId, city=${city.cityName}, " +
                                            "category=$alertCategory, threshold=$thresholdValue",
                                    )
                                snackbarHostState.showSnackbar(
                                    "Alert created! ID: $alertId for ${city.cityName}",
                                )
                                onAlertCreated()

                                // Reset form
                                selectedCity = null
                                citySearchQuery = ""
                                threshold = "10.0"
                                notes = "[TEST] "
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create Alert")
            }
        }
    }
}

@Composable
private fun ExistingTestAlertsCard(
    testAlerts: List<UserCityAlert>,
    alertDao: AlertDao,
    snackbarHostState: SnackbarHostState,
    onAlertDeleted: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Test Alerts (${testAlerts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (testAlerts.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val count = testAlerts.size
                                withContext(Dispatchers.IO) {
                                    testAlerts.forEach { alertDao.deleteAlert(it.alert) }
                                }
                                Timber.tag("DevPortal").d("Deleted $count test alerts")
                                snackbarHostState.showSnackbar(
                                    "Deleted $count test alerts",
                                )
                                onAlertDeleted()
                            }
                        },
                    ) {
                        Text("Delete All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (testAlerts.isEmpty()) {
                Text(
                    text = "No test alerts yet. Create one using presets or custom builder above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                testAlerts.forEach { userCityAlert ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${userCityAlert.city.cityName} - ${userCityAlert.alert.alertCategory.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "ID: ${userCityAlert.alert.id} • Threshold: ${userCityAlert.alert.threshold}mm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (userCityAlert.alert.notes.isNotEmpty()) {
                                    Text(
                                        text = userCityAlert.alert.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val alertId = userCityAlert.alert.id
                                        val cityName = userCityAlert.city.cityName
                                        withContext(Dispatchers.IO) {
                                            alertDao.deleteAlert(userCityAlert.alert)
                                        }
                                        Timber.tag("DevPortal").d("Deleted individual alert: ID=$alertId, city=$cityName")
                                        snackbarHostState.showSnackbar(
                                            "Deleted alert ID: $alertId",
                                        )
                                        onAlertDeleted()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete alert",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertCategoryDropdown(
    selectedCategory: WeatherAlertCategory,
    onCategorySelected: (WeatherAlertCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value =
                when (selectedCategory) {
                    WeatherAlertCategory.SNOW_FALL -> "❄️ Snow Fall"
                    WeatherAlertCategory.RAIN_FALL -> "🌧️ Rain Fall"
                },
            onValueChange = {},
            readOnly = true,
            label = { Text("Alert Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("❄️ Snow Fall") },
                onClick = {
                    onCategorySelected(WeatherAlertCategory.SNOW_FALL)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("🌧️ Rain Fall") },
                onClick = {
                    onCategorySelected(WeatherAlertCategory.RAIN_FALL)
                    expanded = false
                },
            )
        }
    }
}

private suspend fun createAlertFromPreset(
    context: Context,
    preset: AlertPreset,
    alertDao: AlertDao,
    cityDao: CityDao,
): Long =
    withContext(Dispatchers.IO) {
        // Find city by name
        val cities = cityDao.searchCitiesByNameStartingWith(preset.cityName, limit = 1)
        var cityList: List<City> = emptyList()
        cities.collect { cityList = it }

        val city = cityList.firstOrNull()

        if (city != null) {
            val alert =
                Alert(
                    cityId = city.id,
                    alertCategory = preset.category,
                    threshold = preset.threshold,
                    notes = preset.notes,
                )
            val alertId = alertDao.insertAlert(alert)
            Timber
                .tag("DevPortal")
                .d(
                    "Created preset alert: ${preset.name}, ID=$alertId, city=${city.cityName}",
                )
            alertId
        } else {
            Timber
                .tag("DevPortal")
                .e("City not found for preset: ${preset.cityName}")
            -1L
        }
    }
