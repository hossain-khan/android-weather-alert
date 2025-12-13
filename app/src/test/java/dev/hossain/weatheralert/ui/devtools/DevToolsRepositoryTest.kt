package dev.hossain.weatheralert.ui.devtools

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AlertHistory
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.di.TestDatabaseModule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [DevToolsRepository].
 */
@RunWith(RobolectricTestRunner::class)
class DevToolsRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var alertDao: AlertDao
    private lateinit var alertHistoryDao: AlertHistoryDao
    private lateinit var cityDao: CityDao
    private lateinit var repository: DevToolsRepository

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        database = TestDatabaseModule.provideInMemoryDatabase(context)
        alertDao = TestDatabaseModule.provideAlertDao(database)
        alertHistoryDao = TestDatabaseModule.provideAlertHistoryDao(database)
        cityDao = TestDatabaseModule.provideCityDao(database)

        repository = DevToolsRepositoryImpl(alertDao, alertHistoryDao, cityDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // Alert operations tests

    @Test
    fun `createTestAlert adds TEST prefix to notes`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)

            // When
            val alertId =
                repository.createTestAlert(
                    cityId = 1,
                    category = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "Custom note",
                )

            // Then
            val alert = alertDao.getAlertWithCity(alertId)
            assertThat(alert.alert.notes).isEqualTo("[TEST] Custom note")
        }

    @Test
    fun `createTestAlert does not duplicate TEST prefix`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)

            // When
            val alertId =
                repository.createTestAlert(
                    cityId = 1,
                    category = WeatherAlertCategory.RAIN_FALL,
                    threshold = 5.0f,
                    notes = "[TEST] Already prefixed",
                )

            // Then
            val alert = alertDao.getAlertWithCity(alertId)
            assertThat(alert.alert.notes).isEqualTo("[TEST] Already prefixed")
        }

    @Test
    fun `getTestAlerts returns only test alerts`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)

            // Create test alert
            alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "[TEST] Test alert",
                ),
            )

            // Create regular alert
            alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.RAIN_FALL,
                    threshold = 5.0f,
                    notes = "Regular alert",
                ),
            )

            // When
            val testAlerts = repository.getTestAlerts()

            // Then
            assertThat(testAlerts).hasSize(1)
            assertThat(testAlerts[0].alert.notes).contains("[TEST]")
        }

    @Test
    fun `deleteTestAlert removes alert by id`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)

            val alertId =
                repository.createTestAlert(
                    cityId = 1,
                    category = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "Test",
                )

            // When
            repository.deleteTestAlert(alertId)

            // Then
            val alerts = alertDao.getAll()
            assertThat(alerts).isEmpty()
        }

    @Test
    fun `deleteAllTestAlerts removes only test alerts`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)

            // Create test alerts
            repository.createTestAlert(1, WeatherAlertCategory.SNOW_FALL, 10.0f, "Test 1")
            repository.createTestAlert(1, WeatherAlertCategory.RAIN_FALL, 5.0f, "Test 2")

            // Create regular alert
            alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 15.0f,
                    notes = "Regular alert",
                ),
            )

            // When
            val deletedCount = repository.deleteAllTestAlerts()

            // Then
            assertThat(deletedCount).isEqualTo(2)
            val remainingAlerts = alertDao.getAll()
            assertThat(remainingAlerts).hasSize(1)
            assertThat(remainingAlerts[0].notes).isEqualTo("Regular alert")
        }

    // History operations tests

    @Test
    fun `generateAlertHistory creates correct number of entries`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)
            
            // Create a test alert for the history entries to reference
            val alertId = alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "[TEST] Test alert",
                )
            )
            
            val startTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
            val endTime = System.currentTimeMillis()
            val cities = listOf("New York", "Los Angeles", "Chicago")
            val categories = listOf(WeatherAlertCategory.SNOW_FALL, WeatherAlertCategory.RAIN_FALL)

            // When
            val generatedCount =
                repository.generateAlertHistory(
                    count = 10,
                    startTime = startTime,
                    endTime = endTime,
                    cities = cities,
                    categories = categories,
                )

            // Then
            assertThat(generatedCount).isEqualTo(10)
            val history = alertHistoryDao.getAll()
            assertThat(history).hasSize(10)
        }

    @Test
    fun `generateAlertHistory creates entries within time range`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)
            
            // Create a test alert for the history entries to reference
            alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "[TEST] Test alert",
                )
            )
            
            val startTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
            val endTime = System.currentTimeMillis()

            // When
            repository.generateAlertHistory(
                count = 5,
                startTime = startTime,
                endTime = endTime,
                cities = listOf("Test City"),
                categories = listOf(WeatherAlertCategory.SNOW_FALL),
            )

            // Then
            val history = alertHistoryDao.getAll()
            history.forEach { entry ->
                assertThat(entry.triggeredAt).isAtLeast(startTime)
                assertThat(entry.triggeredAt).isAtMost(endTime)
            }
        }

    @Test
    fun `getHistoryStats returns correct counts`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)
            
            // Create a test alert for the history entries to reference
            val alertId = alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "[TEST] Test alert",
                )
            )
            
            val currentTime = System.currentTimeMillis()
            val sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000L)
            val thirtyDaysAgo = currentTime - (30 * 24 * 60 * 60 * 1000L)
            val fortyDaysAgo = currentTime - (40 * 24 * 60 * 60 * 1000L)

            // Create history entries at different times and categories
            alertHistoryDao.insert(
                createTestHistoryEntry(alertId = alertId, triggeredAt = currentTime - (3 * 24 * 60 * 60 * 1000L), category = WeatherAlertCategory.SNOW_FALL),
            )
            alertHistoryDao.insert(
                createTestHistoryEntry(alertId = alertId, triggeredAt = currentTime - (10 * 24 * 60 * 60 * 1000L), category = WeatherAlertCategory.RAIN_FALL),
            )
            alertHistoryDao.insert(
                createTestHistoryEntry(alertId = alertId, triggeredAt = currentTime - (35 * 24 * 60 * 60 * 1000L), category = WeatherAlertCategory.SNOW_FALL),
            )

            // When
            val stats = repository.getHistoryStats()

            // Then
            assertThat(stats.totalCount).isEqualTo(3)
            assertThat(stats.last7DaysCount).isEqualTo(1)
            assertThat(stats.last30DaysCount).isEqualTo(2)
            assertThat(stats.snowCount).isEqualTo(2)
            assertThat(stats.rainCount).isEqualTo(1)
        }

    @Test
    fun `clearTestHistory removes all history entries`() =
        runTest {
            // Given
            val city = createTestCity(id = 1, name = "Test City")
            cityDao.insertCity(city)
            
            // Create a test alert for the history entries to reference
            alertDao.insertAlert(
                Alert(
                    cityId = 1,
                    alertCategory = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "[TEST] Test alert",
                )
            )
            
            repository.generateAlertHistory(
                count = 5,
                startTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L),
                endTime = System.currentTimeMillis(),
                cities = listOf("Test City"),
                categories = listOf(WeatherAlertCategory.SNOW_FALL),
            )

            // When
            val clearedCount = repository.clearTestHistory()

            // Then
            assertThat(clearedCount).isEqualTo(5)
            val remainingHistory = alertHistoryDao.getAll()
            assertThat(remainingHistory).isEmpty()
        }

    // City operations tests

    @Test
    fun `searchCities returns matching cities`() =
        runTest {
            // Given
            cityDao.insertCity(createTestCity(1, "New York"))
            cityDao.insertCity(createTestCity(2, "New Orleans"))
            cityDao.insertCity(createTestCity(3, "Los Angeles"))

            // When
            val results = repository.searchCities("New", limit = 10)

            // Then
            assertThat(results).hasSize(2)
            assertThat(results.map { it.cityName }).containsExactly("New York", "New Orleans")
        }

    @Test
    fun `searchCities respects limit`() =
        runTest {
            // Given
            repeat(15) { index ->
                cityDao.insertCity(createTestCity(index.toLong(), "City $index", population = 1000000 - (index * 1000)))
            }

            // When
            val results = repository.searchCities("City", limit = 5)

            // Then
            assertThat(results).hasSize(5)
        }

    @Test
    fun `getPopularCities returns cities sorted by population`() =
        runTest {
            // Given
            cityDao.insertCity(createTestCity(1, "Small City", population = 100000))
            cityDao.insertCity(createTestCity(2, "Large City", population = 5000000))
            cityDao.insertCity(createTestCity(3, "Medium City", population = 1000000))

            // When
            val popularCities = repository.getPopularCities(limit = 3)

            // Then
            assertThat(popularCities).hasSize(3)
            assertThat(popularCities[0].cityName).isEqualTo("Large City")
            assertThat(popularCities[1].cityName).isEqualTo("Medium City")
            assertThat(popularCities[2].cityName).isEqualTo("Small City")
        }

    @Test
    fun `getPopularCities respects limit`() =
        runTest {
            // Given
            repeat(10) { index ->
                cityDao.insertCity(createTestCity(index.toLong(), "City $index", population = 1000000 - (index * 1000)))
            }

            // When
            val popularCities = repository.getPopularCities(limit = 3)

            // Then
            assertThat(popularCities).hasSize(3)
        }

    // Helper functions

    private fun createTestCity(
        id: Long,
        name: String,
        population: Int = 1000000,
    ) = City(
        id = id,
        city = name,
        cityName = name,
        lat = 40.7128,
        lng = -74.0060,
        country = "USA",
        iso2 = "US",
        iso3 = "USA",
        provStateName = "New York",
        capital = null,
        population = population,
    )

    private fun createTestHistoryEntry(
        alertId: Long,
        triggeredAt: Long,
        category: WeatherAlertCategory,
    ) = AlertHistory(
        alertId = alertId,
        triggeredAt = triggeredAt,
        weatherValue = 15.0,
        thresholdValue = 10.0f,
        cityName = "Test City",
        alertCategory = category,
    )
}
