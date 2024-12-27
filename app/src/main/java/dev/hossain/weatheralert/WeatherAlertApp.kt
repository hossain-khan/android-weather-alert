package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dev.hossain.weatheralert.di.AppComponent
import timber.log.Timber
import javax.inject.Inject

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

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    // https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setWorkerFactory(workerFactory)
                .build()
}
