package dev.hossain.weatheralert.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *
 * ```sql
 * CREATE TABLE "cities" (
 * 	"city"	TEXT NOT NULL,
 * 	"city_ascii"	TEXT NOT NULL,
 * 	"lat"	REAL NOT NULL,
 * 	"lng"	REAL NOT NULL,
 * 	"country"	TEXT NOT NULL,
 * 	"iso2"	TEXT NOT NULL,
 * 	"iso3"	TEXT NOT NULL,
 * 	"admin_name"	TEXT,
 * 	"capital"	TEXT,
 * 	"population"	INTEGER,
 * 	"id"	INTEGER NOT NULL,
 * 	PRIMARY KEY("id")
 * )
 * ```
 */
@Entity(
    tableName = "cities",
    indices = [
        Index(name = "idx_country", value = ["country"], unique = false),
        Index(name = "idx_city_ascii", value = ["city_ascii"], unique = false),
        Index(name = "idx_city", value = ["city"], unique = false),
    ],
)
data class City(
    val city: String,
    val city_ascii: String,
    val lat: Double,
    val lng: Double,
    val country: String,
    val iso2: String,
    val iso3: String,
    val admin_name: String?,
    val capital: String?,
    val population: Int?,
    @PrimaryKey val id: Int,
)
