package dev.hossain.weatheralert.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuitx.overlays.BottomSheetOverlay
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme

/**
 * Shows a bottom sheet overlay with learn more about alerts.
 *
 * See https://slackhq.github.io/circuit/circuitx/#overlays
 *
 * UPDATE: I wasn't able to use the [OverlayHost] to show the overlay from circuit.
 */
suspend fun OverlayHost.showLearnMoreAboutAlerts(): Unit =
    show(
        BottomSheetOverlay(
            model = Unit,
            skipPartiallyExpandedState = true,
        ) { _, overlayNavigator ->
            LearnMoreAboutAlerts(
                onDismiss = {
                    //    overlayNavigator.finish(Unit)
                },
            )
        },
    )

@Composable
fun LearnMoreAboutAlerts(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .padding(16.dp)
                .wrapContentSize(Alignment.Center),
    ) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Image(
                painter = painterResource(id = R.drawable.alert_notification),
                contentDescription = "Alert notification",
                modifier =
                    Modifier
                        .padding(16.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.snow_forecast_snowflake_icon),
                contentDescription = "Snowflake icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .size(42.dp)
                        .align(Alignment.TopStart),
            )
            Image(
                painter = painterResource(id = R.drawable.snow_outline_icon_v3),
                contentDescription = "Snowflake icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .size(20.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 40.dp, y = 12.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.snow_outline_icon_v3),
                contentDescription = "Snowflake icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .size(15.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 15.dp, y = 40.dp),
            )
        }
        Text(
            text =
                "This app does not provide daily weather forecast." +
                    " Instead, it allows you to set alerts based on weather conditions." +
                        "\n\nSet an alert threshold, and you'll be notified " +
                        "when the conditions match your criteria.",
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
        )
        Text(
            text =
                "Disclaimer: The accuracy of the weather data depends on the service, " +
                    "and the forecast data may not be consistent across services.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
            modifier =
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
private fun LearnMoreAboutAlertsPreview() {
    WeatherAlertAppTheme(darkTheme = isSystemInDarkTheme()) {
        LearnMoreAboutAlerts {}
    }
}
