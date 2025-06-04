package dev.hossain.weatheralert.util

import android.os.Bundle // Required for Bundle
import com.google.common.truth.Truth.assertThat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS
import com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME
import com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS
import com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD
import com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT
import com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_BEGIN
import com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_COMPLETE
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor // Required for ArgumentCaptor
import org.mockito.Captor // Required for Captor
import org.mockito.Mock
import org.mockito.Mockito.verify // Required for verify
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class AnalyticsImplTest {

    @Mock
    private lateinit var mockFirebaseAnalytics: FirebaseAnalytics

    @Captor
    private lateinit var bundleCaptor: ArgumentCaptor<Bundle>

    private lateinit var analytics: AnalyticsImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        analytics = AnalyticsImpl(mockFirebaseAnalytics)
    }

    // Test methods will be added here
    @Test
    fun `logScreenView - logs screen_view event with screen name and class`() = runTest {
        val screenClass = TestScreen::class

        analytics.logScreenView(screenClass)

        verify(mockFirebaseAnalytics).logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getString(SCREEN_NAME)).isEqualTo("TestScreen")
        assertThat(bundle.getString(SCREEN_CLASS)).isEqualTo("dev.hossain.weatheralert.util.AnalyticsImplTest\$TestScreen")
    }

    // Dummy screen class for testing
    private object TestScreen : com.slack.circuit.runtime.screen.Screen {}

    @Test
    fun `logWorkerJob - logs worker_job_initiated event with interval and alerts count`() = runTest {
        val interval = 15L
        val alertsCount = 3L

        analytics.logWorkerJob(interval, alertsCount)

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_WORKER_JOB_STARTED,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong("update_interval")).isEqualTo(interval)
        assertThat(bundle.getLong("alerts_count")).isEqualTo(alertsCount)
    }

    @Test
    fun `logWorkSuccess - logs worker_job_success event with success param`() = runTest {
        analytics.logWorkSuccess()

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_WORKER_JOB_COMPLETED,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS)).isEqualTo(1L)
    }

    @Test
    fun `logWorkFailed - logs worker_job_failed event with success, method, and error code`() = runTest {
        val service = WeatherForecastService.OPEN_METEO
        val errorCode = 123L

        analytics.logWorkFailed(service, errorCode)

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_WORKER_JOB_FAILED,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS)).isEqualTo(0L)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD)).isEqualTo(service.name)
        assertThat(bundle.getLong("error_code")).isEqualTo(errorCode)
    }

    @Test
    fun `logCityDetails - logs select_content event with item_id, item_name, and content_type`() = runTest {
        val cityId = 987L
        val cityName = "Test City"

        analytics.logCityDetails(cityId, cityName)

        verify(mockFirebaseAnalytics).logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID)).isEqualTo(cityId)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME)).isEqualTo(cityName)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE)).isEqualTo("city")
    }

    @Test
    fun `logAddServiceApiKey - when API key added successfully - logs event with success, method, and directed_from_error as false`() = runTest {
        val service = WeatherForecastService.OPEN_WEATHER
        val isApiKeyAdded = true
        val initiatedFromApiError = false

        analytics.logAddServiceApiKey(service, isApiKeyAdded, initiatedFromApiError)

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_ADD_SERVICE_API_KEY,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS)).isEqualTo(1L)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD)).isEqualTo(service.name)
        assertThat(bundle.getString("directed_from_error")).isEqualTo(initiatedFromApiError.toString())
    }

    @Test
    fun `logAddServiceApiKey - when API key not added - logs event with failure, method, and directed_from_error as true`() = runTest {
        val service = WeatherForecastService.WEATHER_API
        val isApiKeyAdded = false
        val initiatedFromApiError = true

        analytics.logAddServiceApiKey(service, isApiKeyAdded, initiatedFromApiError)

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_ADD_SERVICE_API_KEY,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS)).isEqualTo(0L)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD)).isEqualTo(service.name)
        assertThat(bundle.getString("directed_from_error")).isEqualTo(initiatedFromApiError.toString())
    }

    @Test
    fun `logSendFeedback - logs send_app_feedback event`() { // Not a suspend function, so no runTest
        analytics.logSendFeedback()

        verify(mockFirebaseAnalytics).logEvent(
            Analytics.EVENT_SEND_APP_FEEDBACK,
            bundleCaptor.capture() // Capturing to verify no unexpected params, or an empty bundle
        )

        // Verify that the bundle is empty or contains no unexpected parameters.
        // A simple check is that the bundle itself is not null,
        // and specific sensitive parameters are not present if that's a concern.
        // For this event, Firebase typically sends some default params automatically.
        // We are mainly concerned that our specific event name is logged.
        val bundle = bundleCaptor.value
        assertThat(bundle).isNotNull()
    }

    @Test
    fun `logViewTutorial - when isComplete is false - logs tutorial_begin event`() { // Not a suspend function
        analytics.logViewTutorial(isComplete = false)

        verify(mockFirebaseAnalytics).logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_BEGIN,
            bundleCaptor.capture()
        )
        val bundle = bundleCaptor.value
        assertThat(bundle).isNotNull() // Similar to logSendFeedback, check bundle if needed
    }

    @Test
    fun `logViewTutorial - when isComplete is true - logs tutorial_complete event`() { // Not a suspend function
        analytics.logViewTutorial(isComplete = true)

        verify(mockFirebaseAnalytics).logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_COMPLETE,
            bundleCaptor.capture()
        )
        val bundle = bundleCaptor.value
        assertThat(bundle).isNotNull() // Similar to logSendFeedback, check bundle if needed
    }

    @Test
    fun `logViewServiceExternalUrl - logs select_content event with item_id and content_type`() { // Not a suspend function
        val service = WeatherForecastService.TOMORROW_IO

        analytics.logViewServiceExternalUrl(service)

        verify(mockFirebaseAnalytics).logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT,
            bundleCaptor.capture()
        )

        val bundle = bundleCaptor.value
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID)).isEqualTo(service.name)
        assertThat(bundle.getString(com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE)).isEqualTo("service_website")
    }
}
