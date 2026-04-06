package dev.hossain.weatheralert.di

import android.app.Activity
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/**
 * A Metro multi-binding [MapKey] used for registering a [Activity] into the top level graphs.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@MapKey(implicitClassKey = true)
annotation class ActivityKey(
    val value: KClass<out Activity> = Nothing::class,
)
