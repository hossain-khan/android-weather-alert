package dev.hossain.citydb.config

// ISO 2-letter country code for the United States
internal const val USA_COUNTRY_CODE_ISO2 = "US"

// ISO 3-letter country code for the United States
internal const val USA_COUNTRY_CODE_ISO3 = "USA"

// Full country name for the United States
internal const val USA_COUNTRY_NAME = "United States"

// ISO 2-letter country code for Canada
internal const val CANADA_COUNTRY_CODE_ISO2 = "CA"

// ISO 3-letter country code for Canada
internal const val CANADA_COUNTRY_CODE_ISO3 = "CAN"

// Full country name for Canada
internal const val CANADA_COUNTRY_NAME = "Canada"

// File path for the TODOs test database that uses sample code from Android documentation
internal const val DB_FILE_NAME_TODO_TEST = "db/todos.db"

// File path for the original world cities database used by the alert app
internal const val DB_FILE_NAME_ALERT_APP = "../alertapp-original-world-cities.db"

// File path for the enhanced database with additional USA and Canada cities
internal const val DB_FILE_NAME_ENHANCED_DB = "../alertapp-add-on-usa-canada-cities.db"

// Table name for cities in the database
internal const val DB_TABLE_NAME_CITIES = "cities"

// ID offset for Canadian cities to avoid collisions with existing IDs
// So, item id with 1124279679 will be 1000000000000 + 1124279679 = 1001124279679
internal const val CANADA_CITY_ID_MUSK = 1000000000000

// ID offset for USA cities to avoid collisions with existing IDs
// So, item id with 1840034016 will be 2000000000000 + 1840034016 = 2001840034016
internal const val USA_CITY_ID_MUSK = 2000000000000

// File path for the CSV file containing world cities data
internal const val CSV_WORLD_CITIES = "../simplemaps_worldcities_basic/worldcities.csv"

// File path for the CSV file containing Canadian cities data
internal const val CSV_CANADIAN_CITIES = "../simplemaps_canadacities_basic/canadacities.csv"

// File path for the CSV file containing USA cities data
internal const val CSV_USA_CITIES = "../simplemaps_uscities_basic/uscities.csv"

// File path for the report of matched USA cities
internal const val USA_CITIES_MATCHED_REPORT = "src/main/resources/CheckUsaCities.txt"

// File path for the report of added USA cities
internal const val USA_CITIES_ADD_REPORT = "src/main/resources/AddUsaCities.txt"

// File path for the report of matched Canadian cities
internal const val CANADA_CITIES_MATCHED_REPORT = "src/main/resources/CheckCanadianCities.txt"

// File path for the report of added Canadian cities
internal const val CANADA_CITIES_ADD_REPORT = "src/main/resources/AddCanadianCities.txt"

/**
 * Escapes single quotes in the input string by replacing them with two single quotes.
 *
 * @param input The input string to escape.
 * @return The escaped string with single quotes replaced by two single quotes.
 */
fun escapeSingleQuote(input: String): String {
    return input.replace("'", "''")
}