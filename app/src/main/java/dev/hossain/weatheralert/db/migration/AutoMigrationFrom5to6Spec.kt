package dev.hossain.weatheralert.db.migration

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

class AutoMigrationFrom5to6Spec : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Added new column to store alert id in the city forecast table.
        db.execSQL("ALTER TABLE city_forecasts ADD COLUMN alert_id INTEGER NOT NULL DEFAULT 0")
    }
}
