package dev.hossain.weatheralert.ui.addapikey

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
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
import dev.hossain.weatheralert.data.SnackbarData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class BringYourOwnApiKeyScreen(
    val requestId: String,
) : Screen {
    data class State(
        val apiKey: String,
        val isApiKeyValid: Boolean,
        val isApiCallInProgress: Boolean,
        val snackbarData: SnackbarData? = null,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data class ApiKeyChanged(
            val value: String,
        ) : Event()

        data class SubmitApiKey(
            val apiKey: String,
        ) : Event()

        data object GoBack : Event()
    }
}

class BringYourOwnApiKeyPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: BringYourOwnApiKeyScreen,
        private val weatherRepository: WeatherRepository,
    ) : Presenter<BringYourOwnApiKeyScreen.State> {
        @Composable
        override fun present(): BringYourOwnApiKeyScreen.State {
            val scope = rememberCoroutineScope()
            var apiKey by remember { mutableStateOf("") }
            var isApiKeyValid by remember { mutableStateOf(false) }
            var isApiCallInProgress by remember { mutableStateOf(false) }
            var snackbarData: SnackbarData? by remember { mutableStateOf(null) }

            return BringYourOwnApiKeyScreen.State(
                apiKey = apiKey,
                isApiKeyValid = isApiKeyValid,
                isApiCallInProgress = isApiCallInProgress,
                snackbarData = snackbarData,
            ) { event ->
                when (event) {
                    is BringYourOwnApiKeyScreen.Event.ApiKeyChanged -> {
                        apiKey = event.value
                        isApiKeyValid = apiKey.matches(Regex("^[a-f0-9]{32}\$"))
                    }

                    is BringYourOwnApiKeyScreen.Event.SubmitApiKey -> {
                        isApiCallInProgress = true
                        scope.launch {
                            val result = weatherRepository.isValidApiKey(apiKey)
                            isApiCallInProgress = false
                            when (result) {
                                is ApiResult.Success -> {
                                    snackbarData =
                                        SnackbarData("API key is valid") {
                                            navigator.pop()
                                        }
                                }
                                is ApiResult.Failure -> {
                                    snackbarData = SnackbarData("Invalid API key. Please try again.") {}
                                }
                            }
                        }
                    }

                    BringYourOwnApiKeyScreen.Event.GoBack -> {
                        snackbarData = null
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(BringYourOwnApiKeyScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: BringYourOwnApiKeyScreen,
            ): BringYourOwnApiKeyPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(BringYourOwnApiKeyScreen::class, AppScope::class)
@Composable
fun BringYourOwnApiKeyScreen(
    state: BringYourOwnApiKeyScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var clicked by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your API Key") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        modifier =
                            Modifier.clickable {
                                state.eventSink(BringYourOwnApiKeyScreen.Event.GoBack)
                            },
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateTopPadding(), horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text =
                    "Unfortunately, API key provided with the app has been exhausted.\n\n" +
                        "To continue to use this app, you need to provide your own API key from OpenWeatherMap.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.servers),
                    contentDescription = "City",
                    modifier = Modifier.align(Alignment.Center),
                )
                Image(
                    painter = painterResource(id = R.drawable.clouds),
                    contentDescription = "City",
                    modifier = Modifier.size(96.dp).align(Alignment.TopStart).padding(start = 24.dp),
                )
            }

            val annotatedLinkString =
                buildAnnotatedString {
                    append("Visit ")
                    withLink(
                        LinkAnnotation.Url(
                            url = "https://openweathermap.org/api",
                            styles =
                                TextLinkStyles(
                                    style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                                    hoveredStyle = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                                ),
                            linkInteractionListener = {
                                // on click...
                                if (!clicked) uriHandler.openUri("https://openweathermap.org/api")
                                clicked = true
                            },
                        ),
                    ) {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("openweathermap.org")
                        }
                    }
                    append(" to get your API key.")
                }
            Text(text = annotatedLinkString)

            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { state.eventSink(BringYourOwnApiKeyScreen.Event.ApiKeyChanged(it)) },
                label = { Text("API Key") },
                leadingIcon = {
                    if (state.isApiKeyValid) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Valid API key",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Invalid API key",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                placeholder = { Text("Enter your API key here") },
                supportingText = {
                    Text("API key should be 32 characters long and contain only hexadecimal characters.")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                enabled = state.isApiKeyValid,
                onClick = { state.eventSink(BringYourOwnApiKeyScreen.Event.SubmitApiKey(state.apiKey)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save API Key")
            }

            if (state.isApiCallInProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BringYourOwnApiKeyScreenPreview() {
    WeatherAlertAppTheme {
        BringYourOwnApiKeyScreen(
            state =
                BringYourOwnApiKeyScreen.State(
                    apiKey = "",
                    isApiKeyValid = false,
                    isApiCallInProgress = false,
                    eventSink = {},
                ),
        )
    }
}
