package dev.hossain.weatheralert.circuit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.eithernet.ApiResult
import com.slack.eithernet.exceptionOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.data.AlertTileData
import dev.hossain.weatheralert.data.ConfiguredAlerts
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherAlertCategory
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class CurrentWeatherAlertScreen(
    val id: String,
) : Screen {
    data class State(
        val tiles: List<AlertTileData>,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object OnItemClicked : Event()

        data object AddNewAlertClicked : Event()
    }
}

class CurrentWeatherAlertPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: CurrentWeatherAlertScreen,
        private val preferencesManager: PreferencesManager,
        private val weatherRepository: WeatherRepository,
    ) : Presenter<CurrentWeatherAlertScreen.State> {
        @Composable
        override fun present(): CurrentWeatherAlertScreen.State {
            var weatherTiles by remember { mutableStateOf(emptyList<AlertTileData>()) }

            LaunchedEffect(Unit) {
                preferencesManager.userConfiguredAlerts
                    .map { configuredAlerts: ConfiguredAlerts ->
                        Timber.d("Found ${configuredAlerts.alerts.size} configuredAlerts.")
                        val alertTileData = mutableListOf<AlertTileData>()
                        configuredAlerts.alerts.forEach { alert ->
                            val apiResult =
                                weatherRepository.getDailyForecast(
                                    latitude = alert.lat,
                                    longitude = alert.lon,
                                    apiKey = BuildConfig.WEATHER_API_KEY,
                                )

                            when (apiResult) {
                                is ApiResult.Success -> {
                                    val snowStatus = apiResult.value.daily[1].snowVolume ?: 0.0
                                    val rainStatus = apiResult.value.daily[1].rainVolume ?: 0.0
                                    alertTileData.add(
                                        AlertTileData(
                                            category = "${alert.alertCategory}",
                                            threshold = "${alert.threshold} ${alert.alertCategory.unit}",
                                            currentStatus = "Tomorrow: ${
                                                if (alert.alertCategory == WeatherAlertCategory.SNOW_FALL) {
                                                    snowStatus
                                                } else {
                                                    rainStatus
                                                }} ${alert.alertCategory.unit}",
                                        ),
                                    )
                                }
                                is ApiResult.Failure -> {
                                    Timber.d("Error fetching weather data: ${apiResult.exceptionOrNull()}")
                                }
                            }
                        }
                        alertTileData
                    }.collect { tileData: List<AlertTileData> ->
                        Timber.d("Found weather data: ${tileData.size} items.")
                        weatherTiles = tileData
                    }
            }

            return CurrentWeatherAlertScreen.State(weatherTiles) { event ->
                when (event) {
                    CurrentWeatherAlertScreen.Event.OnItemClicked -> TODO()
                    CurrentWeatherAlertScreen.Event.AddNewAlertClicked -> {
                        navigator.goTo(AlertSettingsScreen("add-new-alert"))
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
    WeatherAlertAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Weather Alerts") })
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
            content = { padding ->
                Column(
                    modifier =
                        modifier
                            .fillMaxSize()
                            .padding(padding),
                ) {
                    AlertTileGrid(tiles = state.tiles)
                }
            },
        )
    }
}

@Composable
fun AlertTileGrid(tiles: List<AlertTileData>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(tiles) { tile ->
            AlertTile(data = tile, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AlertTile(
    data: AlertTileData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                text = data.category,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Threshold: ${data.threshold}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.currentStatus,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 1. Smooth Animations for Alerts
 *
 *     Purpose: Draw attention to new alerts or updates in the app.
 *     Implementation:
 *         Use animated transitions in Jetpack Compose when new data arrives.
 *         Use a pulse effect or fade-in animation for alert tiles.
 */
@Composable
fun AlertTileEnhanced(
    category: String,
    threshold: String,
    status: String,
    isUpdated: Boolean,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isUpdated) {
        if (isUpdated) {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            )
            scale.animateTo(1f, animationSpec = tween(200))
        }
    }

    Card(
        modifier =
            Modifier
                .scale(scale.value)
                .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = category, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Threshold: $threshold", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: $status", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CurrentWeatherAlertsPreview() {
    val sampleTiles =
        listOf(
            AlertTileData("Snowfall", "5 cm", "Tomorrow: 7 cm"),
            AlertTileData("Rainfall", "10 mm", "Tomorrow: 12 mm"),
        )
    CurrentWeatherAlerts(CurrentWeatherAlertScreen.State(sampleTiles) {})
}
