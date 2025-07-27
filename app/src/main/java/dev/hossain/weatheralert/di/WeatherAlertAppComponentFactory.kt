package dev.hossain.weatheralert.di

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.annotation.Keep
import androidx.core.app.AppComponentFactory
import dev.hossain.weatheralert.WeatherAlertApp
import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass

/**
 * Custom implementation of [AppComponentFactory] used to inject Android components
 * (specifically Activities) via Metro using constructor injection. This factory
 * allows the Android system to delegate activity instantiation to Metro's dependency
 * graph, enabling constructor injection instead of field injection.
 *
 * This class is referenced in the `AndroidManifest` within the `<application>` tag.
 */
@Keep
class WeatherAlertAppComponentFactory : AppComponentFactory() {
    /**
     * Retrieves an instance of the specified class (typically an Activity) from the provided
     * Metro providers map. If a provider exists for the class, it uses that provider to
     * obtain the instance; otherwise, it returns null.
     */
    private inline fun <reified T : Any> getInstance(
        classLoader: ClassLoader,
        className: String,
        providers: Map<KClass<out T>, @JvmSuppressWildcards Provider<T>>,
    ): T? {
        val clazz = Class.forName(className, false, classLoader).asSubclass(T::class.java)
        val modelProvider = providers[clazz.kotlin] ?: return null
        return modelProvider()
    }

    /**
     * Called by the Android system to instantiate activities. This method attempts to
     * retrieve an activity instance from Metro's dependency graph. If successful, it
     * returns the injected activity; otherwise, it falls back to the system's default
     * activity instantiation process.
     */
    override fun instantiateActivityCompat(
        classLoader: ClassLoader,
        className: String,
        intent: Intent?,
    ): Activity =
        getInstance(classLoader, className, activityProviders)
            ?: super.instantiateActivityCompat(classLoader, className, intent)

    /**
     * Called by the Android system to instantiate the Application class. This method
     * initializes the Metro dependency graph and retrieves the map of activity providers,
     * which are used later for activity injection.
     */
    override fun instantiateApplicationCompat(
        classLoader: ClassLoader,
        className: String,
    ): Application {
        val app = super.instantiateApplicationCompat(classLoader, className)
        // Retrieve the Metro app graph and the activity providers from it
        activityProviders = (app as WeatherAlertApp).appGraph.activityProviders
        return app
    }

    /**
     * Companion object to store activity providers. This object holds the Metro-provided
     * map of activity classes to their corresponding providers. It's used to inject activities
     * upon instantiation.
     */
    companion object {
        private lateinit var activityProviders: Map<KClass<out Activity>, Provider<Activity>>
    }
}
