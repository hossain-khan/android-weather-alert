package dev.hossain.weatheralert.ui.alertslist

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.AlertTileData
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.network.NetworkMonitor
import dev.hossain.weatheralert.ui.about.AboutAppScreen
import dev.hossain.weatheralert.ui.about.AppCreditsScreen
import dev.hossain.weatheralert.ui.addalert.AddNewWeatherAlertScreen
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import dev.hossain.weatheralert.ui.onboarding.OnboardingScreen
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.settings.UserSettingsScreen
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.formatSnoozeUntil
import dev.hossain.weatheralert.util.formatTimestampToElapsedTime
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.parseMarkdown
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class CurrentWeatherAlertScreen(
    val id: String,
) : Screen {
    data class State(
        val tiles: List<AlertTileData>?,
        val recentlyDeletedAlert: AlertTileData?,
        val userMessage: String? = null,
        val isNetworkUnavailable: Boolean = false,
        val lastWeatherCheckTime: Long = 0L,
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

        data object AboutAppClicked : Event()

        data object CreditsClicked : Event()

        data object SendFeedbackClicked : Event()

        data object ViewOnboardingClicked : Event()

        data class LearnMoreClicked(
            val isOpened: Boolean,
        ) : Event()

        data class UndoDelete(
            val item: AlertTileData,
        ) : Event()
    }
}

@AssistedInject
class CurrentWeatherAlertPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: CurrentWeatherAlertScreen,
        private val weatherRepository: WeatherRepository,
        private val alertDao: AlertDao,
        private val networkMonitor: NetworkMonitor,
        private val analytics: Analytics,
        private val preferencesManager: PreferencesManager,
    ) : Presenter<CurrentWeatherAlertScreen.State> {
        @Composable
        override fun present(): CurrentWeatherAlertScreen.State {
            val scope = rememberCoroutineScope()
            val uriHandler = LocalUriHandler.current
            var weatherTiles by remember { mutableStateOf<List<AlertTileData>?>(null) }
            var recentlyDeletedAlert by remember { mutableStateOf<AlertTileData?>(null) }
            var deletedItemIndex by remember { mutableIntStateOf(-1) }
            var forecastAlerts by remember { mutableStateOf(emptyList<UserCityAlert>()) }
            var userMessage by remember { mutableStateOf<String?>(null) }
            var isNetworkUnavailable by remember { mutableStateOf(false) }
            var lastWeatherCheckTime by remember { mutableStateOf(0L) }

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
                    // NOTE: Currently it's a list, because everytime there is a refresh
                    // a new city forecast is added, old one is not deleted at the moment.
                    // Only the latest city forecast data is used.
                    val cityForecast = alert.latestCityForecast()

                    val apiResult =
                        weatherRepository.getDailyForecast(
                            alertId = alert.alert.id,
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
                                    forecastSourceName = cityForecast?.forecastSourceService?.serviceConfig()?.serviceName ?: "",
                                    snoozedUntil = alert.alert.snoozedUntil,
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

            LaunchedEffect(Unit) {
                // Collect last weather check time
                preferencesManager.lastWeatherCheckTime.collect { timestamp ->
                    lastWeatherCheckTime = timestamp
                }
            }

            return CurrentWeatherAlertScreen.State(
                tiles = weatherTiles,
                recentlyDeletedAlert = recentlyDeletedAlert,
                userMessage = userMessage,
                isNetworkUnavailable = isNetworkUnavailable,
                lastWeatherCheckTime = lastWeatherCheckTime,
            ) { event ->
                when (event) {
                    is CurrentWeatherAlertScreen.Event.OnItemClicked -> {
                        Timber.d("Alert item clicked with id: ${event.alertId}")
                        navigator.goTo(WeatherAlertDetailsScreen(event.alertId))
                    }

                    CurrentWeatherAlertScreen.Event.AddNewAlertClicked -> {
                        navigator.goTo(AddNewWeatherAlertScreen)
                    }

                    is CurrentWeatherAlertScreen.Event.AlertRemoved -> {
                        val updatedTiles = requireNotNull(weatherTiles).toMutableList()
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
                        val updatedTiles = requireNotNull(weatherTiles).toMutableList()
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
                        navigator.goTo(UserSettingsScreen)
                    }

                    CurrentWeatherAlertScreen.Event.AboutAppClicked -> {
                        navigator.goTo(AboutAppScreen)
                    }

                    CurrentWeatherAlertScreen.Event.CreditsClicked -> {
                        navigator.goTo(AppCreditsScreen)
                    }

                    CurrentWeatherAlertScreen.Event.SendFeedbackClicked -> {
                        // Take user to GitHub issues page to report issue or provide feedback.
                        analytics.logSendFeedback()
                        uriHandler.openUri("https://github.com/hossain-khan/android-weather-alert/issues")
                    }

                    CurrentWeatherAlertScreen.Event.ViewOnboardingClicked -> {
                        navigator.goTo(OnboardingScreen)
                    }

                    is CurrentWeatherAlertScreen.Event.LearnMoreClicked -> {
                        if (event.isOpened) {
                            analytics.logViewTutorial(isComplete = false)
                        } else {
                            analytics.logViewTutorial(isComplete = true)
                        }
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
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousScrollOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, scrollOffset) ->
                if (index > previousIndex || (index == previousIndex && scrollOffset > previousScrollOffset)) {
                    isFabVisible = false
                } else if (index < previousIndex || (index == previousIndex && scrollOffset < previousScrollOffset)) {
                    isFabVisible = true
                }
                previousIndex = index
                previousScrollOffset = scrollOffset
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Weather Alerts")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(R.drawable.weather_alert_icon_no_fill),
                            contentDescription = "Weather Alerts",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp).offset(y = (-2).dp),
                        )
                    }
                },
                actions = {
                    AppMenuItems {
                        state.eventSink(it)
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        state.eventSink(CurrentWeatherAlertScreen.Event.AddNewAlertClicked)
                    },
                    text = { Text("Add Alert") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Alert") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPaddingValues ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        ) {
            if (state.tiles == null) {
                // Show loading indicator in the middle of the screen
                LoadingAlertsProgressUi()
            } else {
                if (state.tiles.isEmpty()) {
                    EmptyAlertState(
                        onLearnMoreOpened = { state.eventSink(CurrentWeatherAlertScreen.Event.LearnMoreClicked(isOpened = true)) },
                        onLearnMoreClosed = { state.eventSink(CurrentWeatherAlertScreen.Event.LearnMoreClicked(isOpened = false)) },
                    )
                } else {
                    AlertTileGrid(
                        tiles = state.tiles,
                        eventSink = state.eventSink,
                        listState = listState,
                        lastWeatherCheckTime = state.lastWeatherCheckTime,
                    )
                }
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
    listState: LazyListState,
    lastWeatherCheckTime: Long,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // Add spacing between items (on top of list item card paddings)
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
                        .animateItem(),
            )
        }

        // Add footer with last check timestamp
        item {
            LastCheckTimestampFooter(
                lastCheckTime = lastWeatherCheckTime,
                modifier = Modifier.fillMaxWidth(),
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

                    SwipeToDismissBoxValue.Settled -> {
                        return@rememberSwipeToDismissBoxState false
                    }
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
                eventSink = eventSink,
            )
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
    eventSink: (CurrentWeatherAlertScreen.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snoozeText = formatSnoozeUntil(data.snoozedUntil)
    val isSnoozed = snoozeText != null

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(4.dp)
                .then(
                    if (isSystemInDarkTheme()) {
                        Modifier.border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    },
                ).clickable {
                    eventSink(CurrentWeatherAlertScreen.Event.OnItemClicked(data.alertId))
                },
        elevation = CardDefaults.cardElevation(cardElevation),
        shape = RoundedCornerShape(12.dp),
    ) {
        val colors: ListItemColors =
            when {
                isSnoozed -> {
                    // Use a muted color for snoozed alerts
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    )
                }

                data.isAlertActive -> {
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    )
                }

                else -> {
                    ListItemDefaults.colors()
                }
            }
        ListItem(
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text =
                            when (data.category) {
                                WeatherAlertCategory.SNOW_FALL -> "Snowfall"
                                WeatherAlertCategory.RAIN_FALL -> "Rainfall"
                            },
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    // Show snooze badge if alert is snoozed
                    if (isSnoozed) {
                        Box(
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp),
                                    ).padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Snoozed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
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

                    // Show snooze until time if snoozed
                    if (snoozeText != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.snooze_24dp),
                                contentDescription = "Alert is snoozed",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = snoozeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }

                    // Future enhancement - show icon if note is available
                    // Tap tap icon - expand/collapse note content
                    if (data.alertNote.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(
                            text = parseMarkdown(data.alertNote),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    // Show forecast source in case user has multiple alerts from different sources.
                    if (data.forecastSourceName.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.info_24dp),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Forecast Source: ${data.forecastSourceName}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            },
            colors = colors,
            leadingContent = {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    // Increased from default size to provide more vertical space
                    // And push the warning icon bit more bottom
                    modifier = Modifier.height(56.dp),
                ) {
                    // Main category icon
                    Icon(
                        painter = painterResource(iconResId),
                        contentDescription = null,
                        tint =
                            if (isSnoozed) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        modifier = Modifier.size(48.dp),
                    )

                    // Conditionally show warning icon as an overlay when alert is active (and not snoozed)
                    if (data.isAlertActive && !isSnoozed) {
                        Icon(
                            painter = painterResource(R.drawable.warning_24dp),
                            contentDescription = "Alert active",
                            tint = MaterialTheme.colorScheme.error,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp), // Slightly smaller for better proportions
                        )
                    }

                    // Show snooze icon overlay when snoozed
                    if (isSnoozed) {
                        Icon(
                            painter = painterResource(R.drawable.snooze_24dp),
                            contentDescription = "Alert snoozed",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp),
                        )
                    }
                }
            },
            modifier = Modifier.padding(0.dp),
        )
    }
}

@Composable
fun LastCheckTimestampFooter(
    lastCheckTime: Long,
    modifier: Modifier = Modifier,
) {
    if (lastCheckTime > 0) {
        Box(
            modifier =
                modifier
                    .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Last checked: ${formatTimestampToElapsedTime(lastCheckTime)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
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
                currentStatus = "7 mm",
                isAlertActive = false,
                alertNote = "Charge batteries\nGet car in **garage**",
                forecastSourceName = "Tomorrow.io",
            ),
            AlertTileData(
                alertId = 2,
                cityInfo = "New York, USA",
                lat = 0.0,
                lon = 0.0,
                category = WeatherAlertCategory.RAIN_FALL,
                threshold = "10 mm",
                currentStatus = "12 mm",
                isAlertActive = true,
                alertNote = "Note when alert is reached.\n* Charge batteries\n* Get car in **garage**",
                forecastSourceName = "Tomorrow.io",
            ),
            AlertTileData(
                alertId = 1,
                cityInfo = "Toronto, Canada",
                lat = 0.0,
                lon = 0.0,
                category = WeatherAlertCategory.SNOW_FALL,
                threshold = "25 mm",
                currentStatus = "11 mm",
                isAlertActive = true,
                alertNote = "",
                forecastSourceName = "OpenWeatherMap",
            ),
        )
    CurrentWeatherAlerts(
        CurrentWeatherAlertScreen.State(
            tiles = sampleTiles,
            recentlyDeletedAlert = null,
            lastWeatherCheckTime = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // 2 hours ago
        ) {},
    )
}
