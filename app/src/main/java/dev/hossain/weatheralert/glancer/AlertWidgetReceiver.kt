package dev.hossain.weatheralert.glancer


import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import javax.inject.Inject

class AlertWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var alertWidget: AlertWidget

    override val glanceAppWidget: GlanceAppWidget
        get() = alertWidget
}