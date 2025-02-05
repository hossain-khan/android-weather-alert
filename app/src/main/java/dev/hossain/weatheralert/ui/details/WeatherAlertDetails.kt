package dev.hossain.weatheralert.ui.details

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import dev.hossain.weatheralert.data.SnackbarData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.db.ALERT_ID_NONE
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.CityForecast
import dev.hossain.weatheralert.db.CityForecastDao
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.util.convertIsoToHourAmPm
import dev.hossain.weatheralert.util.formatTimestampToElapsedTime
import dev.hossain.weatheralert.util.formatToDate
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.parseMarkdown
import dev.hossain.weatheralert.util.slimTimeLabel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import kotlin.random.Random

/**
 * Screen to show details of a weather alert.
 */
@Parcelize
data class WeatherAlertDetailsScreen(
    val alertId: Long,
) : Screen {
    data class State(
        val alertConfig: Alert?,
        val cityInfo: City?,
        val cityForecast: CityForecast?,
        val alertNote: String,
        val isEditingNote: Boolean,
        val isForecastRefreshing: Boolean,
        val snackbarData: SnackbarData?,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class EditNoteChanged(
            val note: String,
        ) : Event()

        data object DeleteAlert : Event()

        data object RefreshForecast : Event()

        data object SaveNote : Event()

        data object GoBack : Event()
    }
}

class WeatherAlertDetailsPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: WeatherAlertDetailsScreen,
        private val alertDao: AlertDao,
        private val cityForecastDao: CityForecastDao,
        private val weatherRepository: WeatherRepository,
        private val analytics: Analytics,
    ) : Presenter<WeatherAlertDetailsScreen.State> {
        @Composable
        override fun present(): WeatherAlertDetailsScreen.State {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var alertNote by remember { mutableStateOf("") }
            var alertCity by remember { mutableStateOf<City?>(null) }
            var alertConfig by remember { mutableStateOf<Alert?>(null) }
            var cityForecast by remember { mutableStateOf<CityForecast?>(null) }
            var isEditingNote by remember { mutableStateOf(false) }
            var isForecastRefreshing by remember { mutableStateOf(false) }
            var snackbarData: SnackbarData? by remember { mutableStateOf(null) }

            LaunchedImpressionEffect {
                val city = alertDao.getAlertWithCity(screen.alertId).city
                analytics.logScreenView(WeatherAlertDetailsScreen::class)
                analytics.logCityDetails(city.id, city.cityName)
            }

            LaunchedEffect(Unit) {
                val alert: UserCityAlert = alertDao.getAlertWithCity(screen.alertId)
                alertConfig = alert.alert
                alertNote = alert.alert.notes
                alertCity = alert.city

                cityForecastDao
                    .getCityForecastByAlertIdAndCityIdFlow(alertId = alert.alert.id, cityId = alert.city.id)
                    .collect { newForecast ->
                        // Update forecast data when new data is available.
                        // Data is updated on initial load and when user triggers refresh.
                        // 🧪 TEST REFRESH: ?.copy(dailyCumulativeRain = Random.nextDouble() * 100, dailyCumulativeSnow = Random.nextDouble() * 100)
                        cityForecast = newForecast

                        isForecastRefreshing = false
                    }
            }

            return WeatherAlertDetailsScreen.State(
                alertConfig = alertConfig,
                cityInfo = alertCity,
                cityForecast = cityForecast,
                alertNote = alertNote,
                isEditingNote = isEditingNote,
                isForecastRefreshing = isForecastRefreshing,
                snackbarData = snackbarData,
            ) { event ->
                when (event) {
                    is WeatherAlertDetailsScreen.Event.EditNoteChanged -> {
                        isEditingNote = true
                        alertNote = event.note
                    }
                    WeatherAlertDetailsScreen.Event.SaveNote -> {
                        scope.launch {
                            alertDao.updateAlertNote(screen.alertId, alertNote)
                            isEditingNote = false
                        }
                    }
                    WeatherAlertDetailsScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    WeatherAlertDetailsScreen.Event.DeleteAlert -> {
                        scope.launch {
                            alertDao.deleteAlertById(screen.alertId)
                            navigator.pop()
                        }
                    }

                    WeatherAlertDetailsScreen.Event.RefreshForecast -> {
                        isForecastRefreshing = true
                        scope.launch {
                            val currentForecast: CityForecast? = cityForecast
                            val city: City? = alertCity
                            if (city != null && currentForecast != null) {
                                val result =
                                    weatherRepository.getDailyForecast(
                                        alertId = screen.alertId,
                                        cityId = city.id,
                                        latitude = city.lat,
                                        longitude = city.lng,
                                        skipCache = true,
                                    )
                                if (result is ApiResult.Success) {
                                    // NOTE: This will trigger LaunchedEffect to update forecast data automatically.
                                    Timber.d("Refreshed forecast data: $result")
                                    snackbarData = SnackbarData("Forecast data has been refreshed.") {}
                                } else {
                                    Timber.e("Failed to refresh forecast data: $result")
                                    snackbarData = SnackbarData("Oops! Failed to refresh forecast data.") {}
                                }
                            }
                            isForecastRefreshing = false
                        }
                    }
                }
            }
        }

        @CircuitInject(WeatherAlertDetailsScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: WeatherAlertDetailsScreen,
            ): WeatherAlertDetailsPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(WeatherAlertDetailsScreen::class, AppScope::class)
@Composable
fun WeatherAlertDetailsScreen(
    state: WeatherAlertDetailsScreen.State,
    modifier: Modifier = Modifier,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Details") },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(WeatherAlertDetailsScreen.Event.GoBack) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        state.eventSink(WeatherAlertDetailsScreen.Event.DeleteAlert)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete alert",
                        )
                    }
                    IconButton(onClick = {
                        state.eventSink(WeatherAlertDetailsScreen.Event.RefreshForecast)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Forecast Data",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPaddingValues ->
        // https://developer.android.com/reference/kotlin/androidx/compose/material3/pulltorefresh/package-summary
        PullToRefreshBox(
            modifier = Modifier.padding(contentPaddingValues),
            state = pullToRefreshState,
            isRefreshing = state.isForecastRefreshing,
            onRefresh = {
                state.eventSink(WeatherAlertDetailsScreen.Event.RefreshForecast)
            },
        ) {
            LazyColumn(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val alert = state.alertConfig
                val city = state.cityInfo
                val cityForecast = state.cityForecast

                if (alert == null || city == null || cityForecast == null) {
                    item {
                        Timber.d("Loading alerts info...")
                        // Add loading indicator
                        CircularProgressIndicator()
                    }
                } else {
                    item { CityInfoUi(city = city) }
                    item { WeatherAlertForecastUi(alert = alert, forecast = cityForecast) }
                    item { WeatherAlertNoteUi(state = state) }
                    item { WeatherAlertUpdateOnUi(forecast = cityForecast) }
                    item { WeatherForecastSourceUi(forecastSourceService = cityForecast.forecastSourceService) }
                }
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
fun CityInfoUi(
    city: City,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Text(
            text = "Alert City",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
            // modifier = Modifier.align(Alignment.End),
        )
        Card(
            modifier =
                modifier
                    .fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.location_city_24dp),
                    contentDescription = "Alert Category",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(city.city, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${city.provStateName?.let { "$it, " } ?: ""}${city.country}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherAlertForecastUi(
    alert: Alert,
    forecast: CityForecast,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary,
        targetValue = MaterialTheme.colorScheme.error,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    val iconTint: Color =
        if (forecast.dailyCumulativeSnow > alert.threshold || forecast.dailyCumulativeRain > alert.threshold) {
            animatedColor
        } else {
            MaterialTheme.colorScheme.primary
        }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Alert Configuration",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
        )
        Card(
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = alert.alertCategory.iconRes()),
                    contentDescription = "Alert Category",
                    tint = iconTint,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alert Type: ${alert.alertCategory.label}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Threshold: ${alert.threshold.formatUnit(alert.alertCategory.unit)}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Next $CUMULATIVE_DATA_HOURS_24 Hours: ${when (alert.alertCategory) {
                            WeatherAlertCategory.SNOW_FALL -> forecast.dailyCumulativeSnow.formatUnit(alert.alertCategory.unit)
                            WeatherAlertCategory.RAIN_FALL -> forecast.dailyCumulativeRain.formatUnit(alert.alertCategory.unit)
                        }}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Tomorrow: ${when (alert.alertCategory) {
                            WeatherAlertCategory.SNOW_FALL -> forecast.nextDaySnow.formatUnit(alert.alertCategory.unit)
                            WeatherAlertCategory.RAIN_FALL -> forecast.nextDayRain.formatUnit(alert.alertCategory.unit)
                        }}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (forecast.hourlyPrecipitation
                            .take(CUMULATIVE_DATA_HOURS_24)
                            .any {
                                (alert.alertCategory == WeatherAlertCategory.RAIN_FALL && it.rain > 0) ||
                                    (alert.alertCategory == WeatherAlertCategory.SNOW_FALL && it.snow > 0)
                            }
                    ) {
                        // Show precipitation chart only if there is any precipitation data.
                        Spacer(modifier = Modifier.height(16.dp))
                        PrecipitationChartUi(alert.alertCategory, forecast.hourlyPrecipitation)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrecipitationChartUi(
    weatherAlertCategory: WeatherAlertCategory,
    precipitationValues: List<HourlyPrecipitation>,
    modifier: Modifier = Modifier,
) {
    val chartItems = if (precipitationValues.size > CUMULATIVE_DATA_HOURS_24) CUMULATIVE_DATA_HOURS_24 else precipitationValues.size
    val maxRainValue = precipitationValues.maxOfOrNull { it.rain } ?: 100.0
    val maxSnowValue = precipitationValues.maxOfOrNull { it.snow } ?: 100.0

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bar_chart_24dp),
                contentDescription = "Bar Chart Icon",
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "24 Hour Forecast",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                    ).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(chartItems) { itemIndex ->
                val value =
                    if (weatherAlertCategory ==
                        WeatherAlertCategory.SNOW_FALL
                    ) {
                        precipitationValues[itemIndex].snow
                    } else {
                        precipitationValues[itemIndex].rain
                    }

                BarChartItem(
                    // Convert ISO 8601 date-time string to hour of the day.
                    hourOfDayLabel =
                        slimTimeLabel(
                            hourOfDayLabel =
                                convertIsoToHourAmPm(
                                    isoDateTime = precipitationValues[itemIndex].isoDateTime,
                                ),
                        ),
                    value = value,
                    maxValue =
                        if (weatherAlertCategory ==
                            WeatherAlertCategory.SNOW_FALL
                        ) {
                            maxSnowValue
                        } else {
                            maxRainValue
                        },
                )
            }
        }
    }
}

/**
 * Single bar containing labels for hour and precipitation value.
 */
@Composable
private fun BarChartItem(
    hourOfDayLabel: String,
    value: Double,
    maxValue: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier =
            modifier
                .height(120.dp)
                .width(30.dp),
    ) {
        Text(
            text = if (value == 0.0) "•" else "%.1f".format(value),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            modifier =
                Modifier
                    .padding(bottom = 8.dp)
                    .rotate(270f),
        )
        Box(
            modifier =
                Modifier
                    // Use 0.7 multiplier to avoid full height bar and keep space for labels
                    .fillMaxHeight(fraction = (value / maxValue).toFloat() * 0.7f)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    ),
        )
        Text(
            text = hourOfDayLabel,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun WeatherAlertNoteUi(
    state: WeatherAlertDetailsScreen.State,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Text(
            text = "Alert Note",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
        )
        Card(
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(R.drawable.edit_note_24dp),
                    contentDescription = "Alert Category",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    val text: AnnotatedString =
                        if (state.alertNote.isNotEmpty()) {
                            parseMarkdown(state.alertNote)
                        } else {
                            buildAnnotatedString { append("No note added.") }
                        }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        // Extra padding for the icon on the right, to avoid overlap
                        modifier = Modifier.padding(end = 24.dp, bottom = 8.dp),
                    )
                    if (state.isEditingNote) {
                        OutlinedTextField(
                            value = state.alertNote,
                            onValueChange = {
                                state.eventSink(
                                    WeatherAlertDetailsScreen.Event.EditNoteChanged(note = it),
                                )
                            },
                            label = {
                                Text(
                                    if (state.alertNote.isEmpty()) {
                                        "Add reminder note"
                                    } else {
                                        "Edit reminder note"
                                    },
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ElevatedButton(
                            onClick = { state.eventSink(WeatherAlertDetailsScreen.Event.SaveNote) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Save Note")
                        }
                    } else {
                        ElevatedButton(
                            onClick = {
                                state.eventSink(
                                    WeatherAlertDetailsScreen.Event.EditNoteChanged(
                                        state.alertNote,
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (state.alertNote.isEmpty()) {
                                    "Add Note"
                                } else {
                                    "Edit Note"
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherAlertUpdateOnUi(
    forecast: CityForecast,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Text(
            text = "Last Update On",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
        )
        Card(
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(R.drawable.calendar_month_24dp),
                    contentDescription = "Calendar icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = formatToDate(forecast.createdAt),
                        style = MaterialTheme.typography.bodyLarge,
                        // Extra padding for the icon on the right, to avoid overlap
                        modifier = Modifier.padding(end = 24.dp),
                    )
                    Text(
                        text = "(${formatTimestampToElapsedTime(forecast.createdAt)})",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        // Extra padding for the icon on the right, to avoid overlap
                        modifier =
                            Modifier
                                .padding(end = 24.dp)
                                .padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherForecastSourceUi(
    forecastSourceService: WeatherForecastService,
    modifier: Modifier = Modifier,
) {
    val serviceConfig = forecastSourceService.serviceConfig()
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Text(
            text = "Forecast Data Source",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
        )
        Card(
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.server_smb_share_24dp),
                    contentDescription = "Computer server icon for weather data source",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    // Name is not needed for now, because each service logo itself has text.

                    /*Text(
                        text = serviceConfig.serviceName,
                        style = MaterialTheme.typography.bodyLarge,
                        // Extra padding for the icon on the right, to avoid overlap
                        modifier = Modifier.padding(end = 24.dp),
                    )*/

                    Image(
                        painter = painterResource(id = serviceConfig.logoResId),
                        contentDescription = "Weather data source icon",
                        modifier = Modifier.size(width = serviceConfig.logoWidth, height = serviceConfig.logoHeight),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun PreviewWeatherAlertDetailsScreen() {
    WeatherAlertAppTheme {
        WeatherAlertDetailsScreen(
            state =
                WeatherAlertDetailsScreen.State(
                    alertConfig =
                        Alert(
                            id = 1,
                            cityId = 1,
                            alertCategory = WeatherAlertCategory.SNOW_FALL,
                            threshold = 75.0f,
                            notes = "Sample alert note",
                        ),
                    cityInfo =
                        City(
                            id = 1,
                            cityName = "Salt Lake City",
                            lat = 0.0,
                            lng = 0.0,
                            country = "US",
                            iso2 = "US",
                            iso3 = "USA",
                            provStateName = "California",
                            capital = "Sacramento",
                            population = 1000000,
                            city = "Salt Lake City",
                        ),
                    cityForecast =
                        CityForecast(
                            alertId = ALERT_ID_NONE,
                            cityId = 1,
                            latitude = 0.0,
                            longitude = 0.0,
                            dailyCumulativeSnow = 100.0,
                            nextDaySnow = 50.0,
                            dailyCumulativeRain = 100.0,
                            nextDayRain = 50.0,
                            forecastSourceService = WeatherForecastService.OPEN_WEATHER_MAP,
                            hourlyPrecipitation =
                                listOf(
                                    HourlyPrecipitation("2025-01-15T21:42:00Z", 5.0, 2.0),
                                    HourlyPrecipitation("2025-01-15T22:42:00Z", 3.0, 1.5),
                                    HourlyPrecipitation("2025-01-15T23:42:00Z", 4.0, 2.2),
                                    HourlyPrecipitation("2025-01-16T00:42:00Z", 6.0, 3.1),
                                    HourlyPrecipitation("2025-01-16T01:42:00Z", 0.0, 0.0),
                                    HourlyPrecipitation("2025-01-16T02:42:00Z", 0.0, 0.0),
                                    HourlyPrecipitation("2025-01-16T02:42:00Z", 0.0, 0.2),
                                    HourlyPrecipitation("2025-01-16T02:42:00Z", 0.0, 0.6),
                                    HourlyPrecipitation("2025-01-16T02:42:00Z", 0.0, 1.2),
                                ),
                        ),
                    alertNote = "Sample alert note\n* item 1\n* item 2",
                    isEditingNote = false,
                    isForecastRefreshing = false,
                    snackbarData = null,
                    eventSink = {},
                ),
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
private fun CityInfoUiPreview() {
    WeatherAlertAppTheme {
        Scaffold { paddingValues ->
            CityInfoUi(
                city =
                    City(
                        id = 1,
                        city = "Salt Lake City",
                        cityName = "Salt Lake City",
                        lat = 0.0,
                        lng = 0.0,
                        country = "US",
                        iso2 = "US",
                        iso3 = "USA",
                        provStateName = "California",
                        capital = "Sacramento",
                        population = 1000000,
                    ),
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Preview(showBackground = true, name = "PrecipitationChartUi Preview")
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "PrecipitationChartUi Dark Mode",
)
@Composable
fun PreviewPrecipitationChartUi() {
    WeatherAlertAppTheme {
        PrecipitationChartUi(
            WeatherAlertCategory.SNOW_FALL,
            precipitationValues =
                List(12) {
                    HourlyPrecipitation(
                        isoDateTime = "2025-01-15T21:42:00Z",
                        rain = Random.nextDouble() * 100,
                        snow = Random.nextDouble() * 100,
                    )
                },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "BarChartItem Preview")
@Composable
fun PreviewBarChartItem() {
    WeatherAlertAppTheme {
        BarChartItem(
            hourOfDayLabel = "12p",
            value = 62.0,
            maxValue = 70.00,
            modifier = Modifier.padding(8.dp),
        )
    }
}
