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

    @Query("SELECT * FROM alerts WHERE cityId = :cityId")
    suspend fun getAlertsByCityId(cityId: Int): List<Alert>

    @Query("SELECT * FROM alerts WHERE alert_category = :category")
    suspend fun getAlertsByCategory(category: WeatherAlertCategory): List<Alert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert): Long

    @Update
    suspend fun updateAlert(alert: Alert)

    @Delete
    suspend fun deleteAlert(alert: Alert)

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Int)

//    @Transaction
//    @Query("SELECT * FROM alerts")
//    suspend fun getUserCityAlerts(): List<UserCityAlert>

    /**
     * FIXME: The query returns some columns [cityId, alert_category, threshold, notes] which are not used by
     * [UserCityAlert]. You can use @ColumnInfo annotation on the fields to specify the mapping.
     * You can annotate the method with @RewriteQueriesToDropUnusedColumns to direct Room to rewrite your query
     * to avoid fetching unused columns.  You can suppress this warning by annotating the method with
     * @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH).
     *
     * Columns returned by the query:
     * id, cityId, alert_category, threshold, notes, city, city_ascii, lat, lng, country, iso2, iso3, admin_name, capital, population, id.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM alerts
        INNER JOIN cities ON alerts.cityId = cities.id
    """,
    )
    suspend fun getAlertsWithCity(): List<UserCityAlert>
}
