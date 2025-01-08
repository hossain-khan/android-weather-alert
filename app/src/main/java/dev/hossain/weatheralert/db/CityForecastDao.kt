package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    suspend fun deleteCityForecastById(forecastId: Int)

    @Query("SELECT * FROM city_forecasts WHERE forecast_id = :forecastId")
    suspend fun getCityForecastById(forecastId: Int): CityForecast?

    @Query("SELECT * FROM city_forecasts WHERE cityId = :cityId")
    suspend fun getCityForecastsByCityId(cityId: Int): List<CityForecast>
}
