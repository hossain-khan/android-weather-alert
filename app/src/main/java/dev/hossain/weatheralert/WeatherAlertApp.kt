package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import dev.hossain.weatheralert.di.AppComponent
import dev.hossain.weatheralert.worker.WeatherCheckWorker
import dev.hossain.weatheralert.worker.WeatherCheckWorkerFactory
import javax.inject.Inject

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp : Application(), Configuration.Provider {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    @Inject
    lateinit var workerFactory: WeatherCheckWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Initialize WorkManager
        WeatherCheckWorker.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
