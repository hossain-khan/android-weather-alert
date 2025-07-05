package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalWeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoricalWeatherEntity)

    @Query("SELECT * FROM historical_weather WHERE city_id = :cityId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getHistoryForCity(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<List<HistoricalWeatherEntity>>

    @Query("SELECT AVG(snow) FROM historical_weather WHERE city_id = :cityId AND date BETWEEN :start AND :end")
    fun getAverageSnow(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<Double?>

    @Query("SELECT AVG(rain) FROM historical_weather WHERE city_id = :cityId AND date BETWEEN :start AND :end")
    fun getAverageRain(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<Double?>
}
