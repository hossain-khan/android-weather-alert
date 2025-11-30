package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts")
    suspend fun getAll(): List<Alert>

    @Query("SELECT * FROM alerts WHERE city_id = :cityId")
    suspend fun getAlertsByCityId(cityId: Long): List<Alert>

    @Query("SELECT * FROM alerts WHERE alert_category = :category")
    suspend fun getAlertsByCategory(category: WeatherAlertCategory): List<Alert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlertSync(alert: Alert): Long

    @Update
    suspend fun updateAlert(alert: Alert)

    @Query("UPDATE alerts SET notes = :notes WHERE id = :alertId")
    suspend fun updateAlertNote(
        alertId: Long,
        notes: String,
    )

    /**
     * Updates the snooze timestamp for an alert.
     * @param alertId The ID of the alert to snooze.
     * @param snoozedUntil The timestamp (in milliseconds) until which the alert should be snoozed.
     */
    @Query("UPDATE alerts SET snoozed_until = :snoozedUntil WHERE id = :alertId")
    suspend fun updateSnoozeUntil(
        alertId: Long,
        snoozedUntil: Long?,
    )

    /**
     * Clears the snooze for an alert (un-snoozes it).
     * @param alertId The ID of the alert to un-snooze.
     */
    @Query("UPDATE alerts SET snoozed_until = NULL WHERE id = :alertId")
    suspend fun clearSnooze(alertId: Long)

    @Delete
    suspend fun deleteAlert(alert: Alert)

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Long)

    @Transaction
    @Query("SELECT * FROM alerts WHERE id = :alertId")
    suspend fun getAlertWithCity(alertId: Long): UserCityAlert

    @Transaction
    @Query("SELECT * FROM alerts")
    suspend fun getAllAlertsWithCities(): List<UserCityAlert>
}
