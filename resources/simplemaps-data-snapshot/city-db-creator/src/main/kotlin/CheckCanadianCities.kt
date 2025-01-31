package dev.hossain.citydb

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.use
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.hossain.citydb.config.*
import java.io.File
import kotlin.time.measureTime

/**
 * Checks if the Canadian cities are in the database.
 */
fun main() {
    val elapsed = measureTime {
        // Total Canadian cities: 1737 and matches: 466
        executeCityMatch()

        //debugCityMatching()

        // Total Canadian cities: 1737 and Collision matches: 444
        //findIdCollision()
    }

    // debugCityMatching - Time elapsed: 3.672165291s
    // executeCityMatch- Time elapsed: 3.711421833s
    println("Time elapsed: $elapsed")
}


private fun debugCityMatching() {
    val outputFile = File(CANADA_CITIES_MATCHED_REPORT)
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ALERT_APP)
    outputFile.bufferedWriter().use { writer ->
        val countSql = "SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES WHERE 1"
        databaseConnection.prepare(countSql).use { stmt ->
            while (stmt.step()) {
                writer.write("Total DB records: ${stmt.getText(0)}\n\n")
            }
        }

        val canadianCities = getCanadianCities()
        canadianCities.forEach { csvCity ->
            val citySql = """
                SELECT * FROM $DB_TABLE_NAME_CITIES
                WHERE city_ascii = '${escapeSingleQuote(csvCity["city_ascii"]!!)}' AND iso3 = '$CANADA_COUNTRY_CODE_ISO3'
            """.trimIndent()

            var didFindMatch = false
            databaseConnection.prepare(citySql).use { stmt ->
                while (stmt.step()) {
                    val city = stmt.getText(1)
                    val lat = stmt.getDouble(2)
                    val lng = stmt.getDouble(3)
                    val province = stmt.getText(7)
                    val country = stmt.getText(6)

                    writer.write("""
                        CSV City: ${csvCity["city"]}, ${csvCity["province_name"]} ${csvCity["lat"]} ${csvCity["lng"]} matched with DB 
                        City: $city, state: $province, Lat: $lat, Lng: $lng, Country: $country (Matches - province: ${province == csvCity["province_name"]} lat: ${lat == csvCity["lat"]?.toDouble()} lng: ${lng == csvCity["lng"]?.toDouble()})
                        - - - - - - - - - - - - - - - - - - - - - - - -
                    """.trimIndent() + "\n")

                    println("CSV City: ${csvCity["city"]}, ${csvCity["province_name"]} ${csvCity["lat"]} ${csvCity["lng"]} matched with DB City: $city, state: $province, Lat: $lat, Lng: $lng, Country: $country")

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


private fun executeCityMatch() {
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ALERT_APP)
    val countSql = "SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES WHERE 1"
    databaseConnection.prepare(countSql).use { stmt ->
        while (stmt.step()) {
            println("Total records: ${stmt.getText(0)}")
        }
    }

    val canadianCities = getCanadianCities()
    val totalCanadianCities = canadianCities.size
    var totalMatches = 0
    // For each canadian city, check if it exists in the cities table
    canadianCities.forEach { city ->
        val citySql = """
            SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES
            WHERE city_ascii = '${escapeSingleQuote(city["city_ascii"]!!)}' AND iso3 = '$CANADA_COUNTRY_CODE_ISO3'
        """.trimIndent()

        databaseConnection.prepare(citySql).use { stmt ->
            while (stmt.step()) {
                if (stmt.getText(0).toInt() > 0) {
                    totalMatches++
                }
                println("ID: ${city["id"]} City: ${city["city"]}, Province: ${city["province_id"]} - Count: ${stmt.getText(0)}")
            }
        }
    }

    println("Total Canadian cities: $totalCanadianCities and matches: $totalMatches")

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

    val canadianCities = getCanadianCities()
    val totalCanadianCities = canadianCities.size
    var totalCollisionMatches = 0
    // For each canadian city, check if the id matches with world cities
    // Total Canadian cities: 1737 and Collision matches: 444
    canadianCities.forEach { city ->
        val citySql = """
            SELECT COUNT(*) FROM $DB_TABLE_NAME_CITIES
            WHERE id = ${city["id"]!!.toLong()} AND iso3 = '$CANADA_COUNTRY_CODE_ISO3'
        """.trimIndent()

        databaseConnection.prepare(citySql).use { stmt ->
            while (stmt.step()) {
                if (stmt.getText(0).toInt() > 0) {
                    totalCollisionMatches++
                }
                println("City: ${city["city"]}, Province: ${city["province_id"]} - Count: ${stmt.getText(0)}")
            }
        }
    }

    println("Total Canadian cities: $totalCanadianCities and Collision matches: $totalCollisionMatches")

    databaseConnection.close()
}


/**
 * Loads Canadian cities from CSV file.
 *
 * ```csv
 * "city","city_ascii","province_id","province_name","lat","lng","population","density","timezone","ranking","postal","id"
 * "Hamilton","Hamilton","ON","Ontario","43.2567","-79.8692","729560","509.1","America/Toronto","2","L0R L0P L8W L8V L8T L8S L8R L8P L8G L8E L8B L8N L8M L8L L8K L8J L8H L9G L9A L9B L9C L9H L9K N1R","1124567288"
 * "Mississauga","Mississauga","ON","Ontario","43.6000","-79.6500","717961","2452.5","America/Toronto","2","L4W L4V L4T L4Z L4Y L4X L5R L5S L5T L5V L5W L5A L5B L5C L5E L5G L5H L5J L5K L5L L5M L5N","1124112672"
 * "Brampton","Brampton","ON","Ontario","43.6833","-79.7667","656480","2469.0","America/Toronto","2","L7A L6T L6W L6V L6P L6S L6R L6Y L6X L6Z","1124625989"
 * "Surrey","Surrey","BC","British Columbia","49.1900","-122.8489","568322","1797.9","America/Vancouver","2","V4A V4N V4P V3R V3S V3W V3T V3V V3X V3Z","1124001454"
 * "Kitchener","Kitchener","ON","Ontario","43.4186","-80.4728","522888","1877.6","America/Toronto","1","N2K N2H N2N N2M N2C N2B N2A N2G N2E N2R N2P","1124158530"
 * ```
 */
private fun loadCanadianCities() {
    csvReader().open(CSV_CANADIAN_CITIES) {
        /*
         * Provides data like:
         *
         * row = {LinkedHashMap@1495}  size = 12
         *  "city" -> "Toronto"
         *  "city_ascii" -> "Toronto"
         *  "province_id" -> "ON"
         *  "province_name" -> "Ontario"
         *  "lat" -> "43.7417"
         *  "lng" -> "-79.3733"
         *  "population" -> "5647656"
         *  "density" -> "4427.8"
         *  "timezone" -> "America/Toronto"
         *  "ranking" -> "1"
         *  "postal" -> "M5T M5V M5P M5S M5R M5E"
         *  "id" -> "1124279679"
         */
        readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
            println(row)
        }
    }
}

private fun getCanadianCities(): List<Map<String, String>> {
    val file: File = File(CSV_CANADIAN_CITIES)
    return csvReader().readAllWithHeader(file)
}