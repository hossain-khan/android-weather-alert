package dev.hossain.citydb.config

internal const val USA_COUNTRY_CODE = "USA"
internal const val USA_COUNTRY_NAME = "USA"
internal const val CANADA_COUNTRY_CODE = "CAN"
internal const val CANADA_COUNTRY_NAME = "Canada"
internal const val DB_FILE_NAME_TODO_TEST = "db/todos.db"
internal const val DB_FILE_NAME_ALERT_APP = "../alertapp.db"
internal const val DB_FILE_NAME_ENHANCED_DB = "../alertapp-enhanced.db"
internal const val DB_TABLE_NAME_CITIES = "cities"
internal const val CANADA_CITY_ID_MUSK = 1000000000000
internal const val USA_CITY_ID_MUSK = 2000000000000
internal const val CSV_WORLD_CITIES = "../simplemaps_worldcities_basic/worldcities.csv"
internal const val CSV_CANADIAN_CITIES = "../simplemaps_canadacities_basic/canadacities.csv"
internal const val CSV_USA_CITIES = "../simplemaps_uscities_basic/uscities.csv"
internal const val USA_CITIES_MATCHED_REPORT = "src/main/resources/CheckUsaCities.txt"
internal const val USA_CITIES_ADD_REPORT = "src/main/resources/AddUsaCities.txt"
internal const val CANADA_CITIES_MATCHED_REPORT = "src/main/resources/CheckCanadianCities.txt"
internal const val CANADA_CITIES_ADD_REPORT = "src/main/resources/AddCanadianCities.txt"

fun escapeSingleQuote(input: String): String {
    return input.replace("'", "''")
}