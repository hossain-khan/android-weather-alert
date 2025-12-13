package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY triggered_at DESC")
    suspend fun getAll(): List<AlertHistory>

    @Query(
        """
        SELECT * FROM alert_history 
        WHERE triggered_at >= :startTime 
        ORDER BY triggered_at DESC
        """,
    )
    suspend fun getHistorySince(startTime: Long): List<AlertHistory>

    @Query(
        """
        SELECT * FROM alert_history 
        WHERE alert_category = :category 
        ORDER BY triggered_at DESC
        """,
    )
    suspend fun getHistoryByCategory(category: WeatherAlertCategory): List<AlertHistory>

    @Query(
        """
        SELECT * FROM alert_history 
        WHERE city_name = :cityName 
        ORDER BY triggered_at DESC
        """,
    )
    suspend fun getHistoryByCityName(cityName: String): List<AlertHistory>

    @Query(
        """
        SELECT * FROM alert_history 
        WHERE triggered_at >= :startTime AND triggered_at <= :endTime 
        ORDER BY triggered_at DESC
        """,
    )
    suspend fun getHistoryByDateRange(
        startTime: Long,
        endTime: Long,
    ): List<AlertHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alertHistory: AlertHistory): Long

    @Query("DELETE FROM alert_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM alert_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM alert_history WHERE triggered_at >= :startTime")
    suspend fun getCountSince(startTime: Long): Int

    /**
     * Batch insert alert history entries within a transaction.
     * More efficient than individual inserts for bulk operations.
     *
     * @param historyEntries List of alert history entries to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyEntries: List<AlertHistory>)

    /**
     * Get statistics about alert history using SQL aggregations.
     * More efficient than loading all records into memory.
     */
    @Query("SELECT COUNT(*) FROM alert_history")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM alert_history WHERE alert_category = :category")
    suspend fun getCountByCategory(category: WeatherAlertCategory): Int
}
