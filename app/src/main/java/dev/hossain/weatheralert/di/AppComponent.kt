package dev.hossain.weatheralert.di

import android.app.Activity
import android.content.Context
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.BindsInstance
import dev.hossain.weatheralert.WeatherAlertApp
import javax.inject.Provider

@MergeComponent(
    scope = AppScope::class,
    modules = [
        AppModule::class,
        NetworkModule::class,
        CircuitModule::class,
    ],
)
@SingleIn(AppScope::class)
interface AppComponent {
    val activityProviders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<Activity>>

    /**
     * Injects dependencies into [WeatherAlertApp].
     */
    fun inject(app: WeatherAlertApp)

    @MergeComponent.Factory
    interface Factory {
        fun create(
            @ApplicationContext @BindsInstance context: Context,
        ): AppComponent
    }

    companion object {
        fun create(context: Context): AppComponent = DaggerAppComponent.factory().create(context)
    }
}
