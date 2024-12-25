package dev.hossain.weatheralert.glancer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.layout.Column
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import javax.inject.Inject
import androidx.glance.currentState
import androidx.glance.GlanceId
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.padding
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.model.AlertCategory
import dev.hossain.weatheralert.core.model.AlertConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertWidget @Inject constructor(
    private val dataStore: AlertConfigDataStore
) : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val alerts = dataStore.getAlertConfigs().first()

        provideContent {
            AlertWidgetContent(alerts)
        }
    }

    @Composable
    fun AlertWidgetContent(alerts: List<AlertConfig>) {
        Column(modifier = GlanceModifier.padding(8.dp)) {
            if (alerts.isEmpty()) {
                Text("No alerts configured")
            } else {
                alerts.forEach { config ->
                    val forecast = getForecastForCategory(config.category) // Implement this based on how you fetch and store forecast
                    val status = if (forecast != null && forecast >= config.threshold) "ALERT" else "OK"
                    Text(
                        text = "${config.category}: ${config.threshold} - $status",
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }
    }

    // Placeholder function, you'll need to implement the actual logic
    private fun getForecastForCategory(category: AlertCategory): Double? {
        // This should ideally fetch the latest forecast from where you store it
        // (e.g., in-memory cache updated by the Worker, or another DataStore)
        return null
    }
}