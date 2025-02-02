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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.datamodel.WeatherService
import dev.hossain.weatheralert.ui.about.LearnMoreAboutAlerts
import dev.hossain.weatheralert.ui.serviceConfig
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyAlertState(modifier: Modifier = Modifier) {
    // https://developer.android.com/develop/ui/compose/components/bottom-sheets
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.dimensions.horizontalScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Your no-fuss, personal weather alerter.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(id = R.drawable.hiking_direction),
            contentDescription = "No alerts configured.",
        )
        Text(
            text = "No custom weather alerts configured.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(2.dp))
        TextButton(onClick = {
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
            WeatherService.entries.filter { it.isEnabled }.forEach { service ->
                val serviceConfig = service.serviceConfig()
                Image(
                    painter = painterResource(id = serviceConfig.logoResId),
                    contentDescription = "${serviceConfig.serviceName} Logo",
                    modifier =
                        Modifier
                            .padding(top = 16.dp, start = 56.dp)
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
                showBottomSheet = false
            },
            sheetState = sheetState,
        ) {
            LearnMoreAboutAlerts {
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
