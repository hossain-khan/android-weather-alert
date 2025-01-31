package dev.hossain.citydb

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.hossain.citydb.config.*
import java.io.File
import kotlin.time.measureTime

/**
 * Checks if the USA cities are in the database.
 */
fun main() {
    val elapsed = measureTime {
        //loadUsaCities()

        // Total USA cities: 31120 and matches: 605
        //executeCityMatching()

        //debugCityMatching()

        // Total USA cities: 31120 and Collision matches: 4566
        // Conclusion: ❌ ID collision between world cities and USA cities
        findIdCollision()
    }

    // executeCityMatching - Time elapsed 1m 3.469409584s
    // debugCityMatching - Time elapsed: 1m 2.940418500s
    // findIdCollision() - Time elapsed: 655.892917ms
    println("Time elapsed: $elapsed")
}

/**
 * Writes report to [USA_CITIES_MATCHED_REPORT] file.
 */
private fun debugCityMatching() {
    val outputFile = File(USA_CITIES_MATCHED_REPORT)
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ALERT_APP)
    outputFile.bufferedWriter().use { writer ->
        val countSql = "SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES WHERE 1"
        databaseConnection.prepare(countSql).use { stmt ->
            while (stmt.step()) {
                writer.write("Total records: ${stmt.getText(0)}\n")
            }
        }

        val usaCities = getUsaCities()
        usaCities.forEach { csvCity ->
            val citySql = """
                SELECT * FROM $DB_TABLE_NAME_CITIES
                WHERE city_ascii = '${escapeSingleQuote(csvCity["city_ascii"]!!)}' AND iso3 = '$USA_COUNTRY_CODE_ISO3'
            """.trimIndent()

            var didFindMatch = false
            databaseConnection.prepare(citySql).use { stmt ->
                while (stmt.step()) {
                    val city = stmt.getText(1)
                    val lat = stmt.getDouble(2)
                    val lng = stmt.getDouble(3)
                    val state = stmt.getText(7)
                    val country = stmt.getText(6)

                    writer.write("""
                        CSV City: ${csvCity["city"]}, ${csvCity["state_name"]} ${csvCity["lat"]} ${csvCity["lng"]} matched with DB 
                        City: $city, state: $state, Lat: $lat, Lng: $lng, Country: $country (Matches - state: ${state == csvCity["state_name"]} lat: ${lat == csvCity["lat"]?.toDouble()} lng: ${lng == csvCity["lng"]?.toDouble()})
                        - - - - - - - - - - - - - - - - - - - - - - - -
                    """.trimIndent() + "\n")

                    println("CSV City: ${csvCity["city"]}, ${csvCity["state_name"]} ${csvCity["lat"]} ${csvCity["lng"]} matched with DB City: $city, state: $state, Lat: $lat, Lng: $lng, Country: $country")

                    didFindMatch = true
                }
            }
            if(didFindMatch) {
                writer.write("\n\n ================= END CITY MATCH ================= \n\n")
            }
        }
    }
    databaseConnection.close()
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
            WHERE city_ascii = '${escapeSingleQuote(city["city_ascii"]!!)}' AND iso3 = '$USA_COUNTRY_CODE_ISO3'
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


private fun findIdCollision() {
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ALERT_APP)
    val countSql = "SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES WHERE 1"
    databaseConnection.prepare(countSql).use { stmt ->
        while (stmt.step()) {
            println("Total DB records: ${stmt.getText(0)}")
        }
    }

    val usaCities = getUsaCities()
    val totalCities = usaCities.size
    var totalCollisionMatches = 0
    // For each usa city from csv, check if the id matches with world cities
    // Report: Total USA cities: 31120 and Collision matches: 4566
    usaCities.forEach { city ->
        val citySql = """
            SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES
            WHERE id = ${city["id"]!!.toLong()} AND iso3 = '$USA_COUNTRY_CODE_ISO3'
        """.trimIndent()

        databaseConnection.prepare(citySql).use { stmt ->
            while (stmt.step()) {
                if (stmt.getText(0).toInt() > 0) {
                    totalCollisionMatches++
                }
                println("City: ${city["city"]}, State: ${city["state_name"]} - Count: ${stmt.getText(0)}")
            }
        }
    }

    println("Total USA cities: $totalCities and Collision matches: $totalCollisionMatches")

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