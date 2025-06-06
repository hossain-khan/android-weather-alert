package dev.hossain.weatheralert.ui.alertslist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.ui.about.LearnMoreAboutAlerts
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyAlertState(
    onLearnMoreOpened: () -> Unit = {},
    onLearnMoreClosed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // https://developer.android.com/develop/ui/compose/components/bottom-sheets
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text =
                buildAnnotatedString {
                    append("ℹ️ This is ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("not")
                    }
                    append(" your usual weather forecast app.")
                },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(id = R.drawable.hiking_direction),
            contentDescription = "No alerts configured.",
        )
        Text(
            text = "Set weather alerts and get notified when thresholds are met.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(2.dp))
        TextButton(onClick = {
            onLearnMoreOpened()
            showBottomSheet = true
        }) { Text("Learn more") }

        Column(modifier = Modifier.padding(vertical = 80.dp)) {
            Text(
                text = "Powered by,",
                style =
                    MaterialTheme.typography.bodySmall
                        .copy(color = MaterialTheme.colorScheme.tertiary),
                fontStyle = FontStyle.Italic,
            )
            WeatherForecastService.entries.filter { it.isEnabled }.forEach { service ->
                val serviceConfig = service.serviceConfig()
                Image(
                    painter = painterResource(id = serviceConfig.logoResId),
                    contentDescription = "${serviceConfig.serviceName} Forecast Service Logo",
                    modifier =
                        Modifier
                            .padding(top = 16.dp, start = 24.dp)
                            // Add multiplier to make the logo bigger
                            .size(serviceConfig.logoWidth * 1.2f, serviceConfig.logoHeight * 1.2f)
                            // Reduces intensity by a bit
                            .alpha(0.9f),
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onLearnMoreClosed()
                showBottomSheet = false
            },
            sheetState = sheetState,
        ) {
            LearnMoreAboutAlerts {
                onLearnMoreClosed()
                showBottomSheet = false
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun EmptyStatePreview() {
    WeatherAlertAppTheme {
        EmptyAlertState(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        )
    }
}
