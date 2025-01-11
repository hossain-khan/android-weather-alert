package dev.hossain.weatheralert.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.SnackbarData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherService
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class UserSettingsScreen(
    val requestId: String,
) : Screen {
    data class State(
        val selectedService: WeatherService,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class ServiceSelected(val service: WeatherService) : Event()
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
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Select your preferred weather service:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
            RadioButtonGroup(
                selectedService = state.selectedService,
                onServiceSelected = { service ->
                    state.eventSink(UserSettingsScreen.Event.ServiceSelected(service))
                }
            )
        }
    }
}

@Composable
fun RadioButtonGroup(
    selectedService: WeatherService,
    onServiceSelected: (WeatherService) -> Unit,
) {
    Column {
        WeatherService.values().forEach { service ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onServiceSelected(service) }
            ) {
                RadioButton(
                    selected = selectedService == service,
                    onClick = { onServiceSelected(service) }
                )
                Text(
                    text = service.name.replace("_", " ").capitalize(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}