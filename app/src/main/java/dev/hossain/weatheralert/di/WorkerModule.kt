package dev.hossain.weatheralert.di

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.work.WeatherCheckWorker

// Anvil module to contribute the WorkerFactory
@Module
@ContributesTo(AppScope::class)
object WorkerModule {
    @Provides
    fun provideWorkerFactory(
        preferencesManager: PreferencesManager,
        weatherService: WeatherRepository,
    ): WorkerFactory =
        object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): CoroutineWorker? {
                val workerClass = Class.forName(workerClassName).asSubclass(CoroutineWorker::class.java)
                return when (workerClass) {
                    WeatherCheckWorker::class.java ->
                        WeatherCheckWorker(
                            context = appContext,
                            params = workerParameters,
                            preferencesManager = preferencesManager,
                            weatherService = weatherService,
                        )
                    else -> null
                }
            }
        }
}
