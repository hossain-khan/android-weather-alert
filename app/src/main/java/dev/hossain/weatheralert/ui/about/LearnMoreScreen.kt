package dev.hossain.weatheralert.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuitx.overlays.BottomSheetOverlay
import dev.hossain.weatheralert.R

/**
 * Shows a bottom sheet overlay with learn more about alerts.
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
fun LearnMoreAboutAlerts(onDismiss: () -> Unit) {
    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .wrapContentSize(Alignment.Center),
    ) {
        Image(
            painter = painterResource(id = R.drawable.alert_notification),
            contentDescription = "Alert notification",
            modifier =
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
        )
        Text(
            text = "You can set custom weather alerts for your city.\n\nSet it and forget it. Once set, you will receive a notification when the weather condition matches your criteria.",
            style = MaterialTheme.typography.bodyLarge,
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
    ContentWithOverlays {
        LearnMoreAboutAlerts {}
    }
}
