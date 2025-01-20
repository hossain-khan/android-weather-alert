package dev.hossain.weatheralert.ui.addapikey

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
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
import com.slack.circuitx.effects.LaunchedImpressionEffect
import com.slack.eithernet.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.ApiKey
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.SnackbarData
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.data.WeatherService
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.WeatherServiceLogoConfig
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class BringYourOwnApiKeyScreen(
    /**
     * The service API key is being added for, e.g., OpenWeatherMap, Tomorrow.io, etc.
     */
    val weatherApiService: WeatherService,
    /**
     * Indicates if API error is received and then user is navigated to this screen.
     */
    val isOriginatedFromError: Boolean = false,
) : Screen {
    data class State(
        val weatherService: WeatherService,
        val originatedFromApiError: Boolean,
        val apiKeyInput: String,
        val isApiKeyValid: Boolean,
        val isUserProvidedApiKey: Boolean,
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

/**
 * Presenter for [BringYourOwnApiKeyScreen].
 */
class BringYourOwnApiKeyPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: BringYourOwnApiKeyScreen,
        private val weatherRepository: WeatherRepository,
        private val preferencesManager: PreferencesManager,
        private val apiKeyProvider: ApiKey,
        private val analytics: Analytics,
    ) : Presenter<BringYourOwnApiKeyScreen.State> {
        @Composable
        override fun present(): BringYourOwnApiKeyScreen.State {
            val scope = rememberCoroutineScope()
            var apiKey by remember { mutableStateOf("") }
            var isApiKeyValid by remember { mutableStateOf(false) }
            var isUserProvidedApiKey by remember { mutableStateOf(false) }
            var isApiCallInProgress by remember { mutableStateOf(false) }
            var snackbarData: SnackbarData? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                // Prepopulates the API key, IFF it was provided by user.
                if (apiKeyProvider.hasUserProvidedApiKey(screen.weatherApiService)) {
                    apiKey = apiKeyProvider.activeServiceApiKey
                    isUserProvidedApiKey = true
                    isApiKeyValid = apiKeyProvider.isValidKey(screen.weatherApiService, apiKey)
                }
            }

            LaunchedImpressionEffect {
                analytics.logScreenView(BringYourOwnApiKeyScreen::class)
            }

            return BringYourOwnApiKeyScreen.State(
                weatherService = screen.weatherApiService,
                originatedFromApiError = screen.isOriginatedFromError,
                apiKeyInput = apiKey,
                isApiKeyValid = isApiKeyValid,
                isUserProvidedApiKey = isUserProvidedApiKey,
                isApiCallInProgress = isApiCallInProgress,
                snackbarData = snackbarData,
            ) { event ->
                when (event) {
                    is BringYourOwnApiKeyScreen.Event.ApiKeyChanged -> {
                        apiKey = event.value
                        val isValidKey =
                            apiKeyProvider.isValidKey(screen.weatherApiService, event.value)
                        isApiKeyValid = isValidKey
                    }

                    is BringYourOwnApiKeyScreen.Event.SubmitApiKey -> {
                        isApiCallInProgress = true
                        scope.launch {
                            val result = weatherRepository.isValidApiKey(screen.weatherApiService, apiKey)
                            isApiCallInProgress = false
                            when (result) {
                                is ApiResult.Success -> {
                                    Timber.d("API key is valid - saving to preferences.")
                                    logAddServiceApiKey(isApiKeyAdded = true)
                                    preferencesManager.saveUserApiKey(screen.weatherApiService, apiKey)
                                    snackbarData =
                                        SnackbarData("✔️API key is valid and saved.", "Continue") {
                                            if (screen.isOriginatedFromError) {
                                                navigator.pop()
                                            } else {
                                                navigator.resetRoot(CurrentWeatherAlertScreen("api-set"))
                                            }
                                        }
                                }
                                is ApiResult.Failure -> {
                                    logAddServiceApiKey(isApiKeyAdded = false)
                                    // Reset the supporting text message to show the API format guide.
                                    isUserProvidedApiKey = false

                                    var serverMessage = ""
                                    if (result is ApiResult.Failure.HttpFailure) {
                                        result.error?.let {
                                            serverMessage = "\n⚠️ API message: $it"
                                        }
                                    }
                                    snackbarData =
                                        SnackbarData(
                                            message = "Invalid API key. Please double check and try again.$serverMessage",
                                            actionLabel = "Okay",
                                        ) {
                                            snackbarData = null
                                        }
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

        private suspend fun logAddServiceApiKey(isApiKeyAdded: Boolean) {
            analytics.logAddServiceApiKey(
                weatherService = screen.weatherApiService,
                isApiKeyAdded = isApiKeyAdded,
                initiatedFromApiError = screen.isOriginatedFromError,
            )
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
    val serviceConfig: WeatherServiceLogoConfig = state.weatherService.serviceConfig()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your API Key") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(BringYourOwnApiKeyScreen.Event.GoBack)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPaddingValues ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (state.originatedFromApiError) {
                Text(
                    text = serviceConfig.apiExhaustedMessage,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            // Image with clouds and servers for visual appeal.
            WeatherServiceImageAsset(serviceConfig)

            WeatherApiServiceLinkedText(serviceConfig)

            OutlinedTextField(
                value = state.apiKeyInput,
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
                            painter = painterResource(id = R.drawable.key_24dp),
                            contentDescription = "Invalid API key",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                placeholder = { Text("Enter your API key here") },
                supportingText = {
                    Text(
                        text =
                            if (state.isUserProvidedApiKey) {
                                "⚡️ Your provided API key is being used for alert service."
                            } else {
                                serviceConfig.apiFormatGuide
                            },
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                enabled = state.isApiKeyValid,
                onClick = {
                    keyboardController?.hide()
                    state.eventSink(BringYourOwnApiKeyScreen.Event.SubmitApiKey(state.apiKeyInput))
                },
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

/**
 * Visual representation of weather service with clouds and servers.
 */
@Composable
private fun WeatherServiceImageAsset(serviceConfig: WeatherServiceLogoConfig) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.servers),
            contentDescription = "Server Icon Image",
            modifier = Modifier.align(Alignment.Center),
        )
        Image(
            painter = painterResource(id = R.drawable.clouds),
            contentDescription = "Clouds Icon",
            modifier =
                Modifier
                    .size(96.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp),
        )
        Image(
            painter = painterResource(id = serviceConfig.logoResId),
            contentDescription = "Weather service logo",
            modifier =
                Modifier
                    .size(serviceConfig.logoWidth, serviceConfig.logoHeight)
                    .align(Alignment.TopEnd)
                    .padding(end = 24.dp),
        )
    }
}

@Composable
private fun WeatherApiServiceLinkedText(serviceConfig: WeatherServiceLogoConfig) {
    var clickedUrl by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val annotatedLinkString =
        buildAnnotatedString {
            append("Visit ")
            withLink(
                LinkAnnotation.Url(
                    url = serviceConfig.apiServiceUrl,
                    styles =
                        TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                            hoveredStyle = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                        ),
                    linkInteractionListener = {
                        if (!clickedUrl) uriHandler.openUri(serviceConfig.apiServiceUrl)
                        clickedUrl = true
                    },
                ),
            ) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(serviceConfig.apiServiceUrlLabel)
                }
            }
            append(" to get your free API key for '")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                append(serviceConfig.apiServiceProductName)
            }
            append("'.")
        }
    Text(text = annotatedLinkString)
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BringYourOwnApiKeyScreenPreview() {
    WeatherAlertAppTheme {
        BringYourOwnApiKeyScreen(
            state =
                BringYourOwnApiKeyScreen.State(
                    weatherService = WeatherService.OPEN_WEATHER_MAP,
                    originatedFromApiError = true,
                    apiKeyInput = "123456abcdef123456abcdef123456ab",
                    isApiKeyValid = false,
                    isUserProvidedApiKey = true,
                    isApiCallInProgress = false,
                    eventSink = {},
                ),
        )
    }
}
