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
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.work.WeatherCheckWorker

// Anvil module to contribute the WorkerFactory
@Module
@ContributesTo(AppScope::class)
object WorkerModule {
    @Provides
    fun provideWorkerFactory(
        alertDao: AlertDao,
        weatherRepository: WeatherRepository,
        historicalWeatherRepository: dev.hossain.weatheralert.data.HistoricalWeatherRepository,
        analytics: Analytics,
        preferencesManager: PreferencesManager,
    ): WorkerFactory =
        object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): CoroutineWorker? {
                val workerClass =
                    Class
                        .forName(workerClassName)
                        .asSubclass(CoroutineWorker::class.java)
                return when (workerClass) {
                    WeatherCheckWorker::class.java ->
                        WeatherCheckWorker(
                            context = appContext,
                            params = workerParameters,
                            alertDao = alertDao,
                            weatherRepository = weatherRepository,
                            historicalWeatherRepository = historicalWeatherRepository,
                            analytics = analytics,
                            preferencesManager = preferencesManager,
                        )
                    else -> null
                }
            }
        }
}
