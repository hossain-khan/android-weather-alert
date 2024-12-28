package dev.hossain.weatheralert

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dev.hossain.weatheralert.di.AppComponent
import dev.hossain.weatheralert.work.WeatherCheckWorker
import timber.log.Timber


/**
 * Application class for the app with key initializations.
 */
class WeatherAlertApp : Application() {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        enqueueWork()
    }

    private fun enqueueWork() {
        Timber.d("Enqueueing weather check worker")
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(OneTimeWorkRequest.Builder(WeatherCheckWorker::class.java).build())
    }
}
