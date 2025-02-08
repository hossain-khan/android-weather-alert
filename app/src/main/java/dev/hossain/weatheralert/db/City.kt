package dev.hossain.weatheralert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a city entity of the bundled DB.
 *
 * - https://developer.android.com/training/data-storage/room/defining-data
 *
 * @see CityDao
 */
@Entity(
    tableName = "cities",
    indices = [
        Index(name = "idx_country", value = ["country"], unique = false),
        Index(name = "idx_city_ascii", value = ["city_ascii"], unique = false),
        Index(name = "idx_city", value = ["city"], unique = false),
    ],
)
data class City constructor(
    val city: String,
    @ColumnInfo(name = "city_ascii") val cityName: String,
    val lat: Double,
    val lng: Double,
    val country: String,
    val iso2: String,
    val iso3: String,
    @ColumnInfo(name = "admin_name") val provStateName: String?,
    val capital: String?,
    val population: Int?,
    @PrimaryKey val id: Long,
)
