package dev.hossain.weatheralert.ui.addalert

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.DEFAULT_RAIN_THRESHOLD
import dev.hossain.weatheralert.data.DEFAULT_SNOW_THRESHOLD
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.SnackbarData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.datamodel.WeatherService
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.addapikey.BringYourOwnApiKeyScreen
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.settings.UserSettingsScreen
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import io.tomorrow.api.TomorrowIoService
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.openweathermap.api.OpenWeatherService
import timber.log.Timber

@Parcelize
data class AddNewWeatherAlertScreen(
    val requestId: String,
) : Screen {
    data class State(
        val selectedApiService: WeatherService?,
        val citySuggestions: List<City>,
        val snowThreshold: Float,
        val rainThreshold: Float,
        val isAllInputValid: Boolean,
        val isApiCallInProgress: Boolean,
        val snackbarData: SnackbarData? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class SnowThresholdChanged(
            val value: Float,
        ) : Event()

        data class RainThresholdChanged(
            val value: Float,
        ) : Event()

        data class SaveSettingsClicked(
            val selectedAlertType: WeatherAlertCategory,
            val snowThreshold: Float,
            val rainThreshold: Float,
        ) : Event()

        data class SearchQueryChanged(
            val query: String,
        ) : Event()

        data class OnCitySelected(
            val city: City,
        ) : Event()

        data class OnReminderNotesUpdated(
            val notes: String,
        ) : Event()

        data object GoBack : Event()

        data object ForecastServiceIconClicked : Event()
    }
}

class AddWeatherAlertPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: AddNewWeatherAlertScreen,
        private val preferencesManager: PreferencesManager,
        private val database: AppDatabase,
        private val weatherRepository: WeatherRepository,
        private val analytics: Analytics,
    ) : Presenter<AddNewWeatherAlertScreen.State> {
        @Composable
        override fun present(): AddNewWeatherAlertScreen.State {
            val scope = rememberCoroutineScope()
            val keyboardController = LocalSoftwareKeyboardController.current
            var updatedSnowThreshold by remember { mutableFloatStateOf(DEFAULT_SNOW_THRESHOLD) }
            var updatedRainThreshold by remember { mutableFloatStateOf(DEFAULT_RAIN_THRESHOLD) }
            var suggestions: List<City> by remember { mutableStateOf(emptyList()) }
            var isSaveButtonEnabled by remember { mutableStateOf(false) }
            var isApiCallInProgress by remember { mutableStateOf(false) }
            var selectedCity: City? by remember { mutableStateOf(null) }
            var selectedApiService by remember { mutableStateOf<WeatherService?>(null) }
            var snackbarData: SnackbarData? by remember { mutableStateOf(null) }
            var reminderNotes: String = ""

            LaunchedEffect(Unit) {
                preferencesManager.preferredWeatherService.collect { service ->
                    Timber.d("Active weather service from preferences: $service")
                    selectedApiService = service
                }
            }

            LaunchedImpressionEffect {
                analytics.logScreenView(AddNewWeatherAlertScreen::class)
            }

            return AddNewWeatherAlertScreen.State(
                selectedApiService = selectedApiService,
                citySuggestions = suggestions,
                snowThreshold = updatedSnowThreshold,
                rainThreshold = updatedRainThreshold,
                isAllInputValid = isSaveButtonEnabled,
                isApiCallInProgress = isApiCallInProgress,
                snackbarData = snackbarData,
            ) { event ->
                when (event) {
                    is AddNewWeatherAlertScreen.Event.RainThresholdChanged -> {
                        updatedRainThreshold = event.value
                    }

                    is AddNewWeatherAlertScreen.Event.SnowThresholdChanged -> {
                        updatedSnowThreshold = event.value
                    }

                    is AddNewWeatherAlertScreen.Event.SaveSettingsClicked -> {
                        Timber.d("Save settings clicked: snow=${event.snowThreshold}, rain=${event.rainThreshold}")
                        isApiCallInProgress = true
                        isSaveButtonEnabled = false

                        scope.launch {
                            // ❌ Wrong, should show toast message instead that you must select from dropdown
                            val city = selectedCity ?: throw IllegalStateException("City not selected")

                            val dailyForecast =
                                weatherRepository.getDailyForecast(
                                    cityId = city.id,
                                    latitude = city.lat,
                                    longitude = city.lng,
                                    skipCache = true,
                                )
                            when (dailyForecast) {
                                is ApiResult.Success -> {
                                    database.alertDao().insertAlert(
                                        Alert(
                                            cityId = city.id,
                                            alertCategory = event.selectedAlertType,
                                            threshold =
                                                when (event.selectedAlertType) {
                                                    WeatherAlertCategory.SNOW_FALL -> event.snowThreshold
                                                    WeatherAlertCategory.RAIN_FALL -> event.rainThreshold
                                                },
                                            notes = reminderNotes,
                                        ),
                                    )

                                    // Finally after saving, navigate back
                                    navigator.pop()
                                }
                                is ApiResult.Failure.ApiFailure -> {
                                    Timber.w("API error, failed to save alert settings")
                                    isApiCallInProgress = false
                                    isSaveButtonEnabled = true
                                    snackbarData =
                                        SnackbarData(message = "Failed to save alert settings. Please try again later.") {}
                                }
                                is ApiResult.Failure.HttpFailure -> {
                                    Timber.w("HTTP error, failed to save alert settings")
                                    isApiCallInProgress = false
                                    isSaveButtonEnabled = true
                                    when (dailyForecast.code) {
                                        OpenWeatherService.ERROR_HTTP_UNAUTHORIZED -> {
                                            snackbarData =
                                                SnackbarData(
                                                    message =
                                                        "The weather API is is likely exhausted or not active. " +
                                                            "Please add your own API key.",
                                                    actionLabel = "Add Key",
                                                ) {
                                                    navigator.goTo(
                                                        BringYourOwnApiKeyScreen(
                                                            weatherApiService = selectedApiService!!,
                                                            isOriginatedFromError = true,
                                                        ),
                                                    )
                                                }
                                        }
                                        TomorrowIoService.ERROR_HTTP_FORBIDDEN -> {
                                            snackbarData =
                                                SnackbarData(
                                                    message =
                                                        "The weather API is is likely exhausted or not active. " +
                                                            "Please add your own API key.",
                                                    actionLabel = "Add Key",
                                                ) {
                                                    navigator.goTo(
                                                        BringYourOwnApiKeyScreen(
                                                            weatherApiService = selectedApiService!!,
                                                            isOriginatedFromError = true,
                                                        ),
                                                    )
                                                }
                                        }
                                        OpenWeatherService.ERROR_HTTP_NOT_FOUND -> {
                                            snackbarData =
                                                SnackbarData(
                                                    message =
                                                        "Weather API is unable to find forecast for the city you have selected. " +
                                                            "Please try different nearby city.",
                                                ) {}
                                        }
                                        OpenWeatherService.ERROR_HTTP_TOO_MANY_REQUESTS -> {
                                            snackbarData =
                                                SnackbarData(
                                                    message = "This public API key rate limit exceed. Please add your own API key.",
                                                    actionLabel = "Add Key",
                                                ) {
                                                    navigator.goTo(
                                                        BringYourOwnApiKeyScreen(
                                                            weatherApiService = selectedApiService!!,
                                                            isOriginatedFromError = true,
                                                        ),
                                                    )
                                                }
                                        }
                                        else -> {
                                            snackbarData =
                                                SnackbarData(message = "Failed to save alert settings. Please try again later.") {}
                                        }
                                    }
                                }
                                is ApiResult.Failure.NetworkFailure -> {
                                    Timber.e(dailyForecast.error, "Network error, failed to save alert settings")
                                    isApiCallInProgress = false
                                    isSaveButtonEnabled = true
                                    snackbarData =
                                        SnackbarData(message = "Network error. Please check your internet connection.") {}
                                }
                                is ApiResult.Failure.UnknownFailure -> {
                                    Timber.e(dailyForecast.error, "Unknown failure, failed to save alert settings")
                                    isApiCallInProgress = false
                                    isSaveButtonEnabled = true
                                    snackbarData =
                                        SnackbarData(message = "Failed to save alert settings. Please try again later.") {}
                                }
                            }
                        }
                    }

                    is AddNewWeatherAlertScreen.Event.SearchQueryChanged -> {
                        scope.launch {
                            database
                                .cityDao()
                                .searchCitiesByNameStartingWith(
                                    searchQuery = event.query,
                                    limit = 20,
                                ).collect {
                                    suggestions = it
                                }
                        }
                    }

                    is AddNewWeatherAlertScreen.Event.OnCitySelected -> {
                        Timber.d("Selected city: ${event.city}")
                        selectedCity = event.city
                        isSaveButtonEnabled = true
                        snackbarData = null

                        // Hide the on-screen keyboard
                        keyboardController?.hide()
                    }

                    is AddNewWeatherAlertScreen.Event.OnReminderNotesUpdated -> {
                        reminderNotes = event.notes
                    }

                    AddNewWeatherAlertScreen.Event.GoBack -> {
                        snackbarData = null
                        navigator.pop()
                    }

                    AddNewWeatherAlertScreen.Event.ForecastServiceIconClicked -> {
                        navigator.goTo(UserSettingsScreen("change-service"))
                    }
                }
            }
        }

        @CircuitInject(AddNewWeatherAlertScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: AddNewWeatherAlertScreen,
            ): AddWeatherAlertPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AddNewWeatherAlertScreen::class, AppScope::class)
@Composable
fun AddNewWeatherAlertScreen(
    state: AddNewWeatherAlertScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Alerts") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AddNewWeatherAlertScreen.Event.GoBack)
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
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val checkedList: SnapshotStateList<Int> = remember { mutableStateListOf<Int>() }
            var selectedIndex: Int by remember { mutableIntStateOf(0) }
            val selectedAlertCategory: WeatherAlertCategory by remember {
                derivedStateOf { WeatherAlertCategory.entries[selectedIndex] }
            }

            AnimatedVisibility(visible = state.isApiCallInProgress) {
                // Show loading indicator when API call is in progress
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.selectedApiService?.let {
                CurrentApiServiceStateUi(it, state.eventSink)
            }

            EditableCityInputDropdownMenu(
                onQueryChange = {
                    state.eventSink(AddNewWeatherAlertScreen.Event.SearchQueryChanged(it))
                },
                suggestions = state.citySuggestions,
                onSuggestionClick = {
                    state.eventSink(AddNewWeatherAlertScreen.Event.OnCitySelected(it))
                },
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WeatherAlertCategory.entries.forEachIndexed { index, alertCategory ->
                    SegmentedButton(
                        shape =
                            SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = WeatherAlertCategory.entries.size,
                            ),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = index in checkedList) {
                                Icon(
                                    painter = painterResource(id = alertCategory.iconRes()),
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                )
                            }
                        },
                        onClick = { selectedIndex = index },
                        selected = index == selectedIndex,
                    ) { Text(alertCategory.label) }
                }
            }

            Crossfade(targetState = selectedIndex, label = "threshold-slider") { thresholdIndex ->
                when (thresholdIndex) {
                    0 ->
                        Column {
                            // Snow Threshold Slider
                            Text(
                                text = "Snowfall Threshold: ${"%.1f".format(
                                    state.snowThreshold,
                                )} ${WeatherAlertCategory.SNOW_FALL.unit}",
                            )
                            // https://m3.material.io/components/sliders/overview
                            // https://developer.android.com/develop/ui/compose/components/slider?hl=en
                            Slider(
                                value = state.snowThreshold,
                                onValueChange = {
                                    state.eventSink(
                                        AddNewWeatherAlertScreen.Event.SnowThresholdChanged(
                                            value = it,
                                        ),
                                    )
                                },
                                valueRange = 10f..100f,
                            )
                        }
                    1 ->
                        Column {
                            // Rain Threshold Slider
                            Text(
                                text = "Rainfall Threshold: ${"%.1f".format(
                                    state.rainThreshold,
                                )} ${WeatherAlertCategory.RAIN_FALL.unit}",
                            )
                            // https://m3.material.io/components/sliders/overview
                            // https://developer.android.com/develop/ui/compose/components/slider?hl=en
                            Slider(
                                value = state.rainThreshold,
                                onValueChange = {
                                    state.eventSink(
                                        AddNewWeatherAlertScreen.Event.RainThresholdChanged(
                                            value = it,
                                        ),
                                    )
                                },
                                valueRange = 1f..50f,
                            )
                        }
                }
            }

            NotificationPermissionStatusUi()

            ReminderNotesUi {
                state.eventSink(AddNewWeatherAlertScreen.Event.OnReminderNotesUpdated(notes = it))
            }

            Button(
                enabled = state.isAllInputValid,
                onClick = {
                    state.eventSink(
                        AddNewWeatherAlertScreen.Event.SaveSettingsClicked(
                            selectedAlertType = selectedAlertCategory,
                            snowThreshold = state.snowThreshold,
                            rainThreshold = state.rainThreshold,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Alert Settings")
            }
        }
    }

    LaunchedEffect(state.snackbarData) {
        val data = state.snackbarData
        if (data != null) {
            val snackbarResult = snackbarHostState.showSnackbar(data.message, data.actionLabel)
            when (snackbarResult) {
                SnackbarResult.Dismissed -> {
                    Timber.d("Snackbar dismissed")
                }

                SnackbarResult.ActionPerformed -> {
                    data.action()
                }
            }
        } else {
            Timber.d("Snackbar data is null - hide")
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }
}

@Composable
private fun CurrentApiServiceStateUi(
    weatherService: WeatherService,
    eventSink: (AddNewWeatherAlertScreen.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val serviceConfig = weatherService.serviceConfig()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text("ℹ️ Forecast data source: ", style = MaterialTheme.typography.labelSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { eventSink(AddNewWeatherAlertScreen.Event.ForecastServiceIconClicked) },
        ) {
            Image(
                painter = painterResource(id = serviceConfig.logoResId),
                modifier =
                    Modifier
                        .size(serviceConfig.logoWidth * 0.7f, serviceConfig.logoHeight * 0.7f),
                contentDescription = "${weatherService.name} logo image",
            )
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Change forecast service",
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                modifier =
                    Modifier
                        .size(14.dp),
            )
        }
    }
}

@Composable
fun ReminderNotesUi(onValueChange: (String) -> Unit) {
    var reminderNotes by remember { mutableStateOf("") }
    OutlinedTextField(
        value = reminderNotes,
        onValueChange = {
            reminderNotes = it
            onValueChange(it)
        },
        label = { Text("Reminder Notes") },
        modifier = Modifier.fillMaxWidth(),
        supportingText = { Text("📝 Some markdown syntax are supported: **bold**, _italic_ and * list-item.") },
        placeholder = {
            Text(
                text = "(Optional) Notes that will show up in the alert notification.",
                style = MaterialTheme.typography.labelSmall,
            )
        },
        singleLine = false,
        minLines = 3,
        maxLines = 5,
    )
}

/**
 * Composable function to display the UI for notification permission status.
 * It handles the permission request and updates the UI based on the permission status.
 */
@Composable
fun NotificationPermissionStatusUi() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission(context)) }
    var labelText by remember { mutableStateOf("") }
    var showPermissionGrantedRow by remember { mutableStateOf(false) }

    LaunchedEffect(permissionGranted) {
        labelText = if (permissionGranted) "Notification status" else "Turn on notification?"
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with notifications
                permissionGranted = true
                // labelText = "Notification permission granted"
                showPermissionGrantedRow = true
            } else {
                // Permission denied, handle accordingly
                permissionGranted = false
                labelText = "Denied notification permission"
            }
        }

    if (requiresNotificationPermission()) {
        when {
            hasNotificationPermission(context) -> {
                // Permission already granted, proceed with notifications status row
                if (showPermissionGrantedRow) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = labelText)
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = null,
                        )
                    }
                }
            }
            else -> {
                // Request the permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = labelText)
                    Switch(
                        checked = permissionGranted,
                        onCheckedChange = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableCityInputDropdownMenu(
    onQueryChange: (String) -> Unit,
    suggestions: List<City>,
    onSuggestionClick: (City) -> Unit,
) {
    val textFieldState: TextFieldState = rememberTextFieldState()
    var lastSelectedCity by remember { mutableStateOf<City?>(null) }

    // The text that the user inputs into the text field can be used to filter the options.
    // This sample uses string subsequence matching.
    val filteredOptions = suggestions

    val (allowExpanded, setExpanded) = remember { mutableStateOf(false) }
    val expanded = allowExpanded && filteredOptions.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = setExpanded,
    ) {
        OutlinedTextField(
            value = textFieldState.text.toString(),
            onValueChange = { newValue ->
                lastSelectedCity = null
                textFieldState.setTextAndPlaceCursorAtEnd(newValue)
                onQueryChange(newValue)
                setExpanded(newValue.isNotEmpty())
            },
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. An editable text field has
            // the anchor type `PrimaryEditable`.
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(PrimaryEditable),
            label = { Text("City") },
            placeholder = { Text("Search and select city...") },
            singleLine = true,
            // Commented because of grey tint color in the box
            // colors = ExposedDropdownMenuDefaults.textFieldColors(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.location_city_24dp),
                    contentDescription = "City Building Icon",
                )
            },
            trailingIcon = {
                lastSelectedCity?.let {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Clear selected city",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier.clickable {
                                textFieldState.setTextAndPlaceCursorAtEnd("")
                                onQueryChange("")
                                setExpanded(false)
                                lastSelectedCity = null
                            },
                    )
                }
            },
        )
        ExposedDropdownMenu(
            modifier = Modifier.heightIn(max = 280.dp),
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
        ) {
            filteredOptions.forEach { city ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = city.cityName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(text = "${city.city}, ${city.provStateName}, ${city.country}", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(city.cityName)
                        setExpanded(false)
                        lastSelectedCity = city
                        onSuggestionClick(city)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private fun requiresNotificationPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private fun hasNotificationPermission(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun AddWeatherAlertScreenPreview() {
    WeatherAlertAppTheme {
        AddNewWeatherAlertScreen(
            AddNewWeatherAlertScreen.State(
                selectedApiService = WeatherService.OPEN_WEATHER_MAP,
                citySuggestions = emptyList(),
                snowThreshold = 5.0f,
                rainThreshold = 10.0f,
                isAllInputValid = true,
                isApiCallInProgress = true,
            ) {},
        )
    }
}
