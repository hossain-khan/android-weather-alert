package dev.hossain.weatheralert.widget

import android.content.Context
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import dev.hossain.weatheralert.data.WeatherAlertKeys
import dev.hossain.weatheralert.data.weatherAlertDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun WeatherAlertWidgetContent(context: Context) {
    val scope = CoroutineScope(Dispatchers.IO)
    val snowAlert = remember { mutableStateOf("Loading...") }
    val rainAlert = remember { mutableStateOf("Loading...") }

    scope.launch {
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
            style = TextStyle(
                color = ColorProvider(android.R.color.black),
                fontSize = 16.dp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = snowAlert.value,
            style = TextStyle(
                color = ColorProvider(android.R.color.holo_blue_dark),
                fontSize = 14.dp
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = rainAlert.value,
            style = TextStyle(
                color = ColorProvider(android.R.color.holo_green_dark),
                fontSize = 14.dp
            )
        )
    }
}

/**
 * Subtle Animations on Widget
 *
 *     Purpose: Make the widget visually appealing.
 *     Implementation:
 *         Animate text color or size changes for alerts.
 *         Use a wave animation or pulsing effect to emphasize alerts.
 */
@Composable
fun AnimatedAlertText(alertText: String) {
    val color = rememberInfiniteTransition().animateColor(
        initialValue = Color.Blue,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = alertText,
        style = TextStyle(color = color.value, fontSize = 16.sp)
    )
}
