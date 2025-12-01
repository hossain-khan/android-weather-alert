package dev.hossain.weatheralert.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.hossain.weatheralert.db.converter.Converters
import dev.hossain.weatheralert.db.migration.AutoMigrationFrom5to6Spec

/**
 * Room database for the weather alert app.
 * Mainly contains the cities with coordinates.
 *
 * - https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [City::class, Alert::class, CityForecast::class],
    version = 7,
    exportSchema = true,
    // https://developer.android.com/training/data-storage/room/migrating-db-versions
    // https://github.com/hossain-khan/android-weather-alert/issues/272#issuecomment-2629512823
    // https://medium.com/androiddevelopers/room-auto-migrations-d5370b0ca6eb
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = AutoMigrationFrom5to6Spec::class),
        AutoMigration(from = 6, to = 7),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    abstract fun alertDao(): AlertDao

    abstract fun forecastDao(): CityForecastDao
}
