package dev.hossain.weatheralert.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceAppWidgetReceiver
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

class WeatherAlertWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        WeatherAlertWidgetContent()
    }
}

/*
 * Use a WorkManager task or similar to periodically update the widget content.
 * For now, you can manually trigger updates during testing.
 */
fun updateWeatherWidget(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        WeatherAlertWidget().updateAll(context)
    }
}