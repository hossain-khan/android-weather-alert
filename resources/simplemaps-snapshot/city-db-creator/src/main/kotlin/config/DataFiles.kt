package dev.hossain.citydb.config

const val USA_COUNTRY_CODE = "USA"
const val CANADA_COUNTRY_CODE = "CAN"
const val DB_FILE_NAME_TODO_TEST = "db/todos.db"
const val DB_FILE_NAME_ALERT_APP = "../alertapp.db"
const val DB_TABLE_NAME_CITIES = "cities"
const val CSV_CANADIAN_CITIES = "../simplemaps_canadacities_basic/canadacities.csv"
const val CSV_USA_CITIES = "../simplemaps_uscities_basic/uscities.csv"
const val USA_CITIES_MATCHED_REPORT = "src/main/resources/CheckUsaCities.txt"
const val CANADA_CITIES_MATCHED_REPORT = "src/main/resources/CheckCanadianCities.txt"

fun escapeSingleQuote(input: String): String {
    return input.replace("'", "''")
}