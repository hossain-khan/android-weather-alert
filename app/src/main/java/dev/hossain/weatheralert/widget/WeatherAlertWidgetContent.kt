package dev.hossain.weatheralert.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceAppWidgetReceiver
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetScope
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WeatherAlertWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Weather Alerts",
            style = TextStyle(
                color = ColorProvider(android.R.color.black),
                fontSize = 16.dp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Snowfall: Tomorrow 7 cm",
            style = TextStyle(
                color = ColorProvider(android.R.color.holo_blue_dark),
                fontSize = 14.dp
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Rainfall: Tomorrow 15 mm",
            style = TextStyle(
                color = ColorProvider(android.R.color.holo_green_dark),
                fontSize = 14.dp
            )
        )
    }
}
