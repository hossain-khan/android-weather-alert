package dev.hossain.weatheralert.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: City)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<City>)

    @Update
    suspend fun updateCity(city: City)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCityById(cityId: Int)

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: Int): City?

    @Query("SELECT * FROM cities ORDER BY city ASC")
    fun getAllCities(): Flow<List<City>>

    @Query("SELECT * FROM cities WHERE city LIKE '%' || :searchQuery || '%' OR country LIKE '%' || :searchQuery || '%' ORDER BY city ASC")
    fun searchCities(searchQuery: String): Flow<List<City>>
}
