package dev.hossain.weatheralert.ui.details

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.iconRes
import dev.hossain.weatheralert.datamodel.CUMULATIVE_DATA_HOURS_24
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.datamodel.WeatherService
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
import dev.hossain.weatheralert.util.formatTimestampToElapsedTime
import dev.hossain.weatheralert.util.formatToDate
import dev.hossain.weatheralert.util.formatUnit
import dev.hossain.weatheralert.util.parseMarkdown
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

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
                    .getCityForecastsByCityIdFlow(cityId = alert.city.id)
                    .collect { newForecast ->
                        // Update forecast data when new data is available.
                        // Data is updated on initial load and when user triggers refresh.
                        // 🧪 TEST REFRESH: ?.copy(nextDayRain = Random.nextDouble() * 100, nextDaySnow = Random.nextDouble() * 100)
                        cityForecast = newForecast.firstOrNull()

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
                                        cityId = city.id,
                                        latitude = city.lat,
                                        longitude = city.lng,
                                        skipCache = true,
                                    )
                                if (result is ApiResult.Success) {
                                    // NOTE: This will trigger LaunchedEffect to update forecast data automatically.
                                    // TODO Show snackbar here
                                    Timber.d("Refreshed forecast data: $result")

                                    // Temp show toast for now - will be replaced with snackbar
                                    Toast.makeText(context, "Forecast data refreshed", Toast.LENGTH_SHORT).show()
                                } else {
                                    // TODO Show snackbar here
                                    Timber.e("Failed to refresh forecast data: $result")

                                    // Temp show toast for now - will be replaced with snackbar
                                    Toast.makeText(context, "Failed to refresh forecast data", Toast.LENGTH_SHORT).show()
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
                    item { WeatherAlertConfigUi(alert = alert, forecast = cityForecast) }
                    item { WeatherAlertNoteUi(state = state) }
                    item { WeatherAlertUpdateOnUi(forecast = cityForecast) }
                    item { WeatherForecastSourceUi(forecastSourceService = cityForecast.forecastSourceService) }
                }
            }
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
fun WeatherAlertConfigUi(
    alert: Alert,
    forecast: CityForecast,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth(),
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
                    tint = MaterialTheme.colorScheme.primary,
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
                        text = "Next $CUMULATIVE_DATA_HOURS_24 Hours: ${when (alert.alertCategory){
                            WeatherAlertCategory.SNOW_FALL -> forecast.dailyCumulativeSnow.formatUnit(alert.alertCategory.unit)
                            WeatherAlertCategory.RAIN_FALL -> forecast.dailyCumulativeRain.formatUnit(alert.alertCategory.unit)
                        }}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Tomorrow: ${when (alert.alertCategory){
                            WeatherAlertCategory.SNOW_FALL -> forecast.nextDaySnow.formatUnit(alert.alertCategory.unit)
                            WeatherAlertCategory.RAIN_FALL -> forecast.nextDayRain.formatUnit(alert.alertCategory.unit)
                        }}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
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
                        modifier = Modifier.padding(end = 24.dp).padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherForecastSourceUi(
    forecastSourceService: WeatherService,
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
                            cityId = 1,
                            latitude = 0.0,
                            longitude = 0.0,
                            dailyCumulativeSnow = 100.0,
                            nextDaySnow = 50.0,
                            dailyCumulativeRain = 100.0,
                            nextDayRain = 50.0,
                            forecastSourceService = WeatherService.OPEN_WEATHER_MAP,
                        ),
                    alertNote = "Sample alert note\n* item 1\n* item 2",
                    isEditingNote = false,
                    isForecastRefreshing = false,
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
