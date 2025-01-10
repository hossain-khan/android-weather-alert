package dev.hossain.weatheralert.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.WeatherAlertCategory
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.UserCityAlert
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class WeatherAlertDetailsScreen(
    val alertId: Int,
) : Screen {
    data class State(
        val alertConfig: Alert?,
        val cityInfo: City?,
        val alertNote: String,
        val isEditingNote: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class EditNoteChanged(
            val note: String,
        ) : Event()

        data object DeleteAlert : Event()

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
    ) : Presenter<WeatherAlertDetailsScreen.State> {
        @Composable
        override fun present(): WeatherAlertDetailsScreen.State {
            val scope = rememberCoroutineScope()
            var alertNote by remember { mutableStateOf("") }
            var alertCity by remember { mutableStateOf<City?>(null) }
            var alertConfig by remember { mutableStateOf<Alert?>(null) }
            var isEditingNote by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                val alert: UserCityAlert = alertDao.getAlertWithCity(screen.alertId.toInt())
                alertConfig = alert.alert
                alertNote = alert.alert.notes
                alertCity = alert.city
            }

            return WeatherAlertDetailsScreen.State(
                alertConfig = alertConfig,
                cityInfo = alertCity,
                alertNote = alertNote,
                isEditingNote = isEditingNote,
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
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val alert = state.alertConfig
            val city = state.cityInfo

            if (alert == null || city == null) {
                Timber.d("Loading alerts info...")
                // Add loading indicator
                CircularProgressIndicator()
            } else {
                Text(
                    text = "City: ${city.cityName}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Category: ${alert.alertCategory.name}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Threshold: ${alert.threshold}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Current Status: ${alert.threshold}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                if (state.isEditingNote) {
                    OutlinedTextField(
                        value = state.alertNote,
                        onValueChange = {
                            state.eventSink(
                                WeatherAlertDetailsScreen.Event.EditNoteChanged(
                                    it,
                                ),
                            )
                        },
                        label = { Text("Edit Note") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { state.eventSink(WeatherAlertDetailsScreen.Event.SaveNote) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save Note")
                    }
                } else {
                    Text(
                        text = "Note: ${state.alertNote}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = {
                            state.eventSink(
                                WeatherAlertDetailsScreen.Event.EditNoteChanged(
                                    state.alertNote,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Edit Note")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun PreviewWeatherAlertDetailsScreen() {
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
                            cityName = "Sample City",
                            lat = 0.0,
                            lng = 0.0,
                            country = "US",
                            iso2 = "US",
                            iso3 = "USA",
                            provStateName = "California",
                            capital = "Sacramento",
                            population = 1000000,
                            city = "Sample City",
                        ),
                    alertNote = "Sample alert note",
                    isEditingNote = false,
                    eventSink = {},
                ),
        )
    }
}
