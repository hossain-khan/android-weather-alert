package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dev.hossain.weatheralert.di.AppComponent
import dev.hossain.weatheralert.notification.createNotificationChannel
import dev.hossain.weatheralert.work.scheduleWeatherAlerts
import jakarta.inject.Inject
import timber.log.Timber

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp :
    Application(),
    Configuration.Provider {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    @Inject
    lateinit var workerFactory: WorkerFactory

    // https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration
    override val workManagerConfiguration: Configuration
        get() {
            Timber.i("Setting up custom WorkManager configuration")
            return Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannel(context = this)
        scheduleWeatherAlerts(context = this)

        // debugNotification(context = this)
    }
}
