package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import dev.hossain.weatheralert.di.AppComponent
import javax.inject.Inject

/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp : Application(), Configuration.Provider {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    // TODO - find anvil way to inject this
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
