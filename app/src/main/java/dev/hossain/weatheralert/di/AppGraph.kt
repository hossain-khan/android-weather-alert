package dev.hossain.weatheralert.di

import android.app.Activity
import android.content.Context
import androidx.work.WorkerFactory
import com.slack.circuit.foundation.Circuit
import dev.hossain.weatheralert.WeatherAlertApp
import dev.hossain.weatheralert.data.PreferencesManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlin.reflect.KClass

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        AppBindings::class,
        NetworkBindings::class,
        DatabaseBindings::class,
        WorkerBindings::class,
        WeatherRepositoryBindings::class,
        UtilBindings::class,
    ],
)
interface AppGraph {
    @Provides
    fun provideApplicationContext(app: WeatherAlertApp): Context = app

    val preferencesManager: PreferencesManager
    val workerFactory: WorkerFactory

    /**
     * A multibinding map of activity classes to their providers accessible for
     * [WeatherAlertAppComponentFactory].
     */
    @Multibinds val activityProviders: Map<KClass<out Activity>, Provider<Activity>>

    val circuit: Circuit

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides app: WeatherAlertApp,
        ): AppGraph
    }
}
