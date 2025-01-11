package dev.hossain.weatheralert.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherService
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSettingsScreen(
    val requestId: String,
) : Screen {
    data class State(
        val selectedService: WeatherService,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class ServiceSelected(
            val service: WeatherService,
        ) : Event()

        data object GoBack : Event()
    }
}

class UserSettingsPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: UserSettingsScreen,
        private val preferencesManager: PreferencesManager,
    ) : Presenter<UserSettingsScreen.State> {
        @Composable
        override fun present(): UserSettingsScreen.State {
            val scope = rememberCoroutineScope()
            var selectedService by remember { mutableStateOf(WeatherService.OPEN_WEATHER_MAP) }

            LaunchedEffect(Unit) {
                preferencesManager.activeWeatherService.collect { service ->
                    selectedService = service
                }
            }

            return UserSettingsScreen.State(
                selectedService = selectedService,
            ) { event ->
                when (event) {
                    is UserSettingsScreen.Event.ServiceSelected -> {
                        selectedService = event.service
                        scope.launch {
                            preferencesManager.selectWeatherService(event.service)
                        }
                    }
                    UserSettingsScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(UserSettingsScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: UserSettingsScreen,
            ): UserSettingsPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(UserSettingsScreen::class, AppScope::class)
@Composable
fun UserSettingsScreen(
    state: UserSettingsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(UserSettingsScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
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
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Select your preferred weather service:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
            )
            RadioButtonGroup(
                selectedService = state.selectedService,
                onServiceSelected = { service ->
                    state.eventSink(UserSettingsScreen.Event.ServiceSelected(service))
                },
            )
        }
    }
}

@Composable
fun RadioButtonGroup(
    selectedService: WeatherService,
    onServiceSelected: (WeatherService) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WeatherService.entries.forEach { service ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onServiceSelected(service) },
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp),
                ) {
                    RadioButton(
                        selected = selectedService == service,
                        onClick = { onServiceSelected(service) },
                    )
                    Column {
                        Image(
                            painter = painterResource(id = service.logoResId()),
                            contentDescription = service.name,
                            modifier = Modifier.size(120.dp, 50.dp),
                        )
                    }
                }
            }
        }
    }
}

fun WeatherService.logoResId(): Int =
    when (this) {
        WeatherService.OPEN_WEATHER_MAP -> R.drawable.openweather_logo
        WeatherService.TOMORROW_IO -> R.drawable.tomorrow_io_logo
    }

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun UserSettingsScreenPreview() {
    val sampleState =
        UserSettingsScreen.State(
            selectedService = WeatherService.OPEN_WEATHER_MAP,
            eventSink = {},
        )
    WeatherAlertAppTheme {
        UserSettingsScreen(state = sampleState)
    }
}
