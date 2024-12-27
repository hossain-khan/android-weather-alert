package dev.hossain.weatheralert.di

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.work.AssistedWorkerFactory
import dev.hossain.weatheralert.work.WeatherCheckWorker
import javax.inject.Provider

// @Module
// object WorkerModule {
//    @Provides
//    @IntoMap
//    @ClassKey(WeatherCheckWorker::class)
//    fun provideMyWorker(factory: WeatherCheckWorker.Factory): CoroutineWorker.Factory = factory
// }

// class AppWorkerFactory @Inject constructor(
//    private val workerFactories: Map<Class<out Worker>, @JvmSuppressWildcards Provider<AssistedInject.Factory>>
// ) : WorkerFactory() {
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker? {
//        val foundEntry = workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
//        val factoryProvider = foundEntry?.value
//        return factoryProvider?.get()?.create(appContext, workerParameters)
//    }
// }

// class CustomWorkerFactory @Inject constructor(
//    private val workerProviders: Map<Class<out CoroutineWorker>, @JvmSuppressWildcards Provider<CoroutineWorker>>
// ) : WorkerFactory() {
//
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): CoroutineWorker? {
//        val workerClass = Class.forName(workerClassName).asSubclass(CoroutineWorker::class.java)
//        val provider = workerProviders[workerClass] ?: return null
//        return provider.get().apply { attachParameters(workerParameters) }
//    }
// }

// Anvil module to contribute the WorkerFactory
@Module
@ContributesTo(AppScope::class) // Replace AppScope with your application scope
object WorkerModule {
    @Provides
    fun provideWorkerFactory(
        workerFactories: Map<Class<out CoroutineWorker>, @JvmSuppressWildcards Provider<AssistedWorkerFactory>>,
    ): WorkerFactory =
        object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): CoroutineWorker? {
                val workerClass = Class.forName(workerClassName).asSubclass(CoroutineWorker::class.java)
                val factoryProvider = workerFactories[workerClass] ?: return null
                val factory = factoryProvider.get()
                return when (workerClass) {
                    WeatherCheckWorker::class.java -> (factory as WeatherCheckWorker.Factory).create(appContext, workerParameters)
                    else -> null
                }
            }
        }
}
