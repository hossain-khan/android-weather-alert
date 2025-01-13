package dev.hossain.citydb

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.use
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.hossain.citydb.config.CSV_USA_CITIES
import dev.hossain.citydb.config.DB_FILE_NAME_ALERT_APP
import dev.hossain.citydb.config.escapeSingleQuote
import java.io.File
import kotlin.time.measureTime

/**
 * Checks if the USA cities are in the database.
 */
fun main() {
    val elapsed = measureTime {
        executeCityMatching()
    }
    // Time elapsed for executeCityMatching: 63469 milliseconds (1m 3.469409584s)
    println("Time elapsed for executeCityMatching: ${elapsed.inWholeMilliseconds} milliseconds ($elapsed)")

    //loadUsaCities()
}

private fun executeCityMatching() {
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ALERT_APP)
    val countSql = "SELECT COUNT(*) FROM cities WHERE 1"
    databaseConnection.prepare(countSql).use { stmt ->
        while (stmt.step()) {
            println("Total records: ${stmt.getText(0)}")
        }
    }

    val usaCities = getUsaCities()
    val totalUsaCities = usaCities.size
    var totalMatches = 0
    var totalMultiMatch = 0
    // For each usa city, check if it exists in the cities table
    // Result: Total USA cities: 31120 and matches: 605
    usaCities.forEach { city ->
        val citySql = """
            SELECT COUNT(*) FROM cities
            WHERE city_ascii = '${escapeSingleQuote(city["city_ascii"]!!)}' AND iso3 = 'USA'
        """.trimIndent()

        databaseConnection.prepare(citySql).use { stmt ->
            //stmt.bindText(1, city["city_ascii"]!!)
            while (stmt.step()) {
                val matchedCount = stmt.getText(0).toInt()
                if (matchedCount > 0) {
                    totalMatches++
                    totalMultiMatch += matchedCount
                }
                println("City: ${city["city"]}, Province: ${city["state_name"]} - Count: ${matchedCount}")
            }
        }
    }

    println("Total USA cities: $totalUsaCities and matches: $totalMatches with multi-match: $totalMultiMatch")

    databaseConnection.close()
}

/**
 * Loads USA cities from CSV file.
 *
 * ```csv
 * "city","city_ascii","state_id","state_name","county_fips","county_name","lat","lng","population","density","source","military","incorporated","timezone","ranking","zips","id"
 * "New York","New York","NY","New York","36081","Queens","40.6943","-73.9249","18908608","11080.3","shape","FALSE","TRUE","America/New_York","1","11229 11228 11226 11225","1840034016"
 * "Los Angeles","Los Angeles","CA","California","06037","Los Angeles","34.1141","-118.4068","11922389","3184.7","shape","FALSE","TRUE","America/Los_Angeles","1","91367 90291 90293","1840020491"
 * "Chicago","Chicago","IL","Illinois","17031","Cook","41.8375","-87.6866","8497759","4614.5","shape","FALSE","TRUE","America/Chicago","1","60018 60649","1840000494"
 * ```
 */
private fun loadUsaCities() {
    csvReader().open(CSV_USA_CITIES) {
        /*
         * Provides data like:
         *
         * "city" -> "New York"
         * "city_ascii" -> "New York"
         * "state_id" -> "NY"
         * "state_name" -> "New York"
         * "county_fips" -> "36081"
         * "county_name" -> "Queens"
         * "lat" -> "40.6943"
         * "lng" -> "-73.9249"
         * "population" -> "18908608"
         * "density" -> "11080.3"
         * "source" -> "shape"
         * "military" -> "FALSE"
         * "incorporated" -> "TRUE"
         * "timezone" -> "America/New_York"
         * "ranking" -> "1"
         * "zips" -> "11229 11228 11226 11225 11224 11222 ..."
         * "id" -> "1840034016"
         */
        readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
            println(row)
        }
    }
}

private fun getUsaCities(): List<Map<String, String>> {
    val file: File = File(CSV_USA_CITIES)
    return csvReader().readAllWithHeader(file)
}