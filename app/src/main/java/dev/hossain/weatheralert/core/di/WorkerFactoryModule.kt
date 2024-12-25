package dev.hossain.weatheralert.core.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.network.WeatherRepository
import dev.hossain.weatheralert.core.work.WeatherCheckWorker
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.di.ApplicationContext
import javax.inject.Singleton

@Module
@ContributesTo(AppScope::class)
object WorkerFactoryModule {
    @Provides
    @Reusable
    fun provideWorkerFactory(
        weatherRepository: WeatherRepository,
        dataStore: AlertConfigDataStore
    ): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                @ApplicationContext appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return when (workerClassName) {
                    WeatherCheckWorker::class.java.name -> WeatherCheckWorker(appContext, workerParameters, weatherRepository, dataStore)
                    else -> null
                }
            }
        }
    }
}