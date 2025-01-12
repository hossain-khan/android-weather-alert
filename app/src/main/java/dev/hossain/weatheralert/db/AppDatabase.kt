package dev.hossain.weatheralert.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the weather alert app.
 * Mainly contains the cities with coordinates.
 *
 * - https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [City::class, Alert::class, CityForecast::class],
    version = 2,
    exportSchema = true,
    // https://developer.android.com/training/data-storage/room/migrating-db-versions
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    abstract fun alertDao(): AlertDao

    abstract fun forecastDao(): CityForecastDao
}
