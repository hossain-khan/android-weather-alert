package dev.hossain.weatheralert.datamodel

/**
 * Contains minimum data required for weather alert app.
 * This data is cached in app database for offline use.
 */
data class AppForecastData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val snow: Snow = Snow(),
    val rain: Rain = Rain(),
    val hourlyPrecipitation: List<HourlyPrecipitation>,
)

data class Snow(
    val dailyCumulativeSnow: Double = 0.0,
    val nextDaySnow: Double = 0.0,
    /**
     * Weekly cumulative snowfall in millimeters.
     * NOTE: This data is not used in the app yet.
     */
    val weeklyCumulativeSnow: Double = 0.0,
)

data class Rain(
    val dailyCumulativeRain: Double = 0.0,
    val nextDayRain: Double = 0.0,
    /**
     * Weekly cumulative rainfall in millimeters.
     * NOTE: This data is not used in the app yet.
     */
    val weeklyCumulativeRain: Double = 0.0,
)
