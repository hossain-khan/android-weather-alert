package dev.hossain.weatheralert.db.migration

import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Auto migration from version 5 to 6.
 *
 * - https://developer.android.com/training/data-storage/room/migrating-db-versions#automigrationspec
 * - https://developer.android.com/reference/kotlin/androidx/room/AutoMigration
 */
@RenameColumn.Entries(
    RenameColumn(
        tableName = "city_forecasts",
        fromColumnName = "cityId",
        toColumnName = "city_id",
    ),
)
@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "city_forecasts",
        columnName = "cityId",
    ),
)
class AutoMigrationFrom5to6Spec : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Add the 'notes' column with a default value
        db.execSQL("ALTER TABLE city_forecasts ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
    }
}
