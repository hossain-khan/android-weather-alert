package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface CityForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCityForecast(cityForecast: CityForecast): Long

    @Update
    suspend fun updateCityForecast(cityForecast: CityForecast)

    @Delete
    suspend fun deleteCityForecast(cityForecast: CityForecast)

    @Query("DELETE FROM city_forecasts WHERE forecast_id = :forecastId")
    suspend fun deleteCityForecastById(forecastId: Long)

    @Query("SELECT * FROM city_forecasts WHERE forecast_id = :forecastId")
    suspend fun getCityForecastById(forecastId: Long): CityForecast?

    /**
     * Get latest forecast for a city.
     *
     * In future, we may have to cleanup the table to keep only latest forecast for each city.
     * However, this is not required for now.
     */
    @Transaction
    @Query("SELECT * FROM city_forecasts WHERE cityId = :cityId ORDER BY created_at DESC")
    suspend fun getCityForecastsByCityId(cityId: Long): List<CityForecast>
}
