package dev.hossain.weatheralert.ui.onboarding

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.effects.LaunchedImpressionEffect
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import dev.hossain.weatheralert.util.Analytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data object OnboardingScreen : Screen {
    data class State(
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed class Event : CircuitUiEvent {
        data object Skip : Event()

        data object Complete : Event()
    }
}

@Inject
class OnboardingPresenter
    constructor(
        @Assisted private val navigator: Navigator,
        private val preferencesManager: PreferencesManager,
        private val analytics: Analytics,
    ) : Presenter<OnboardingScreen.State> {
        @Composable
        override fun present(): OnboardingScreen.State {
            val scope = rememberCoroutineScope()

            LaunchedImpressionEffect {
                analytics.logScreenView(OnboardingScreen::class)
            }

            return OnboardingScreen.State { event ->
                when (event) {
                    OnboardingScreen.Event.Skip -> {
                        scope.launch {
                            preferencesManager.setOnboardingCompleted(true)
                        }
                        navigator.resetRoot(CurrentWeatherAlertScreen("root"))
                    }

                    OnboardingScreen.Event.Complete -> {
                        scope.launch {
                            preferencesManager.setOnboardingCompleted(true)
                        }
                        analytics.logViewTutorial(isComplete = true)
                        navigator.resetRoot(CurrentWeatherAlertScreen("root"))
                    }
                }
            }
        }

        @CircuitInject(OnboardingScreen::class, AppScope::class)
        @AssistedFactory
        fun interface Factory {
            fun create(navigator: Navigator): OnboardingPresenter
        }
    }

/**
 * Data class representing an onboarding page.
 */
private data class OnboardingPage(
    val iconResId: Int,
    val title: String,
    val description: String,
)

/**
 * List of onboarding pages to display.
 */
private val onboardingPages =
    listOf(
        OnboardingPage(
            iconResId = R.drawable.weather_alert_icon,
            title = "Custom Weather Alerts",
            description =
                "Set personalized alerts for weather conditions that matter to you. " +
                    "Unlike regular weather apps, we focus on what you need to know.",
        ),
        OnboardingPage(
            iconResId = R.drawable.snow_forecast_snowflake_icon,
            title = "Set Your Threshold",
            description =
                "Define the amount of snow or rain that matters to you. " +
                    "Get notified only when conditions meet your criteria.",
        ),
        OnboardingPage(
            iconResId = R.drawable.alert_notification,
            title = "Stay Prepared",
            description =
                "Receive timely notifications so you can prepare for weather " +
                    "conditions that affect your daily life.",
        ),
    )

@OptIn(ExperimentalFoundationApi::class)
@CircuitInject(OnboardingScreen::class, AppScope::class)
@Composable
fun OnboardingScreen(
    state: OnboardingScreen.State,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Scaffold(modifier = modifier) { contentPaddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPaddingValues)
                    .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Skip button at top right
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = { state.eventSink(OnboardingScreen.Event.Skip) },
                    enabled = !isLastPage,
                    modifier = Modifier.graphicsLayer { alpha = if (isLastPage) 0f else 1f },
                ) {
                    Text("Skip")
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) { page ->
                OnboardingPageContent(
                    page = onboardingPages[page],
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier =
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    },
                                ),
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (isLastPage) {
                    Button(
                        onClick = { state.eventSink(OnboardingScreen.Event.Complete) },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text("Get Started")
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = page.iconResId),
            contentDescription = null,
            colorFilter =
                if (page.iconResId != R.drawable.alert_notification) {
                    ColorFilter.tint(MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun OnboardingScreenPreview() {
    val sampleState = OnboardingScreen.State(eventSink = {})
    WeatherAlertAppTheme {
        OnboardingScreen(state = sampleState)
    }
}

@Preview(showBackground = true, name = "Page 1 - Custom Alerts - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Page 1 - Custom Alerts - Dark")
@Composable
private fun OnboardingPage1Preview() {
    WeatherAlertAppTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            OnboardingPageContent(
                page = onboardingPages[0],
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Page 2 - Set Threshold - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Page 2 - Set Threshold - Dark")
@Composable
private fun OnboardingPage2Preview() {
    WeatherAlertAppTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            OnboardingPageContent(
                page = onboardingPages[1],
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Page 3 - Stay Prepared - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Page 3 - Stay Prepared - Dark")
@Composable
private fun OnboardingPage3Preview() {
    WeatherAlertAppTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            OnboardingPageContent(
                page = onboardingPages[2],
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
