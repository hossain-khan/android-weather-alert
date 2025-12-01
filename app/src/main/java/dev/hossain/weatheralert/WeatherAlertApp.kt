package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import dev.hossain.weatheralert.di.AppGraph
import dev.hossain.weatheralert.notification.createAppNotificationChannel
import dev.hossain.weatheralert.util.CrashlyticsTree
import dev.hossain.weatheralert.work.scheduleWeatherAlertsWork
import dev.zacsweers.metro.createGraphFactory
import timber.log.Timber

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp :
    Application(),
    Configuration.Provider {
    /** Holder reference for the app graph for Component Factory. */
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    // https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration
    override val workManagerConfiguration: Configuration
        get() {
            Timber.i("Setting up custom WorkManager configuration")
            return Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setWorkerFactory(appGraph.workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()

        installLoggingTree()

        createAppNotificationChannel(context = this)
        scheduleWeatherAlertsWork(context = this, appGraph.preferencesManager.preferredUpdateIntervalSync)

        // Debug functions - uncomment to test manually:
        // dev.hossain.weatheralert.notification.debugNotification(context = this)
        // dev.hossain.weatheralert.notification.debugSnooze(context = this, alertId = 1)
        // scheduleOneTimeWeatherAlertWorkerDebug(context = this)
    }

    private fun installLoggingTree() {
        if (BuildConfig.DEBUG) {
            // Plant a debug tree for development builds
            Timber.plant(Timber.DebugTree())
        } else {
            // Plant the custom Crashlytics tree for production builds
            Timber.plant(CrashlyticsTree())
        }
    }
}
