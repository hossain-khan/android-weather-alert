package dev.hossain.weatheralert.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME
import com.google.firebase.analytics.logEvent
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import dev.hossain.weatheralert.util.Analytics.Companion.EVENT_ADD_SERVICE_API_KEY
import dev.hossain.weatheralert.util.Analytics.Companion.EVENT_SEND_APP_FEEDBACK
import dev.hossain.weatheralert.util.Analytics.Companion.EVENT_WORKER_JOB_COMPLETED
import dev.hossain.weatheralert.util.Analytics.Companion.EVENT_WORKER_JOB_FAILED
import dev.hossain.weatheralert.util.Analytics.Companion.EVENT_WORKER_JOB_STARTED
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

/**
 * Interface for logging analytics events.
 */
interface Analytics {
    companion object {
        internal const val EVENT_WORKER_JOB_STARTED = "wa_worker_job_initiated"
        internal const val EVENT_WORKER_JOB_COMPLETED = "wa_worker_job_success"
        internal const val EVENT_WORKER_JOB_FAILED = "wa_worker_job_failed"
        internal const val EVENT_ADD_SERVICE_API_KEY = "wa_add_service_api_key"
        internal const val EVENT_SEND_APP_FEEDBACK = "wa_send_app_feedback"
    }

    /**
     * Logs a screen view event.
     *
     * @param circuitScreen The screen class to log.
     */
    suspend fun logScreenView(circuitScreen: KClass<out Screen>)

    /**
     * Logs worker job initiated event.
     */
    suspend fun logWorkerJob(
        interval: Long,
        alertsCount: Long,
    )

    suspend fun logWorkSuccess()

    suspend fun logWorkFailed(
        weatherForecastService: WeatherForecastService,
        errorCode: Long = 0L,
    )

    /**
     * Logs event when user selects a city to see alert details.
     */
    suspend fun logCityDetails(
        cityId: Long,
        cityName: String,
    )

    /**
     * Logs attempt to add service API key.
     */
    suspend fun logAddServiceApiKey(
        weatherForecastService: WeatherForecastService,
        isApiKeyAdded: Boolean,
        initiatedFromApiError: Boolean,
    )

    /**
     * Logs event when user intends to sends feedback.
     */
    fun logSendFeedback()

    /**
     * Logs event when user views tutorial.
     */
    fun logViewTutorial(isComplete: Boolean)

    /**
     * Logs event when user views external URL for weather forecast service.
     */
    fun logViewServiceExternalUrl(weatherForecastService: WeatherForecastService)
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
@Inject
class AnalyticsImpl
    constructor(
        private val firebaseAnalytics: FirebaseAnalytics,
    ) : Analytics {
        override suspend fun logScreenView(circuitScreen: KClass<out Screen>) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(SCREEN_NAME, requireNotNull(circuitScreen.simpleName))
                param(SCREEN_CLASS, requireNotNull(circuitScreen.qualifiedName))
            }
        }

        override suspend fun logWorkerJob(
            interval: Long,
            alertsCount: Long,
        ) {
            firebaseAnalytics.logEvent(EVENT_WORKER_JOB_STARTED) {
                param("update_interval", interval)
                param("alerts_count", alertsCount)
            }
        }

        override suspend fun logWorkSuccess() {
            firebaseAnalytics.logEvent(EVENT_WORKER_JOB_COMPLETED) {
                // The result of an operation (long). Specify 1 to indicate success and 0 to indicate failure.
                param(FirebaseAnalytics.Param.SUCCESS, 1L)
            }
        }

        override suspend fun logWorkFailed(
            weatherForecastService: WeatherForecastService,
            errorCode: Long,
        ) {
            firebaseAnalytics.logEvent(EVENT_WORKER_JOB_FAILED) {
                // The result of an operation (long). Specify 1 to indicate success and 0 to indicate failure.
                param(FirebaseAnalytics.Param.SUCCESS, 0L)
                param(FirebaseAnalytics.Param.METHOD, weatherForecastService.name)
                param("error_code", errorCode)
            }
        }

        /**
         * - https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#SELECT_CONTENT()
         */
        override suspend fun logCityDetails(
            cityId: Long,
            cityName: String,
        ) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                param(FirebaseAnalytics.Param.ITEM_ID, cityId)
                param(FirebaseAnalytics.Param.ITEM_NAME, cityName)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "city")
            }
        }

        override suspend fun logAddServiceApiKey(
            weatherForecastService: WeatherForecastService,
            isApiKeyAdded: Boolean,
            initiatedFromApiError: Boolean,
        ) {
            firebaseAnalytics.logEvent(EVENT_ADD_SERVICE_API_KEY) {
                // The result of an operation (long). Specify 1 to indicate success and 0 to indicate failure.
                param(FirebaseAnalytics.Param.SUCCESS, if (isApiKeyAdded) 1L else 0L)
                param(FirebaseAnalytics.Param.METHOD, weatherForecastService.name)
                param("directed_from_error", initiatedFromApiError.toString())
            }
        }

        override fun logSendFeedback() {
            firebaseAnalytics.logEvent(EVENT_SEND_APP_FEEDBACK) {}
        }

        /**
         * - https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#TUTORIAL_BEGIN()
         * - https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#TUTORIAL_COMPLETE()
         */
        override fun logViewTutorial(isComplete: Boolean) {
            if (isComplete) {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE) {}
            } else {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN) {}
            }
        }

        /**
         * - https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#SELECT_CONTENT()
         */
        override fun logViewServiceExternalUrl(weatherForecastService: WeatherForecastService) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                param(FirebaseAnalytics.Param.ITEM_ID, weatherForecastService.name)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "service_website")
            }
        }
    }
