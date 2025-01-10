package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.hossain.weatheralert.data.WeatherAlertCategory

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts")
    suspend fun getAll(): List<Alert>

    @Query("SELECT * FROM alerts WHERE city_id = :cityId")
    suspend fun getAlertsByCityId(cityId: Int): List<Alert>

    @Query("SELECT * FROM alerts WHERE alert_category = :category")
    suspend fun getAlertsByCategory(category: WeatherAlertCategory): List<Alert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert): Long

    @Update
    suspend fun updateAlert(alert: Alert)

    @Query("UPDATE alerts SET notes = :notes WHERE id = :alertId")
    suspend fun updateAlertNote(
        alertId: Int,
        notes: String,
    )

    @Delete
    suspend fun deleteAlert(alert: Alert)

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Int)

    @Transaction
    @Query("SELECT * FROM alerts WHERE id = :alertId")
    suspend fun getAlertWithCity(alertId: Int): UserCityAlert

    @Transaction
    @Query("SELECT * FROM alerts")
    suspend fun getAllAlertsWithCities(): List<UserCityAlert>
}
