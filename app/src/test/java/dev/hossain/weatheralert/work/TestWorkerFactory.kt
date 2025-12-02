package dev.hossain.weatheralert.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.util.Analytics
import dev.hossain.weatheralert.work.WeatherCheckWorker

class TestWorkerFactory(
    private val alertDao: AlertDao,
    private val alertHistoryDao: AlertHistoryDao,
    private val weatherRepository: WeatherRepository,
    private val analytics: Analytics,
    private val preferencesManager: PreferencesManager,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): CoroutineWorker? =
        when (workerClassName) {
            WeatherCheckWorker::class.java.name -> {
                WeatherCheckWorker(
                    context = appContext,
                    params = workerParameters,
                    alertDao = alertDao,
                    alertHistoryDao = alertHistoryDao,
                    weatherRepository = weatherRepository,
                    analytics = analytics,
                    preferencesManager = preferencesManager,
                )
            }

            else -> {
                null
            }
        }
}
