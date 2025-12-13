package dev.hossain.weatheralert.ui.devtools

import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.Alert
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AlertHistory
import dev.hossain.weatheralert.db.AlertHistoryDao
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.UserCityAlert
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.random.Random

/**
 * Implementation of [DevToolsRepository].
 *
 * This repository provides development tools functionality including:
 * - Creating and managing test alerts
 * - Generating test history data
 * - Performing city lookups
 *
 * All test data is marked with [TEST] prefix for easy identification and cleanup.
 */
@ContributesBinding(AppScope::class)
@Inject
class DevToolsRepositoryImpl
    constructor(
        private val alertDao: AlertDao,
        private val alertHistoryDao: AlertHistoryDao,
        private val cityDao: CityDao,
    ) : DevToolsRepository {
        companion object {
            private const val TEST_PREFIX = "[TEST]"
            private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
        }

        override suspend fun createTestAlert(
            cityId: Long,
            category: WeatherAlertCategory,
            threshold: Float,
            notes: String,
        ): Long {
            require(threshold > 0) { "Threshold must be positive" }

            val testNotes =
                if (notes.startsWith(TEST_PREFIX)) {
                    notes
                } else {
                    "$TEST_PREFIX $notes"
                }

            val alert =
                Alert(
                    cityId = cityId,
                    alertCategory = category,
                    threshold = threshold,
                    notes = testNotes,
                )

            val alertId = alertDao.insertAlert(alert)
            Timber.d("Created test alert: id=$alertId, cityId=$cityId, category=$category, threshold=$threshold")
            return alertId
        }

        override suspend fun getTestAlerts(): List<UserCityAlert> {
            val allAlerts = alertDao.getAllAlertsWithCities()
            val testAlerts = allAlerts.filter { it.alert.notes.contains(TEST_PREFIX) }
            Timber.d("Found ${testAlerts.size} test alerts out of ${allAlerts.size} total alerts")
            return testAlerts
        }

        override suspend fun deleteTestAlert(alertId: Long) {
            alertDao.deleteAlertById(alertId)
            Timber.d("Deleted test alert: id=$alertId")
        }

        override suspend fun deleteAllTestAlerts(): Int {
            val testAlerts = getTestAlerts()
            testAlerts.forEach { userCityAlert ->
                alertDao.deleteAlertById(userCityAlert.alert.id)
            }
            Timber.d("Deleted ${testAlerts.size} test alerts")
            return testAlerts.size
        }

        override suspend fun generateAlertHistory(
            count: Int,
            startTime: Long,
            endTime: Long,
            cities: List<String>,
            categories: List<WeatherAlertCategory>,
        ): Int {
            require(count > 0) { "Count must be positive" }
            require(startTime < endTime) { "Start time must be before end time" }
            require(cities.isNotEmpty()) { "Cities list cannot be empty" }
            require(categories.isNotEmpty()) { "Categories list cannot be empty" }

            // Get or create a dummy alert ID for history entries
            // This is needed because AlertHistory has a foreign key constraint on Alert
            val dummyAlertId = getOrCreateDummyAlert()

            var generatedCount = 0
            repeat(count) {
                val randomTime = Random.nextLong(startTime, endTime)
                val randomCity = cities.random()
                val randomCategory = categories.random()
                val randomThreshold = Random.nextFloat() * 50f + 5f // 5-55 range
                val randomWeatherValue = Random.nextDouble(randomThreshold.toDouble() * 0.5, randomThreshold.toDouble() * 2.0)

                val history =
                    AlertHistory(
                        alertId = dummyAlertId,
                        triggeredAt = randomTime,
                        weatherValue = randomWeatherValue,
                        thresholdValue = randomThreshold,
                        cityName = randomCity,
                        alertCategory = randomCategory,
                    )

                alertHistoryDao.insert(history)
                generatedCount++
            }

            Timber.d("Generated $generatedCount alert history entries")
            return generatedCount
        }

        /**
         * Gets or creates a dummy alert for history generation.
         * This is needed because AlertHistory has a foreign key constraint on Alert.
         */
        private suspend fun getOrCreateDummyAlert(): Long {
            val testAlerts = getTestAlerts()
            return if (testAlerts.isNotEmpty()) {
                // Use an existing test alert
                testAlerts.first().alert.id
            } else {
                // Create a dummy city if needed
                val dummyCity = cityDao.getAllCities().first().firstOrNull()
                val cityId =
                    dummyCity?.id ?: run {
                        // If no cities exist, create one
                        val newCity =
                            City(
                                id = Long.MAX_VALUE,
                                city = "[TEST] Dummy City",
                                cityName = "[TEST] Dummy City",
                                lat = 0.0,
                                lng = 0.0,
                                country = "TEST",
                                iso2 = "XX",
                                iso3 = "XXX",
                                provStateName = "Test",
                                capital = null,
                                population = 0,
                            )
                        cityDao.insertCity(newCity)
                        newCity.id
                    }

                // Create a dummy alert
                createTestAlert(
                    cityId = cityId,
                    category = WeatherAlertCategory.SNOW_FALL,
                    threshold = 10.0f,
                    notes = "Dummy alert for history generation",
                )
            }
        }

        override suspend fun getHistoryStats(): HistoryStats {
            val currentTime = System.currentTimeMillis()
            val sevenDaysAgo = currentTime - (7 * MILLIS_IN_DAY)
            val thirtyDaysAgo = currentTime - (30 * MILLIS_IN_DAY)

            val allHistory = alertHistoryDao.getAll()
            val totalCount = allHistory.size
            val last7DaysCount = allHistory.count { it.triggeredAt >= sevenDaysAgo }
            val last30DaysCount = allHistory.count { it.triggeredAt >= thirtyDaysAgo }
            val snowCount = allHistory.count { it.alertCategory == WeatherAlertCategory.SNOW_FALL }
            val rainCount = allHistory.count { it.alertCategory == WeatherAlertCategory.RAIN_FALL }

            val stats =
                HistoryStats(
                    totalCount = totalCount,
                    last7DaysCount = last7DaysCount,
                    last30DaysCount = last30DaysCount,
                    snowCount = snowCount,
                    rainCount = rainCount,
                )

            Timber.d("History stats: $stats")
            return stats
        }

        override suspend fun clearTestHistory(): Int {
            val allHistory = alertHistoryDao.getAll()
            val beforeCount = allHistory.size
            alertHistoryDao.deleteAll()
            Timber.d("Cleared $beforeCount test history entries")
            return beforeCount
        }

        override suspend fun searchCities(
            query: String,
            limit: Int,
        ): List<City> {
            require(query.isNotBlank()) { "Search query cannot be blank" }
            require(limit > 0) { "Limit must be positive" }

            val cities = cityDao.searchCitiesByName(query, limit).first()
            Timber.d("Found ${cities.size} cities matching '$query'")
            return cities
        }

        override suspend fun getPopularCities(limit: Int): List<City> {
            require(limit > 0) { "Limit must be positive" }

            // Get all cities and sort by population
            val cities =
                cityDao
                    .getAllCities()
                    .first()
                    .filter { it.population != null }
                    .sortedByDescending { it.population }
                    .take(limit)

            Timber.d("Retrieved ${cities.size} popular cities")
            return cities
        }
    }
