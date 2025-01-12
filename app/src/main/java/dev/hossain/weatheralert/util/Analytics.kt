package dev.hossain.weatheralert.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import com.slack.circuit.runtime.screen.Screen
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.optional.SingleIn
import dev.hossain.weatheralert.di.AppScope
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Interface for logging analytics events.
 */
interface Analytics {
    /**
     * Logs a screen view event.
     *
     * @param circuitScreen The screen class to log.
     */
    suspend fun logScreenView(circuitScreen: KClass<out Screen>)

    suspend fun logWorkerJob(alertsCount: Long)

    suspend fun logWorkSuccess()
}

/**
 * Implementation of [Analytics] interface.
 *
 * Uses [Firebase Analytics](https://firebase.google.com/docs/analytics/get-started?platform=android) to log different analytics.
 * See:
 * - [Events](https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event)
 * - [Params](https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Param)
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AnalyticsImpl
    @Inject
    constructor() : Analytics {
        private val firebaseAnalytics = Firebase.analytics

        override suspend fun logScreenView(circuitScreen: KClass<out Screen>) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(SCREEN_NAME, requireNotNull(circuitScreen.simpleName))
                param(SCREEN_CLASS, requireNotNull(circuitScreen.qualifiedName))
            }
        }

        override suspend fun logWorkerJob(alertsCount: Long) {
            firebaseAnalytics.logEvent("wa_worker_job_initiated") {
                param("alerts_count", alertsCount)
            }
        }

        override suspend fun logWorkSuccess() {
            firebaseAnalytics.logEvent("wa_worker_job_success") {}
        }
    }
