package dev.hossain.citydb

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.hossain.citydb.config.CSV_WORLD_CITIES
import java.io.File
import kotlin.time.measureTime

/**
 * CREATE TABLE "cities" (
 *    "city" TEXT NOT NULL,
 *    "city_ascii" TEXT NOT NULL,
 *    "lat" REAL NOT NULL,
 *    "lng" REAL NOT NULL,
 *    "country" TEXT NOT NULL,
 *    "iso2" TEXT NOT NULL,
 *    "iso3" TEXT NOT NULL,
 *    "admin_name" TEXT,
 *    "capital" TEXT,
 *    "population" INTEGER,
 *    "id" INTEGER NOT NULL,
 * PRIMARY KEY("id")
 * )
 */
fun main() {
    val elapsed = measureTime {
        // getWorldCities()
        loadWorldCities()
    }

    // loadWorldCities - Time elapsed: 183.077875ms
    // getWorldCities - Time elapsed: 128.520333ms
    println("Time elapsed: $elapsed")
}

private fun getWorldCities(): List<Map<String, String>> {
    val file: File = File(CSV_WORLD_CITIES)
    return csvReader().readAllWithHeader(file)
}


/**
 * Loads world cities from CSV file.
 *
 * ```csv
 * "city","city_ascii","lat","lng","country","iso2","iso3","admin_name","capital","population","id"
 * "Tokyo","Tokyo","35.6897","139.6922","Japan","JP","JPN","Tōkyō","primary","37732000","1392685764"
 * "Jakarta","Jakarta","-6.1750","106.8275","Indonesia","ID","IDN","Jakarta","primary","33756000","1360771077"
 * "Delhi","Delhi","28.6100","77.2300","India","IN","IND","Delhi","admin","32226000","1356872604"
 * "Guangzhou","Guangzhou","23.1300","113.2600","China","CN","CHN","Guangdong","admin","26940000","1156237133"
 * "Mumbai","Mumbai","19.0761","72.8775","India","IN","IND","Mahārāshtra","admin","24973000","1356226629"
 * "Manila","Manila","14.5958","120.9772","Philippines","PH","PHL","Manila","primary","24922000","1608618140"
 * "Shanghai","Shanghai","31.2286","121.4747","China","CN","CHN","Shanghai","admin","24073000","1156073548"
 * "São Paulo","Sao Paulo","-23.5500","-46.6333","Brazil","BR","BRA","São Paulo","admin","23086000","1076532519"
 * "Seoul","Seoul","37.5600","126.9900","Korea, South","KR","KOR","Seoul","primary","23016000","1410836482"
 * "Mexico City","Mexico City","19.4333","-99.1333","Mexico","MX","MEX","Ciudad de México","primary","21804000","1484247881"
 * "Cairo","Cairo","30.0444","31.2358","Egypt","EG","EGY","Al Qāhirah","primary","20296000","1818253931"
 * "New York","New York","40.6943","-73.9249","United States","US","USA","New York","","18908608","1840034016"
 * "Dhaka","Dhaka","23.7639","90.3889","Bangladesh","BD","BGD","Dhaka","primary","18627000","1050529279"
 * ```
 */
private fun loadWorldCities() {
    csvReader().open(CSV_WORLD_CITIES) {
        /*
         * Provides data like:
         *
         * row = {LinkedHashMap@1342}  size = 11
         *  "city" -> "Tokyo"
         *  "city_ascii" -> "Tokyo"
         *  "lat" -> "35.6897"
         *  "lng" -> "139.6922"
         *  "country" -> "Japan"
         *  "iso2" -> "JP"
         *  "iso3" -> "JPN"
         *  "admin_name" -> "Tōkyō"
         *  "capital" -> "primary"
         *  "population" -> "37732000"
         *  "id" -> "1392685764"
         *
         * row = {LinkedHashMap@1692}  size = 11
         *  "city" -> "New York"
         *  "city_ascii" -> "New York"
         *  "lat" -> "40.6943"
         *  "lng" -> "-73.9249"
         *  "country" -> "United States"
         *  "iso2" -> "US"
         *  "iso3" -> "USA"
         *  "admin_name" -> "New York"
         *  "capital" -> ""
         *  "population" -> "18908608"
         *  "id" -> "1840034016"
         */
        readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
            println(row)
        }
    }
}