package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dev.hossain.weatheralert.di.AppComponent
import javax.inject.Inject

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp : Application(), Configuration.Provider {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    @Inject
    lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }

    fun appComponent(): AppComponent = appComponent

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
