package dev.hossain.weatheralert.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherAlertWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WeatherAlertWidgetContent(context)
        }
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