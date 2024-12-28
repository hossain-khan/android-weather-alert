package dev.hossain.weatheralert

import android.app.Application
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import dev.hossain.weatheralert.di.AppComponent
import dev.hossain.weatheralert.work.WeatherCheckWorker
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

        enqueueWork()
    }

    /**
     * Enqueue weather check worker to run in background using WorkManager.
     * - https://developer.android.com/topic/libraries/architecture/workmanager
     */
    private fun enqueueWork() {
        Timber.d("Enqueueing weather check worker")
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(OneTimeWorkRequest.Builder(WeatherCheckWorker::class.java).build())
    }
}
