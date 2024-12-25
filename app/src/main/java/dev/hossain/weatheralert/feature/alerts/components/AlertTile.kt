package dev.hossain.weatheralert.feature.alerts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.model.AlertConfig

@Composable
fun AlertTile(
    alertConfig: AlertConfig,
    forecast: Double?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (alertConfig.category) {
                        is AlertCategory.Snow -> "Snow Fall"
                        is AlertCategory.Rain -> "Rain Fall"
                    },
                    color = when {
                        forecast != null && forecast >= alertConfig.threshold -> Color.Red
                        else -> Color.LightGray
                    }
                )
                Text(text = "Threshold: ${alertConfig.threshold}${getUnit(alertConfig.category)}")
                Text(
                    text = "Forecast: ${
                        forecast?.toString() ?: "Loading..."
                    }${getUnit(alertConfig.category)}"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun getUnit(category: AlertCategory): String {
    return when (category) {
        is AlertCategory.Snow -> "cm"
        is AlertCategory.Rain -> "mm"
        else -> ""
    }
}