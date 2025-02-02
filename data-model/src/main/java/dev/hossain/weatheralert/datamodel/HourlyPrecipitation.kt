package dev.hossain.weatheralert.datamodel

import java.time.format.DateTimeFormatter

data class HourlyPrecipitation(
    /**
     * ISO 8601 date-time string.
     *
     * Example:
     * - "2025-02-02T11:39:14.533Z"
     *
     * @see DateTimeFormatter.ISO_DATE_TIME
     */
    val isoDateTime: String,
    val rain: Double = 0.0,
    val snow: Double = 0.0,
)
