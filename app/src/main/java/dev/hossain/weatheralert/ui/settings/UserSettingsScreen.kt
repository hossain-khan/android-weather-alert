package dev.hossain.weatheralert.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.data.ApiKey
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherService
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.addapikey.BringYourOwnApiKeyScreen
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.work.scheduleWeatherAlertsWork
import dev.hossain.weatheralert.work.supportedWeatherUpdateInterval
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class UserSettingsScreen(
    val requestId: String,
) : Screen {
    data class State(
        val selectedService: WeatherService,
        val selectedUpdateFrequency: Long,
        val isUserProvidedApiKeyInUse: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class ServiceSelected(
            val service: WeatherService,
        ) : Event()

        data class RemoveServiceApiKey(
            val service: WeatherService,
        ) : Event()

        data class UpdateFrequencySelected(
            val frequency: Long,
        ) : Event()

        data object AddServiceApiKey : Event()

        data object GoBack : Event()
    }
}

class UserSettingsPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: UserSettingsScreen,
        private val preferencesManager: PreferencesManager,
        private val apiKeyProvider: ApiKey,
        private val analytics: Analytics,
    ) : Presenter<UserSettingsScreen.State> {
        @Composable
        override fun present(): UserSettingsScreen.State {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var selectedService by remember { mutableStateOf(WeatherService.OPEN_WEATHER_MAP) }
            var isUserProvidedApiKeyInUse by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                preferencesManager.preferredWeatherService.collect { service ->
                    Timber.d("Active weather service from preferences: $service")
                    selectedService = service
                    isUserProvidedApiKeyInUse = apiKeyProvider.hasUserProvidedApiKey(service)
                }
            }

            LaunchedImpressionEffect {
                analytics.logScreenView(UserSettingsScreen::class)
            }

            return UserSettingsScreen.State(
                selectedService = selectedService,
                selectedUpdateFrequency = preferencesManager.preferredUpdateIntervalSync,
                isUserProvidedApiKeyInUse = isUserProvidedApiKeyInUse,
            ) { event ->
                when (event) {
                    is UserSettingsScreen.Event.ServiceSelected -> {
                        Timber.d("Selected weather service: ${event.service}")
                        selectedService = event.service
                        scope.launch {
                            preferencesManager.savePreferredWeatherService(event.service)
                        }
                    }
                    UserSettingsScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    UserSettingsScreen.Event.AddServiceApiKey -> {
                        navigator.goTo(BringYourOwnApiKeyScreen(weatherApiService = selectedService))
                    }

                    is UserSettingsScreen.Event.UpdateFrequencySelected -> {
                        Timber.d("Selected update frequency: ${event.frequency}")
                        scope.launch {
                            preferencesManager.savePreferredUpdateInterval(event.frequency)
                        }
                        scheduleWeatherAlertsWork(context = context, event.frequency)
                    }

                    is UserSettingsScreen.Event.RemoveServiceApiKey -> TODO()
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
    ) { contentPaddingValues ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WeatherUpdateFrequencyUi(
                selectedFrequency = state.selectedUpdateFrequency,
                onUpdateFrequencySelected = { frequency: Long ->
                    state.eventSink(UserSettingsScreen.Event.UpdateFrequencySelected(frequency))
                },
            )

            Text(
                text = "Select your preferred weather API service:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
            WeatherServiceSelectionGroupUi(
                selectedService = state.selectedService,
                onServiceSelected = { service ->
                    state.eventSink(UserSettingsScreen.Event.ServiceSelected(service))
                },
            )

            AddServiceApiKeyUi(
                selectedService = state.selectedService,
                isServiceApiKeyRequired = state.selectedService.serviceConfig().requiresApiKey,
                isUserProvidedApiKeyInUse = state.isUserProvidedApiKeyInUse,
                eventSink = state.eventSink,
            )
        }
    }
}

@Composable
private fun AddServiceApiKeyUi(
    selectedService: WeatherService,
    isServiceApiKeyRequired: Boolean,
    isUserProvidedApiKeyInUse: Boolean,
    eventSink: (UserSettingsScreen.Event) -> Unit,
) {
    AnimatedVisibility(
        visible = isServiceApiKeyRequired,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        Column {
            ElevatedButton(
                onClick = {
                    eventSink(UserSettingsScreen.Event.AddServiceApiKey)
                },
                modifier =
                    Modifier
                        .padding(top = 24.dp)
                        .align(Alignment.CenterHorizontally),
            ) {
                Text(if (isUserProvidedApiKeyInUse) "Modify API Service Key" else "Add API Service Key")
            }

            if (isUserProvidedApiKeyInUse) {
                ElevatedButton(
                    onClick = {
                        eventSink(UserSettingsScreen.Event.RemoveServiceApiKey(selectedService))
                    },
                    modifier =
                        Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally),
                ) {
                    Text("Remove API Key")
                }
            }

            Text(
                text =
                    buildAnnotatedString {
                        append("[Optional] Use alert service uninterrupted by adding ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("your own")
                        }
                        append(" API key for the selected service.")
                    },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .alpha(0.6f)
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun WeatherUpdateFrequencyUi(
    selectedFrequency: Long,
    onUpdateFrequencySelected: (Long) -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(supportedWeatherUpdateInterval.indexOf(selectedFrequency)) }
    val options = supportedWeatherUpdateInterval.map { "$it hours" }
    Column {
        Text(
            text = "Select how often weather should be checked for notification:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size,
                        ),
                    onClick = {
                        selectedIndex = index
                        onUpdateFrequencySelected(supportedWeatherUpdateInterval[index])
                    },
                    selected = index == selectedIndex,
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
fun WeatherServiceSelectionGroupUi(
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
            if (!service.isEnabled) {
                return@forEach
            }

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

                    val serviceConfig = service.serviceConfig()

                    Column {
                        Image(
                            painter = painterResource(id = serviceConfig.logoResId),
                            contentDescription = service.name,
                            modifier =
                                Modifier.size(
                                    width = serviceConfig.logoWidth,
                                    height = serviceConfig.logoHeight,
                                ),
                        )
                        Text(
                            text = serviceConfig.description,
                            style = MaterialTheme.typography.labelSmall,
                            modifier =
                                Modifier
                                    .alpha(0.6f)
                                    .padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun UserSettingsScreenPreview() {
    val sampleState =
        UserSettingsScreen.State(
            selectedService = WeatherService.OPEN_WEATHER_MAP,
            selectedUpdateFrequency = 12,
            isUserProvidedApiKeyInUse = true,
            eventSink = {},
        )
    WeatherAlertAppTheme {
        UserSettingsScreen(state = sampleState)
    }
}
