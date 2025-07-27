package dev.hossain.weatheralert.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
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
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.BuildConfig
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.parcelize.Parcelize

@Parcelize
data object AboutAppScreen : Screen {
    data class State(
        val appVersion: String,
        val showLearnMoreSheet: Boolean,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()

        data object OpenGitHubProject : Event()

        data object OpenAppEducationDialog : Event()

        data object CloseAppEducationDialog : Event()
    }
}

@Inject
class AboutAppPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val analytics: Analytics,
    ) : Presenter<AboutAppScreen.State> {
        @Composable
        override fun present(): AboutAppScreen.State {
            val uriHandler = LocalUriHandler.current
            var showLearnMoreBottomSheet by remember { mutableStateOf(false) }

            val appVersion =
                buildString {
                    append("v")
                    append(BuildConfig.VERSION_NAME)
                    append(" (")
                    append(BuildConfig.GIT_COMMIT_HASH)
                    append(")")
                }

            LaunchedImpressionEffect {
                analytics.logScreenView(AboutAppScreen::class)
            }

            return AboutAppScreen.State(
                appVersion,
                showLearnMoreSheet = showLearnMoreBottomSheet,
            ) { event ->
                when (event) {
                    AboutAppScreen.Event.GoBack -> {
                        navigator.pop()
                    }

                    AboutAppScreen.Event.OpenGitHubProject -> {
                        uriHandler.openUri("https://github.com/hossain-khan/android-weather-alert")
                    }

                    AboutAppScreen.Event.OpenAppEducationDialog -> {
                        showLearnMoreBottomSheet = true
                        analytics.logViewTutorial(isComplete = false)
                    }

                    AboutAppScreen.Event.CloseAppEducationDialog -> {
                        showLearnMoreBottomSheet = false
                        analytics.logViewTutorial(isComplete = true)
                    }
                }
            }
        }

        @CircuitInject(AboutAppScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): AboutAppPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AboutAppScreen::class, AppScope::class)
@Composable
fun AboutAppScreen(
    state: AboutAppScreen.State,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About App") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AboutAppScreen.Event.GoBack)
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
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.weather_alert_icon),
                    contentDescription = "App icon",
                    modifier =
                        Modifier
                            .size(84.dp)
                            .align(Alignment.CenterHorizontally),
                )
                AppTagLineWithLinkedText(
                    eventSink = state.eventSink,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.github_logo_outline),
                    contentDescription = "GitHub Logo Icon",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier =
                        Modifier
                            .size(84.dp)
                            .align(Alignment.CenterHorizontally),
                )
                Text(
                    text = "Proudly open-source on GitHub",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                TextButton(onClick = {
                    state.eventSink(AboutAppScreen.Event.OpenGitHubProject)
                }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("View Source") }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                if (BuildConfig.DEBUG) {
                    // For fun, show Kodee in debug build
                    Image(
                        painter = painterResource(id = R.drawable.kodee_sharing_love),
                        contentDescription = "Kotlin Kodee Mascot",
                        modifier =
                            Modifier
                                .align(Alignment.CenterHorizontally),
                    )
                }
                Text(
                    text = "Version: ${state.appVersion}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp),
                )
            }
        }
    }

    if (state.showLearnMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                state.eventSink(AboutAppScreen.Event.CloseAppEducationDialog)
            },
            sheetState = sheetState,
        ) {
            LearnMoreAboutAlerts {
                state.eventSink(AboutAppScreen.Event.CloseAppEducationDialog)
            }
        }
    }
}

@Composable
private fun AppTagLineWithLinkedText(
    eventSink: (AboutAppScreen.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tag line: Your no-fuss, personal weather alerter.
    val annotatedLinkString =
        buildAnnotatedString {
            append("Your no-fuss, personal weather ")
            withLink(
                LinkAnnotation.Url(
                    // Dummy URL, not used for this use case.
                    url = "https://hossain.dev",
                    styles =
                        TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                            hoveredStyle = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                        ),
                    linkInteractionListener = {
                        eventSink(AboutAppScreen.Event.OpenAppEducationDialog)
                    },
                ),
            ) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("alerter")
                }
            }
            append(".")
        }
    Text(
        text = annotatedLinkString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun AboutAppScreenPreview() {
    val sampleState =
        AboutAppScreen.State(
            appVersion = "v1.0.0 (b135e2a)",
            showLearnMoreSheet = false,
            eventSink = {},
        )
    WeatherAlertAppTheme {
        AboutAppScreen(state = sampleState)
    }
}
