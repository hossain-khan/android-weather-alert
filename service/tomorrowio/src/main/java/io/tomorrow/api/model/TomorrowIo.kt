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
 * Represents the real-time weather response from the Tomorrow.io API.
 *
 * Sample JSON:
 * ```json
 * {
 *   "data": {
 *     "time": "2025-01-12T01:40:00Z",
 *     "values": {
 *       "cloudBase": null,
 *       "cloudCeiling": null,
 *       "cloudCover": 5,
 *       "dewPoint": -4.88,
 *       "freezingRainIntensity": 0,
 *       "hailProbability": 97.7,
 *       "hailSize": 4.27,
 *       "humidity": 88,
 *       "precipitationProbability": 0,
 *       "pressureSurfaceLevel": 999.33,
 *       "rainIntensity": 0,
 *       "sleetIntensity": 0,
 *       "snowIntensity": 0,
 *       "temperature": -3.19,
 *       "temperatureApparent": -7.02,
 *       "uvHealthConcern": 0,
 *       "uvIndex": 0,
 *       "visibility": 16,
 *       "weatherCode": 1000,
 *       "windDirection": 270.38,
 *       "windGust": 6.5,
 *       "windSpeed": 2.69
 *     }
 *   },
 *   "location": {
 *     "lat": 43.653480529785156,
 *     "lon": -79.3839340209961,
 *     "name": "Toronto, Golden Horseshoe, Ontario, Canada",
 *     "type": "administrative"
 *   }
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class RealTimeWeatherResponse(
    /** Data containing the time and weather values. */
    @Json(name = "data") val data: TimelineData,
    /** Location details. */
    @Json(name = "location") val location: Location,
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
    /** Evapotranspiration (mm). */
    @Json(name = "evapotranspiration") val evapotranspiration: Double?,
    /** Freezing rain intensity (mm/hr). */
    @Json(name = "freezingRainIntensity") val freezingRainIntensity: Double?,
    /** Probability of hail occurrence (%). */
    @Json(name = "hailProbability") val hailProbability: Double?,
    /** Estimated size of hail (cm). */
    @Json(name = "hailSize") val hailSize: Double?,
    /** Relative humidity (%). */
    @Json(name = "humidity") val humidity: Double?,
    /** Ice accumulation (mm). */
    @Json(name = "iceAccumulation") val iceAccumulation: Double?,
    /** Ice accumulation liquid water equivalent (mm). */
    @Json(name = "iceAccumulationLwe") val iceAccumulationLwe: Double?,
    /** Probability of precipitation (%). */
    @Json(name = "precipitationProbability") val precipitationProbability: Int?,
    /** Surface level pressure (hPa). */
    @Json(name = "pressureSurfaceLevel") val pressureSurfaceLevel: Double?,
    /** Rain accumulation (mm). */
    @Json(name = "rainAccumulation") val rainAccumulation: Double?,
    /** Rain accumulation liquid water equivalent (mm). */
    @Json(name = "rainAccumulationLwe") val rainAccumulationLwe: Double?,
    /** Rainfall intensity (mm/hr). */
    @Json(name = "rainIntensity") val rainIntensity: Double?,
    /** Sleet accumulation (mm). */
    @Json(name = "sleetAccumulation") val sleetAccumulation: Double?,
    /** Sleet accumulation liquid water equivalent (mm). */
    @Json(name = "sleetAccumulationLwe") val sleetAccumulationLwe: Double?,
    /** Sleet intensity (mm/hr). */
    @Json(name = "sleetIntensity") val sleetIntensity: Double?,
    /** Snow accumulation (mm). */
    @Json(name = "snowAccumulation") val snowAccumulation: Double?,
    /** Snow accumulation liquid water equivalent (mm). */
    @Json(name = "snowAccumulationLwe") val snowAccumulationLwe: Double?,
    /** Snow depth (cm). */
    @Json(name = "snowDepth") val snowDepth: Double?,
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

/**
 * Represents an error response from the Tomorrow.io API.
 * See [API Reference for Error](https://docs.tomorrow.io/reference/api-errors).
 *
 * Sample JSON:
 * ```json
 * {
 *   "code": 429001,
 *   "type": "Too Many Calls",
 *   "message": "The request limit for this resource has been reached for the current rate limit window. Wait and retry the operation, or examine your API request volume."
 * }
 * ```
 *
 * ```json
 * {
 *   "code": 401001,
 *   "type": "Invalid Auth",
 *   "message": "The method requires authentication but it was not presented or is invalid."
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class TomorrowIoApiErrorResponse(
    /** Error code. */
    @Json(name = "code") val code: Int,
    /** Error type. */
    @Json(name = "type") val type: String,
    /** Error message. */
    @Json(name = "message") val message: String,
)
