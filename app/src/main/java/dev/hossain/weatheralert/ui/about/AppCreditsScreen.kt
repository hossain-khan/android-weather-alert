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
data class AppCreditsScreen(
    val requestId: String,
) : Screen {
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object GoBack : Event()
    }
}

class AppCreditsPresenter
    @AssistedInject
    constructor(
        @Assisted private val navigator: Navigator,
        @Assisted private val screen: AppCreditsScreen,
        private val analytics: Analytics,
    ) : Presenter<AppCreditsScreen.State> {
        @Composable
        override fun present(): AppCreditsScreen.State {
            LaunchedImpressionEffect {
                analytics.logScreenView(AppCreditsScreen::class)
            }

            return AppCreditsScreen.State { event ->
                when (event) {
                    AppCreditsScreen.Event.GoBack -> {
                        navigator.pop()
                    }
                }
            }
        }

        @CircuitInject(AppCreditsScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(
                navigator: Navigator,
                screen: AppCreditsScreen,
            ): AppCreditsPresenter
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(AppCreditsScreen::class, AppScope::class)
@Composable
fun AppCreditsScreen(
    state: AppCreditsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits") },
                navigationIcon = {
                    IconButton(onClick = {
                        state.eventSink(AppCreditsScreen.Event.GoBack)
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
            Image(
                painter = painterResource(R.drawable.world_map),
                contentDescription = "World Map",
                modifier = Modifier.padding(top = 0.dp).align(Alignment.CenterHorizontally).size(100.dp),
            )
            Text(
                text = "City Database",
                style = MaterialTheme.typography.titleMedium,
            )
            SimpleMapsLinkedText()
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(R.drawable.alien_monster_icon),
                contentDescription = "World Map",
                modifier = Modifier.padding(top = 0.dp).align(Alignment.CenterHorizontally).size(100.dp),
            )
            Text(
                text = "Icons",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "- Material Design Icons",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "- Vector icons from various sources",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SimpleMapsLinkedText() {
    val uriHandler = LocalUriHandler.current

    val annotatedLinkString =
        buildAnnotatedString {
            append("The world cities database is provided by ")
            withLink(
                LinkAnnotation.Url(
                    url = "https://simplemaps.com",
                    styles =
                        TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                            hoveredStyle = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                        ),
                    linkInteractionListener = {
                        uriHandler.openUri("https://simplemaps.com")
                    },
                ),
            ) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("simplemaps.com")
                }
            }
            append(".")
        }
    Text(text = annotatedLinkString)
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun AppCreditsScreenPreview() {
    val sampleState =
        AppCreditsScreen.State(
            eventSink = {},
        )
    WeatherAlertAppTheme {
        AppCreditsScreen(state = sampleState)
    }
}
