package dev.hossain.weatheralert

import android.app.Application
import dev.hossain.weatheralert.di.AppComponent
import dev.hossain.weatheralert.notification.createNotificationChannel
import dev.hossain.weatheralert.work.scheduleWeatherAlerts

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp : Application() {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel(this)
        scheduleWeatherAlerts(this)
    }
}
