package dev.hossain.citydb

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.use
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.hossain.citydb.config.*
import java.io.BufferedWriter
import java.io.File
import kotlin.time.measureTime

/**
 * This build upon existing world cities and adds USA and Canada cities that are missing in original table.
 *
 * See existing database - [DB_FILE_NAME_ALERT_APP]
 * The database where data is added on - [DB_FILE_NAME_ENHANCED_DB]
 */
fun main() {
    val elapsed = measureTime {
        addMissingCanadianCities()
        addMissingUsaCities()
    }

    // addMissingCanadianCities - Time elapsed: 4.278676250s
    // addMissingUsaCities - Time elapsed: 1m 32.431552958s
    println("Time elapsed: $elapsed")
}


/**
 * Adds missing Canadian cities to the database.
 */
private fun addMissingCanadianCities() {
    val outputFile = File(CANADA_CITIES_ADD_REPORT)
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ENHANCED_DB)
    outputFile.bufferedWriter().use { writer ->
        val csvCities = getCanadianCities()
        csvCities.forEachIndexed { index, csvCity ->
            println(">>> 👀 Processing city# ${index + 1}")
            val citySql = """
                    SELECT * FROM $DB_TABLE_NAME_CITIES
                    WHERE city_ascii = '${escapeSingleQuote(csvCity["city_ascii"]!!)}' AND iso3 = '$CANADA_COUNTRY_CODE_ISO3'
                """.trimIndent()

            val missingCitiesToInsert = mutableListOf<Map<String, String>>()
            var foundMatchForCityName = false
            databaseConnection.prepare(citySql).use { stmt ->
                while (stmt.step()) {
                    foundMatchForCityName = true
                    val cityAsciiName = stmt.getText(1)
                    val lat = stmt.getDouble(2)
                    val lng = stmt.getDouble(3)
                    val province = stmt.getText(7)
                    val country = stmt.getText(6)

                    val matchesProvince = province == csvCity["province_name"]
                    val matchesCityName = cityAsciiName == csvCity["city_ascii"]
                    val matchesLat = lat == csvCity["lat"]?.toDouble()
                    val matchesLng = lng == csvCity["lng"]?.toDouble()

                    if ((matchesProvince && matchesCityName).not()) {
                        println("City: $cityAsciiName, state: $province, Lat: $lat, Lng: $lng, Country: $country (Matches - province: $matchesProvince lat: $matchesLat lng: $matchesLng)")
                        missingCitiesToInsert.add(csvCity)
                    } else {
                        println("City: $cityAsciiName, state: $province, Lat: $lat, Lng: $lng, Country: $country (Matches - province: $matchesProvince lat: $matchesLat lng: $matchesLng)")
                    }
                }
            } // end collecting missing cities

            if (foundMatchForCityName.not()) {
                // No match found for city name - so insert this new city from CSV
                println("City: ${csvCity["city_ascii"]} not found in DB")
                missingCitiesToInsert.add(csvCity)
            }

            if (missingCitiesToInsert.isNotEmpty()) {
                val timeToInsert = measureTime {
                    insertMissingCanadianCities(databaseConnection, writer, missingCitiesToInsert)
                }

                writer.write("\n\n Inserted ${missingCitiesToInsert.size} cities took time: $timeToInsert")
                writer.write("\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - \n\n")
                println("\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - \n\n")
            }
        }
    }
    databaseConnection.close()
}

/**
 * Inserts missing Canadian cities into the database.
 *
 * Verify after insertion.
 * ```sql
 * SELECT * FROM cities WHERE iso3='CAN' ORDER BY city_ascii ASC
 * ```
 */
fun insertMissingCanadianCities(
    databaseConnection: SQLiteConnection,
    writer: BufferedWriter,
    missingCitiesToInsert: MutableList<Map<String, String>>
) {
    println(">>>> Going to insert these missing cities: $missingCitiesToInsert")
    // World city Min id: 1004003059
    // World city Max id: 1934976309
    // Canada city id :   1124399363
    // Canada city id adder: 1000000000000 = 1001124399363 (1124399363 + 1000000000000)
    // SQLite INT range - which is -9223372036854775808 to 9223372036854775807.
    missingCitiesToInsert.forEach { csvCity ->
        // SQL to insert into cities table
        // TABLE "cities" ( "city" TEXT, "city_ascii" TEXT, "lat" REAL, "lng" REAL, "country" TEXT, "iso2" TEXT, "iso3" TEXT, "admin_name" TEXT, "capital" TEXT, "population" INTEGER, "id" INTEGER )
        val insertCitySql = """
        INSERT OR IGNORE INTO $DB_TABLE_NAME_CITIES (city, city_ascii, lat, lng, country, iso2, iso3, admin_name, capital, population, id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()
        databaseConnection.prepare(insertCitySql).use { stmt ->
            stmt.bindText(1, csvCity["city"]!!)
            stmt.bindText(2, csvCity["city_ascii"]!!)
            stmt.bindDouble(3, csvCity["lat"]!!.toDouble())
            stmt.bindDouble(4, csvCity["lng"]!!.toDouble())
            stmt.bindText(5, CANADA_COUNTRY_NAME)
            stmt.bindText(6, CANADA_COUNTRY_CODE_ISO2)
            stmt.bindText(7, CANADA_COUNTRY_CODE_ISO3)
            stmt.bindText(8, csvCity["province_name"]!!)
            stmt.bindText(9, "")
            stmt.bindInt(10, csvCity["population"]?.toDouble()?.toInt() ?: 0)
            stmt.bindLong(11, csvCity["id"]!!.toLong() + CANADA_CITY_ID_MUSK)

            val isInserted = stmt.step()

            writer.write("Inserted: ${csvCity["city"]} in ${csvCity["province_name"]} \n")
            println("Inserted: ${csvCity["city"]} in ${csvCity["province_name"]} - Inserted state: $isInserted")
        }
    }
}


/**
 * Adds missing USA cities to the database.
 */
private fun addMissingUsaCities() {
    val outputFile = File(USA_CITIES_ADD_REPORT)
    val databaseConnection = BundledSQLiteDriver().open(DB_FILE_NAME_ENHANCED_DB)
    outputFile.bufferedWriter().use { writer ->
        val csvCities = getUsaCities()
        csvCities.forEachIndexed { index, csvCity ->
            println(">>> 👀 Processing city# ${index + 1}")
            val citySql = """
                    SELECT * FROM $DB_TABLE_NAME_CITIES
                    WHERE city_ascii = '${escapeSingleQuote(csvCity["city_ascii"]!!)}' AND iso3 = '$USA_COUNTRY_CODE_ISO3'
                """.trimIndent()

            val missingCitiesToInsert = mutableListOf<Map<String, String>>()
            var foundMatchForCityName = false
            databaseConnection.prepare(citySql).use { stmt ->
                while (stmt.step()) {
                    foundMatchForCityName = true
                    val cityAsciiName = stmt.getText(1)
                    val lat = stmt.getDouble(2)
                    val lng = stmt.getDouble(3)
                    val stateName = stmt.getText(7)
                    val country = stmt.getText(6)

                    val matchesUsState = stateName == csvCity["state_name"]
                    val matchesCityName = cityAsciiName == csvCity["city_ascii"]
                    val matchesLat = lat == csvCity["lat"]?.toDouble()
                    val matchesLng = lng == csvCity["lng"]?.toDouble()

                    if ((matchesUsState && matchesCityName).not()) {
                        println("City: $cityAsciiName, state: $stateName, Lat: $lat, Lng: $lng, Country: $country (Matches - state: $matchesUsState lat: $matchesLat lng: $matchesLng)")
                        missingCitiesToInsert.add(csvCity)
                    } else {
                        println("City: $cityAsciiName, state: $stateName, Lat: $lat, Lng: $lng, Country: $country (Matches - state: $matchesUsState lat: $matchesLat lng: $matchesLng)")
                    }
                }
            } // end collecting missing cities

            if (foundMatchForCityName.not()) {
                // No match found for city name - so insert this new city from CSV
                println("City: ${csvCity["city_ascii"]} not found in DB")
                missingCitiesToInsert.add(csvCity)
            }

            if (missingCitiesToInsert.isNotEmpty()) {
                val timeToInsert = measureTime {
                    insertMissingUsaCities(databaseConnection, writer, missingCitiesToInsert)
                }

                writer.write("\n\n Inserted ${missingCitiesToInsert.size} cities took time: $timeToInsert")
                writer.write("\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - \n\n")
                println("\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - \n\n")
            }
        }
    }
    databaseConnection.close()
}


/**
 * Inserts missing USA cities into the database.
 *
 * Verify after insertion.
 * ```sql
 * SELECT * FROM cities WHERE iso3='CAN' ORDER BY city_ascii ASC
 * ```
 */
fun insertMissingUsaCities(
    databaseConnection: SQLiteConnection,
    writer: BufferedWriter,
    missingCitiesToInsert: MutableList<Map<String, String>>
) {
    println(">>>> Going to insert these missing cities: $missingCitiesToInsert")
    // World city Min id: 1004003059
    // World city Max id: 1934976309
    // USA city id :   1840006107
    // USA city id adder: 2000000000000 = 2001840006107 (1840006107 + 2000000000000)
    // SQLite INT range - which is -9223372036854775808 to 9223372036854775807.
    missingCitiesToInsert.forEach { csvCity ->
        // SQL to insert into cities table
        // TABLE "cities" ( "city" TEXT, "city_ascii" TEXT, "lat" REAL, "lng" REAL, "country" TEXT, "iso2" TEXT, "iso3" TEXT, "admin_name" TEXT, "capital" TEXT, "population" INTEGER, "id" INTEGER )
        val insertCitySql = """
        INSERT OR IGNORE INTO $DB_TABLE_NAME_CITIES (city, city_ascii, lat, lng, country, iso2, iso3, admin_name, capital, population, id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()
        databaseConnection.prepare(insertCitySql).use { stmt ->
            stmt.bindText(1, csvCity["city"]!!)
            stmt.bindText(2, csvCity["city_ascii"]!!)
            stmt.bindDouble(3, csvCity["lat"]!!.toDouble())
            stmt.bindDouble(4, csvCity["lng"]!!.toDouble())
            stmt.bindText(5, USA_COUNTRY_NAME)
            stmt.bindText(6, USA_COUNTRY_CODE_ISO2)
            stmt.bindText(7, USA_COUNTRY_CODE_ISO3)
            stmt.bindText(8, csvCity["state_name"]!!)
            stmt.bindText(9, "")
            stmt.bindInt(10, csvCity["population"]?.toDouble()?.toInt() ?: 0)
            stmt.bindLong(11, csvCity["id"]!!.toLong() + USA_CITY_ID_MUSK)

            val isInserted = stmt.step()

            writer.write("Inserted: ${csvCity["city"]} in ${csvCity["state_name"]} \n")
            println("Inserted: ${csvCity["city"]} in ${csvCity["state_name"]} - Inserted state: $isInserted")
        }
    }
}


/**
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
private fun getCanadianCities(): List<Map<String, String>> {
    val file: File = File(CSV_CANADIAN_CITIES)
    return csvReader().readAllWithHeader(file)
}

/**
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
private fun getUsaCities(): List<Map<String, String>> {
    val file: File = File(CSV_USA_CITIES)
    return csvReader().readAllWithHeader(file)
}