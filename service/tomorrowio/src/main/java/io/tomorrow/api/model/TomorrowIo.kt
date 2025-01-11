package io.tomorrow.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the main response from the Tomorrow.io API.
 */
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    /** Location data with latitude and longitude. */
    @Json(name = "location") val location: Location,
    /** Timelines providing weather data across different intervals. */
    @Json(name = "timelines") val timelines: Timelines,
)

/**
 * Represents the location details.
 */
@JsonClass(generateAdapter = true)
data class Location(
    /** Latitude of the location. */
    @Json(name = "lat") val latitude: Double,
    /** Longitude of the location. */
    @Json(name = "lon") val longitude: Double,
)

/**
 * Represents the timelines for weather data.
 */
@JsonClass(generateAdapter = true)
data class Timelines(
    /** Minutely weather data. */
    @Json(name = "minutely") val minutely: List<TimelineData>,
    /** Hourly weather data. */
    @Json(name = "hourly") val hourly: List<TimelineData>,
    /** Daily weather data. */
    @Json(name = "daily") val daily: List<TimelineData>,
)

/**
 * Represents individual timeline data.
 */
@JsonClass(generateAdapter = true)
data class TimelineData(
    /** The timestamp for the data point. */
    @Json(name = "time") val time: String,
    /** The weather values for the data point. */
    @Json(name = "values") val values: WeatherValues,
)

/**
 * Represents weather-related values for a specific timeline data point.
 */
@JsonClass(generateAdapter = true)
data class WeatherValues(
    /** Cloud base height (km). */
    @Json(name = "cloudBase") val cloudBase: Double?,
    /** Cloud ceiling height (km). */
    @Json(name = "cloudCeiling") val cloudCeiling: Double?,
    /** Cloud cover percentage. */
    @Json(name = "cloudCover") val cloudCover: Double?,
    /** Dew point temperature (°C). */
    @Json(name = "dewPoint") val dewPoint: Double?,
    /** Freezing rain intensity (mm/hr). */
    @Json(name = "freezingRainIntensity") val freezingRainIntensity: Double?,
    /** Probability of hail occurrence (%). */
    @Json(name = "hailProbability") val hailProbability: Double?,
    /** Estimated size of hail (cm). */
    @Json(name = "hailSize") val hailSize: Double?,
    /** Relative humidity (%). */
    @Json(name = "humidity") val humidity: Double?,
    /** Probability of precipitation (%). */
    @Json(name = "precipitationProbability") val precipitationProbability: Int?,
    /** Surface level pressure (hPa). */
    @Json(name = "pressureSurfaceLevel") val pressureSurfaceLevel: Double?,
    /** Rainfall intensity (mm/hr). */
    @Json(name = "rainIntensity") val rainIntensity: Double?,
    /** Sleet intensity (mm/hr). */
    @Json(name = "sleetIntensity") val sleetIntensity: Double?,
    /** Snowfall intensity (mm/hr). */
    @Json(name = "snowIntensity") val snowIntensity: Double?,
    /** Temperature (°C). */
    @Json(name = "temperature") val temperature: Double?,
    /** Apparent temperature (°C). */
    @Json(name = "temperatureApparent") val temperatureApparent: Double?,
    /** UV health concern index. */
    @Json(name = "uvHealthConcern") val uvHealthConcern: Int?,
    /** UV index. */
    @Json(name = "uvIndex") val uvIndex: Int?,
    /** Visibility distance (km). */
    @Json(name = "visibility") val visibility: Double?,
    /** Weather code representing conditions. */
    @Json(name = "weatherCode") val weatherCode: Int?,
    /** Wind direction (degrees). */
    @Json(name = "windDirection") val windDirection: Double?,
    /** Wind gust speed (m/s). */
    @Json(name = "windGust") val windGust: Double?,
    /** Wind speed (m/s). */
    @Json(name = "windSpeed") val windSpeed: Double?,
)
