package dev.hossain.weatheralert.widget

import android.content.Context
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.hossain.weatheralert.data.WeatherAlertKeys
import dev.hossain.weatheralert.data.weatherAlertDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun WeatherAlertWidgetContent(context: Context) {
    val snowAlert = remember { mutableStateOf("Loading...") }
    val rainAlert = remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        val preferences = context.weatherAlertDataStore.data.first()
        snowAlert.value = preferences[WeatherAlertKeys.SNOW_ALERT] ?: "No Snow Alert"
        rainAlert.value = preferences[WeatherAlertKeys.RAIN_ALERT] ?: "No Rain Alert"
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Weather Alerts",
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = snowAlert.value,
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = rainAlert.value,
        )
    }
}
