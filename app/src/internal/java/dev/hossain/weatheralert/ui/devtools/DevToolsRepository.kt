package dev.hossain.weatheralert.ui.devtools

import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.db.City
import dev.hossain.weatheralert.db.UserCityAlert

/**
 * Repository for development tools data operations.
 *
 * This repository encapsulates all database operations needed by the developer portal,
 * including creating test alerts, generating history data, and performing city lookups.
 */
interface DevToolsRepository {
    // Alert operations
    
    /**
     * Creates a test alert with the [TEST] prefix in notes.
     *
     * @param cityId The ID of the city from the database
     * @param category The weather alert category (SNOW_FALL or RAIN_FALL)
     * @param threshold The threshold value for the alert
     * @param notes Additional notes (will be prefixed with [TEST])
     * @return The ID of the created alert
     */
    suspend fun createTestAlert(
        cityId: Long,
        category: WeatherAlertCategory,
        threshold: Float,
        notes: String,
    ): Long
    
    /**
     * Retrieves all test alerts (those with [TEST] prefix in notes).
     *
     * @return List of all test alerts with their associated city data
     */
    suspend fun getTestAlerts(): List<UserCityAlert>
    
    /**
     * Deletes a specific test alert by ID.
     *
     * @param alertId The ID of the alert to delete
     */
    suspend fun deleteTestAlert(alertId: Long)
    
    /**
     * Deletes all test alerts (those with [TEST] prefix in notes).
     *
     * @return Number of alerts deleted
     */
    suspend fun deleteAllTestAlerts(): Int
    
    // History operations
    
    /**
     * Generates random alert history entries for testing.
     *
     * @param count Number of history entries to generate
     * @param startTime Start time in milliseconds
     * @param endTime End time in milliseconds
     * @param cities List of city names to use
     * @param categories List of weather alert categories to use
     * @return Number of history entries created
     */
    suspend fun generateAlertHistory(
        count: Int,
        startTime: Long,
        endTime: Long,
        cities: List<String>,
        categories: List<WeatherAlertCategory>,
    ): Int
    
    /**
     * Gets statistics about alert history.
     *
     * @return Statistics including counts by time period and category
     */
    suspend fun getHistoryStats(): HistoryStats
    
    /**
     * Clears all test alert history entries.
     *
     * @return Number of history entries deleted
     */
    suspend fun clearTestHistory(): Int
    
    // City operations
    
    /**
     * Searches for cities by name.
     *
     * @param query The search query
     * @param limit Maximum number of results to return (default 20)
     * @return List of matching cities
     */
    suspend fun searchCities(
        query: String,
        limit: Int = 20,
    ): List<City>
    
    /**
     * Gets popular cities (by population).
     *
     * @param limit Maximum number of cities to return (default 10)
     * @return List of popular cities
     */
    suspend fun getPopularCities(limit: Int = 10): List<City>
}
