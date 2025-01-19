package dev.hossain.weatheralert.ui.alertslist

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import com.slack.eithernet.ApiResult
import com.slack.eithernet.exceptionOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.AlertTileData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.network.NetworkMonitor
import dev.hossain.weatheralert.ui.addalert.AddNewWeatherAlertScreen
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import dev.hossain.weatheralert.ui.settings.UserSettingsScreen
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.parseMarkdown
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class CurrentWeatherAlertScreen(
    val id: String,
) : Screen {
    data class State(
        val tiles: List<AlertTileData>,
        val recentlyDeletedAlert: AlertTileData?,
        val userMessage: String? = null,
        val isNetworkUnavailable: Boolean = false,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class AlertRemoved(
            val item: AlertTileData,
        ) : Event()

        data class OnItemClicked(
            val alertId: Long,
        ) : Event()

        data object AddNewAlertClicked : Event()

        data object MessageShown : Event()

        data object SettingsClicked : Event()

        data class UndoDelete(
            val item: AlertTileData,
        ) : Event()
    }
}

class CurrentWeatherAlertPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: CurrentWeatherAlertScreen,
        private val weatherRepository: WeatherRepository,
        private val alertDao: AlertDao,
        private val networkMonitor: NetworkMonitor,
        private val analytics: Analytics,
    ) : Presenter<CurrentWeatherAlertScreen.State> {
        @Composable
        override fun present(): CurrentWeatherAlertScreen.State {
            val scope = rememberCoroutineScope()
            var weatherTiles by remember { mutableStateOf(emptyList<AlertTileData>()) }
            var recentlyDeletedAlert by remember { mutableStateOf<AlertTileData?>(null) }
            var deletedItemIndex by remember { mutableIntStateOf(-1) }
            var forecastAlerts by remember { mutableStateOf(emptyList<UserCityAlert>()) }
            var userMessage by remember { mutableStateOf<String?>(null) }
            var isNetworkUnavailable by remember { mutableStateOf(false) }

            LaunchedImpressionEffect {
                analytics.logScreenView(CurrentWeatherAlertScreen::class)
            }

            LaunchedEffect(Unit) {
                val userCityAlerts = alertDao.getAllAlertsWithCities()

                // Saves it locally to undo delete later
                forecastAlerts = userCityAlerts
                Timber.d("Found ${userCityAlerts.size} userCityAlerts.")

                val alertTileData = mutableListOf<AlertTileData>()
                userCityAlerts.forEach { alert ->
                    val apiResult =
                        weatherRepository.getDailyForecast(
                            cityId = alert.city.id,
                            latitude = alert.city.lat,
                            longitude = alert.city.lng,
                            // Load from the cache for reduced API usage
                            // and for offline usage. DO NOT SKIP CACHE.
                            skipCache = false,
                        )

                    when (apiResult) {
                        is ApiResult.Success -> {
                            val forecastData = apiResult.value
                            val snowStatus = forecastData.snow.dailyCumulativeSnow
                            val rainStatus = forecastData.rain.dailyCumulativeRain
                            alertTileData.add(
                                AlertTileData(
                                    alertId = alert.alert.id,
                                    cityInfo = alert.city.cityName,
                                    lat = alert.city.lat,
                                    lon = alert.city.lng,
                                    category = alert.alert.alertCategory,
                                    threshold = alert.alert.threshold.formatUnit(alert.alert.alertCategory.unit),
                                    currentStatus =
                                        if (alert.alert.alertCategory == WeatherAlertCategory.SNOW_FALL) {
                                            snowStatus.formatUnit(alert.alert.alertCategory.unit)
                                        } else {
                                            rainStatus.formatUnit(alert.alert.alertCategory.unit)
                                        },
                                    isAlertActive =
                                        when (alert.alert.alertCategory) {
                                            WeatherAlertCategory.SNOW_FALL -> snowStatus > alert.alert.threshold
                                            WeatherAlertCategory.RAIN_FALL -> rainStatus > alert.alert.threshold
                                        },
                                    alertNote = alert.alert.notes,
                                ),
                            )
                        }
                        is ApiResult.Failure -> {
                            Timber.d("Error fetching weather data: ${apiResult.exceptionOrNull()}")
                        }
                    }
                }
                weatherTiles = alertTileData
            }

            LaunchedEffect(Unit) {
                // Start monitoring network
                networkMonitor.isConnected.collect { isConnected ->
                    isNetworkUnavailable = !isConnected
                }
            }

            return CurrentWeatherAlertScreen.State(
                tiles = weatherTiles,
                recentlyDeletedAlert = recentlyDeletedAlert,
                userMessage = userMessage,
                isNetworkUnavailable = isNetworkUnavailable,
            ) { event ->
                when (event) {
                    is CurrentWeatherAlertScreen.Event.OnItemClicked -> {
                        Timber.d("Alert item clicked with id: ${event.alertId}")
                        navigator.goTo(WeatherAlertDetailsScreen(event.alertId))
                    }

                    CurrentWeatherAlertScreen.Event.AddNewAlertClicked -> {
                        navigator.goTo(AddNewWeatherAlertScreen("add-new-alert"))
                    }

                    is CurrentWeatherAlertScreen.Event.AlertRemoved -> {
                        val updatedTiles = weatherTiles.toMutableList()
                        deletedItemIndex = updatedTiles.indexOf(event.item)
                        updatedTiles.remove(event.item)
                        weatherTiles = updatedTiles
                        scope.launch {
                            alertDao.deleteAlertById(event.item.alertId)
                            // Triggers alert is deleted and allows user to `UNDO`
                            recentlyDeletedAlert = event.item
                        }
                    }

                    is CurrentWeatherAlertScreen.Event.UndoDelete -> {
                        val updatedTiles = weatherTiles.toMutableList()
                        updatedTiles.add(deletedItemIndex, event.item)
                        weatherTiles = updatedTiles
                        scope.launch {
                            val alert: UserCityAlert? = forecastAlerts.find { it.alert.id == event.item.alertId }
                            alert?.let {
                                alertDao.insertAlert(it.alert)
                            } ?: run {
                                Timber.e("Undo delete failed for alertId: ${event.item.alertId}")
                            }
                        }
                    }

                    CurrentWeatherAlertScreen.Event.MessageShown -> {
                        userMessage = null
                        recentlyDeletedAlert = null
                    }

                    CurrentWeatherAlertScreen.Event.SettingsClicked -> {
                        navigator.goTo(UserSettingsScreen("settings"))
                    }
                }
            }
        }

        @CircuitInject(CurrentWeatherAlertScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: CurrentWeatherAlertScreen,
            ): CurrentWeatherAlertPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(CurrentWeatherAlertScreen::class, AppScope::class)
@Composable
fun CurrentWeatherAlerts(
    state: CurrentWeatherAlertScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Alerts") },
                actions = {
                    IconButton(onClick = {
                        state.eventSink(CurrentWeatherAlertScreen.Event.SettingsClicked)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    state.eventSink(CurrentWeatherAlertScreen.Event.AddNewAlertClicked)
                },
                text = { Text("Add Alert") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Alert") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (state.tiles.isEmpty()) {
                EmptyAlertState()
            } else {
                AlertTileGrid(
                    tiles = state.tiles,
                    eventSink = state.eventSink,
                )
            }
        }
    }
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            state.eventSink(CurrentWeatherAlertScreen.Event.MessageShown)
        }
    }

    LaunchedEffect(state.isNetworkUnavailable) {
        if (state.isNetworkUnavailable) {
            snackbarHostState.showSnackbar(
                message = "ⓘ Network is unavailable.",
                duration = SnackbarDuration.Indefinite,
            )
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(state.recentlyDeletedAlert) {
        state.recentlyDeletedAlert?.let { alert ->
            // userMessage = "Alert for ${event.item.cityInfo} removed."
            val result =
                snackbarHostState.showSnackbar(
                    message = "Alert for ${alert.cityInfo} removed.",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short,
                )
            if (result == SnackbarResult.ActionPerformed) {
                state.eventSink(CurrentWeatherAlertScreen.Event.UndoDelete(alert))
            } else if (result == SnackbarResult.Dismissed) {
                state.eventSink(CurrentWeatherAlertScreen.Event.MessageShown)
            }
        }
    }
}

@Composable
fun AlertTileGrid(
    tiles: List<AlertTileData>,
    eventSink: (CurrentWeatherAlertScreen.Event) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                vertical = MaterialTheme.dimensions.verticalScreenPadding,
                horizontal = MaterialTheme.dimensions.horizontalScreenPadding,
            ),
    ) {
        itemsIndexed(
            items = tiles,
            key = { _, item -> item.uuid },
        ) { _, alertTileData ->
            AlertTileItem(
                alertTileData = alertTileData,
                eventSink = eventSink,
                modifier =
                    Modifier
                        .animateItem()
                        .clickable {
                            eventSink(CurrentWeatherAlertScreen.Event.OnItemClicked(alertTileData.alertId))
                        },
            )
        }
    }
}

@Composable
fun AlertTileItem(
    alertTileData: AlertTileData,
    modifier: Modifier = Modifier,
    eventSink: (CurrentWeatherAlertScreen.Event) -> Unit,
) {
    val context = LocalContext.current
    val currentItem by rememberUpdatedState(alertTileData)
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart,
                    -> {
                        eventSink(CurrentWeatherAlertScreen.Event.AlertRemoved(currentItem))
                    }
                    SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                }
                return@rememberSwipeToDismissBoxState true
            },
            // positional threshold of 50%
            positionalThreshold = { it * .50f },
        )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { DismissBackground(dismissState) },
        content = {
            val cardElevation: Dp =
                animateDpAsState(
                    if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) 8.dp else 4.dp,
                    label = "card-elevation",
                ).value
            AlertListItem(
                data = alertTileData,
                cardElevation = cardElevation,
                iconResId = alertTileData.category.iconRes(),
            )
            // AlertTile(data = alertTileData, cardElevation, modifier = Modifier.fillMaxWidth())
        },
    )
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color =
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.5f)
            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.5f)
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        }

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color, shape = RoundedCornerShape(12.dp))
                .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "delete",
        )
        Spacer(modifier = Modifier)
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "delete",
        )
    }
}

@Composable
fun AlertListItem(
    data: AlertTileData,
    cardElevation: Dp,
    @DrawableRes
    iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp)
                .then(
                    if (isSystemInDarkTheme()) {
                        Modifier.border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    },
                ),
        elevation = CardDefaults.cardElevation(cardElevation),
        shape = RoundedCornerShape(12.dp),
    ) {
        val colors: ListItemColors =
            if (data.isAlertActive) {
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                )
            } else {
                ListItemDefaults.colors()
            }
        ListItem(
            headlineContent = {
                Text(
                    text =
                        when (data.category) {
                            WeatherAlertCategory.SNOW_FALL -> "Snowfall"
                            WeatherAlertCategory.RAIN_FALL -> "Rainfall"
                        },
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            supportingContent = {
                Column {
                    Text(text = "City: ${data.cityInfo}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Threshold: ${data.threshold}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Next ${CUMULATIVE_DATA_HOURS_24}H: ${data.currentStatus}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    // Future enhancement - show icon if note is available
                    // Tap tap icon - expand/collapse note content
                    if (data.alertNote.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(
                            text = parseMarkdown(data.alertNote),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            colors = colors,
            leadingContent = {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            },
            trailingContent = {
                Icon(
                    imageVector = if (data.isAlertActive) Icons.Default.WarningAmber else Icons.Default.TagFaces,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            modifier = Modifier.padding(0.dp),
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CurrentWeatherAlertsPreview() {
    val sampleTiles =
        listOf(
            AlertTileData(
                alertId = 1,
                cityInfo = "Dhaka, Bangladesh",
                lat = 0.0,
                lon = 0.0,
                category = WeatherAlertCategory.SNOW_FALL,
                threshold = "5 mm",
                currentStatus = "Tomorrow: 7 mm",
                isAlertActive = false,
                alertNote = "test note",
            ),
            AlertTileData(
                alertId = 2,
                cityInfo = "New York, USA",
                lat = 0.0,
                lon = 0.0,
                category = WeatherAlertCategory.RAIN_FALL,
                threshold = "10 mm",
                currentStatus = "Tomorrow: 12 mm",
                isAlertActive = true,
                alertNote = "Note when alert is reached.\n* Charge batteries\n* Get car in **garage**",
            ),
        )
    CurrentWeatherAlerts(
        CurrentWeatherAlertScreen.State(
            tiles = sampleTiles,
            recentlyDeletedAlert = null,
        ) {},
    )
}
