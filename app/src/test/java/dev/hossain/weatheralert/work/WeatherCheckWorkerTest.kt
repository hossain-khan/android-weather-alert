package dev.hossain.weatheralert.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.firebase.FirebaseApp
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.di.NetworkBindings
import dev.hossain.weatheralert.test.TestUtils
import dev.hossain.weatheralert.util.Analytics
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import javax.inject.Inject

@Ignore("Will fix it later after metro migration")
@RunWith(RobolectricTestRunner::class)
class WeatherCheckWorkerTest {
    // Guide @ https://github.com/square/okhttp/tree/master/mockwebserver
    private lateinit var mockWebServer: MockWebServer

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var testWorkerFactory: TestWorkerFactory

    @Inject
    internal lateinit var weatherRepository: WeatherRepository

    @Inject
    internal lateinit var appDatabase: AppDatabase

    @Inject
    internal lateinit var alertDao: AlertDao

    @Inject
    internal lateinit var analytics: Analytics

    @Inject
    internal lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        io.tomorrow.api.di.TomorrowIoModule.tomorrowIoBaseUrl = mockWebServer.url("/")

        injectAndSetupTestClass()

        testWorkerFactory =
            TestWorkerFactory(
                alertDao = alertDao,
                weatherRepository = weatherRepository,
                analytics = analytics,
                preferencesManager = preferencesManager,
            )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        mockWebServer.shutdown()
        appDatabase.close()
    }

    @Test
    fun `given no alerts set - results in successful work execution`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(TestUtils.loadJsonFromResources("open-weather-cancun.json")),
        )

        val worker =
            TestListenableWorkerBuilder<WeatherCheckWorker>(context)
                .setWorkerFactory(testWorkerFactory)
                .build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertThat(result, notNullValue())
        }
    }

    @Test
    fun `given single alert set and success API response - results in successful work execution`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(TestUtils.loadJsonFromResources("open-weather-cancun.json")),
        )

        val worker =
            TestListenableWorkerBuilder<WeatherCheckWorker>(context)
                .setWorkerFactory(testWorkerFactory)
                .build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertThat(result, notNullValue())
        }
    }

    // Helper method to inject dependencies
    private fun injectAndSetupTestClass() {
        FirebaseApp.initializeApp(context)
    }
}
