package dev.hossain.citydb

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import dev.hossain.citydb.config.DB_FILE_NAME_TODO_TEST

/**
 * Source: https://developer.android.com/kotlin/multiplatform/sqlite#sqlite_driver_apis
 */
fun main() {
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_TODO_TEST)
    databaseConnection.execSQL(
        "CREATE TABLE IF NOT EXISTS Todo (id INTEGER PRIMARY KEY, content TEXT)"
    )
    databaseConnection.prepare(
        "INSERT OR IGNORE INTO Todo (id, content) VALUES (? ,?)"
    ).use { stmt ->
        stmt.bindInt(index = 1, value = 1)
        stmt.bindText(index = 2, value = "Try Room in the KMP project.")
        stmt.step()
    }
    databaseConnection.prepare("SELECT content FROM Todo").use { stmt ->
        while (stmt.step()) {
            println("Action item: ${stmt.getText(0)}")
        }
    }
    databaseConnection.close()
}