package dev.hossain.weatheralert.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import kotlinx.parcelize.Parcelize

@Parcelize
data class AboutAppScreen(
    val requestId: String,
) : Screen {
    data class State(
        val appVersion: String,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

class AboutAppPresenter
@AssistedInject
constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: AboutAppScreen,
    private val analytics: Analytics,
) : Presenter<AboutAppScreen.State> {
    @Composable
    override fun present(): AboutAppScreen.State {
        val appVersion = "1.0.0" // Replace with actual version retrieval logic

        LaunchedImpressionEffect {
            analytics.logScreenView(AboutAppScreen::class)
        }

        return AboutAppScreen.State(appVersion) { event ->
            when (event) {
                AboutAppScreen.Event.GoBack -> {
                    navigator.pop()
                }
            }
        }
    }

    @CircuitInject(AboutAppScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: Navigator,
            screen: AboutAppScreen,
        ): AboutAppPresenter
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AboutAppScreen::class, AppScope::class)
@Composable
fun AboutAppScreen(
    state: AboutAppScreen.State,
    modifier: Modifier = Modifier,
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Weather Alert App",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Version: ${state.appVersion}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "This app provides weather alerts for your selected locations.",
                style = MaterialTheme.typography.bodyMedium,
            )
            // Add more information as needed
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun AboutAppScreenPreview() {
    val sampleState =
        AboutAppScreen.State(
            appVersion = "1.0.0",
            eventSink = {},
        )
    WeatherAlertAppTheme {
        AboutAppScreen(state = sampleState)
    }
}