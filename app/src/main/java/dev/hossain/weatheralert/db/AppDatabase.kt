package dev.hossain.weatheralert.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the weather alert app.
 * Mainly contains the cities with coordinates.
 *
 * - https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [City::class, Alert::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    abstract fun alertDao(): AlertDao
}
