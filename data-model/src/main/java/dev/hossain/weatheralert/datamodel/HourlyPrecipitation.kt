package dev.hossain.weatheralert.datamodel

import java.time.format.DateTimeFormatter

data class HourlyPrecipitation(
    /**
     * ISO 8601 date-time string.
     * @see DateTimeFormatter.ISO_DATE_TIME
     */
    val isoDateTime: String,
    val rain: Double,
    val snow: Double,
)
