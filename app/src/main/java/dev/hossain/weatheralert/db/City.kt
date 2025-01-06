package dev.hossain.weatheralert.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class City(
    val city: String,
    val city_ascii: String,
    val lat: Double,
    val lng: Double,
    val country: String,
    val iso2: String,
    val iso3: String,
    val admin_name: String,
    val capital: String?,
    val population: Int?,
    @PrimaryKey val id: Int,
)
