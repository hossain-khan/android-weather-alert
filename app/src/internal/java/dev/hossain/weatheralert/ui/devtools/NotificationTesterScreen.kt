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
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import dev.hossain.weatheralert.notification.SnoozeAlertReceiver
import dev.hossain.weatheralert.notification.debugNotification
import dev.hossain.weatheralert.notification.debugSnooze
import dev.hossain.weatheralert.notification.triggerNotification
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

/**
 * Developer tool for testing weather alert notifications.
 *
 * This screen allows developers to send test notifications without waiting for
 * real weather conditions. Useful for testing notification appearance, actions,
 * and system integration.
 *
 * Features:
 * - Send test notification with realistic weather data
 * - Test notification actions (View Details, Snooze)
 * - Verify notification appearance in system tray
 */
@Parcelize
data object NotificationTesterScreen : Screen {
    /**
     * UI state for the Notification Tester screen.
     *
     * @property eventSink Callback for handling user events
     */
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    /**
     * Events that can be triggered from the Notification Tester.
     */
    sealed class Event : CircuitUiEvent {
        /** Navigate back to Developer Portal */
        data object GoBack : Event()
    }
}

@AssistedInject
class NotificationTesterPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
    ) : Presenter<NotificationTesterScreen.State> {
        @Composable
        override fun present(): NotificationTesterScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(NotificationTesterScreen::class)
                Timber.tag("DevPortal").d("Notification Tester opened")
            }

            return NotificationTesterScreen.State { event ->
                when (event) {
                    NotificationTesterScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(NotificationTesterScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): NotificationTesterPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(NotificationTesterScreen::class, AppScope::class)
@Composable
fun NotificationTesterScreen(
    state: NotificationTesterScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔔 Notification Tester") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(NotificationTesterScreen.Event.GoBack)
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
        NotificationTesterContent(
            snackbarHostState = snackbarHostState,
            modifier =
                modifier
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        )
    }
}

@Composable
private fun NotificationTesterContent(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form state
    var alertCategory by remember { mutableStateOf(WeatherAlertCategory.SNOW_FALL) }
    var cityName by remember { mutableStateOf("Toronto") }
    var currentValue by remember { mutableFloatStateOf(30f) }
    var thresholdValue by remember { mutableFloatStateOf(15f) }
    var reminderNotes by remember { mutableStateOf("* Charge batteries\n* Check tire pressure\n* Order Groceries") }
    var snoozeDuration by remember { mutableStateOf(SnoozeAlertReceiver.SNOOZE_TOMORROW) }
    var alertId by remember { mutableStateOf("1") }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Presets Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            alertCategory = WeatherAlertCategory.SNOW_FALL
                            cityName = "Toronto"
                            currentValue = 30f
                            thresholdValue = 15f
                            reminderNotes = "* Charge batteries\n* Check tire pressure"
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Snow Alert", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            alertCategory = WeatherAlertCategory.RAIN_FALL
                            cityName = "Seattle"
                            currentValue = 45f
                            thresholdValue = 25f
                            reminderNotes = "* Bring umbrella\n* Check windows"
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Rain Alert", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            alertCategory = WeatherAlertCategory.SNOW_FALL
                            cityName = "Buffalo"
                            currentValue = 75f
                            thresholdValue = 10f
                            reminderNotes = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("High Snow", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            reminderNotes = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("No Notes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Custom Configuration Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Custom Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                // Alert Category Dropdown
                AlertCategoryDropdown(
                    selectedCategory = alertCategory,
                    onCategorySelected = { alertCategory = it },
                )

                // City Name
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("City Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Current Value Slider
                Column {
                    Text(
                        text = "Current Value: ${currentValue.toInt()}mm",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = currentValue,
                        onValueChange = { currentValue = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Threshold Value Slider
                Column {
                    Text(
                        text = "Threshold: ${thresholdValue.toInt()}mm",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = thresholdValue,
                        onValueChange = { thresholdValue = it },
                        valueRange = 0f..50f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Reminder Notes
                OutlinedTextField(
                    value = reminderNotes,
                    onValueChange = { reminderNotes = it },
                    label = { Text("Reminder Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                )

                // Alert ID
                OutlinedTextField(
                    value = alertId,
                    onValueChange = { alertId = it },
                    label = { Text("Alert ID (for snooze testing)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Snooze Duration Dropdown
                SnoozeDurationDropdown(
                    selectedDuration = snoozeDuration,
                    onDurationSelected = { snoozeDuration = it },
                )
            }
        }

        // Action Buttons Section
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        sendTestNotification(
                            context = context,
                            alertCategory = alertCategory,
                            cityName = cityName,
                            currentValue = currentValue.toDouble(),
                            thresholdValue = thresholdValue,
                            reminderNotes = reminderNotes,
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Notification sent! Check your notification drawer.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Send Test Notification")
                }

                OutlinedButton(
                    onClick = {
                        val id = alertId.toLongOrNull() ?: 1L
                        testSnooze(
                            context = context,
                            alertId = id,
                            snoozeDuration = snoozeDuration,
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Snooze applied to alert #$id")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Test Snooze (Alert #$alertId)")
                }

                OutlinedButton(
                    onClick = {
                        Timber.tag("DevPortal").d("Sending debug preset notification")
                        debugNotification(context)
                        scope.launch {
                            snackbarHostState.showSnackbar("Debug notification sent!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Send Debug Preset")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnoozeDurationDropdown(
    selectedDuration: String,
    onDurationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val durationOptions =
        mapOf(
            SnoozeAlertReceiver.SNOOZE_1_HOUR to "1 Hour",
            SnoozeAlertReceiver.SNOOZE_3_HOURS to "3 Hours",
            SnoozeAlertReceiver.SNOOZE_1_DAY to "1 Day",
            SnoozeAlertReceiver.SNOOZE_TOMORROW to "Tomorrow (8 AM)",
            SnoozeAlertReceiver.SNOOZE_1_WEEK to "1 Week",
        )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = durationOptions[selectedDuration] ?: "Unknown",
            onValueChange = {},
            readOnly = true,
            label = { Text("Snooze Duration") },
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
            durationOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onDurationSelected(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun sendTestNotification(
    context: Context,
    alertCategory: WeatherAlertCategory,
    cityName: String,
    currentValue: Double,
    thresholdValue: Float,
    reminderNotes: String,
) {
    Timber.tag("DevPortal").d(
        "Sending test notification: category=$alertCategory, city=$cityName, " +
            "current=$currentValue, threshold=$thresholdValue",
    )

    triggerNotification(
        context = context,
        userAlertId = 1,
        notificationTag = "test",
        alertCategory = alertCategory,
        currentValue = currentValue,
        thresholdValue = thresholdValue,
        cityName = cityName,
        reminderNotes = reminderNotes,
    )
}

private fun testSnooze(
    context: Context,
    alertId: Long,
    snoozeDuration: String,
) {
    Timber.tag("DevPortal").d("Testing snooze: alertId=$alertId, duration=$snoozeDuration")
    debugSnooze(
        context = context,
        alertId = alertId,
        snoozeDuration = snoozeDuration,
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
fun NotificationTesterScreenPreview() {
    val sampleState =
        NotificationTesterScreen.State(
            eventSink = {},
        )
    WeatherAlertAppTheme {
        NotificationTesterScreen(state = sampleState)
    }
}
