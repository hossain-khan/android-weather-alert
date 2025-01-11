package org.openweathermap.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Weather forecast data.
 *
 * Sample JSON:
 * ```json
 * {
 *   "lat": 38.4685,
 *   "lon": -100.9596,
 *   "timezone": "America/Chicago",
 *   "timezone_offset": -21600,
 *   "current": {},
 *   "hourly": [],
 *   "daily": [],
 *   "alerts": []
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class WeatherForecast(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @Json(name = "timezone_offset") val timezoneOffset: Int,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    @Json(name = "dt") val date: Long,
    @Json(name = "temp") val temperature: Temperature,
    @Json(name = "weather") val weather: List<WeatherDescription>,
    @Json(name = "rain") val rainVolume: Double?,
    @Json(name = "snow") val snowVolume: Double?,
)

/**
 * Hourly forecast data.
 *
 * Sample JSON:
 * ```json
 * {
 *   "dt": 1736024400,
 *   "temp": -3.8,
 *   "feels_like": -10.8,
 *   "pressure": 1012,
 *   "humidity": 86,
 *   "dew_point": -6.03,
 *   "uvi": 0.09,
 *   "clouds": 100,
 *   "visibility": 3002,
 *   "wind_speed": 8.17,
 *   "wind_deg": 274,
 *   "wind_gust": 13.8,
 *   "weather": [
 *     {
 *       "id": 600,
 *       "main": "Snow",
 *       "description": "light snow",
 *       "icon": "13d"
 *     }
 *   ],
 *   "pop": 1,
 *   "snow": {
 *     "1h": 0.35
 *   },
 *   "rain": {
 *     "1h": 4.86
 *   }
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class HourlyForecast(
    @Json(name = "dt") val date: Long, // Unix timestamp
    @Json(name = "temp") val temperature: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "pressure") val pressure: Int,
    @Json(name = "humidity") val humidity: Int,
    @Json(name = "dew_point") val dewPoint: Double,
    @Json(name = "uvi") val uvi: Double,
    @Json(name = "clouds") val clouds: Int,
    @Json(name = "visibility") val visibility: Int? = null,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "wind_deg") val windDeg: Int,
    @Json(name = "wind_gust") val windGust: Double,
    @Json(name = "weather") val weather: List<WeatherDescription>,
    @Json(name = "pop") val pop: Double,
    @Json(name = "snow") val snow: SnowVolume? = null,
    @Json(name = "rain") val rain: RainVolume? = null,
)

/**
 * Snow volume data.
 *
 * Sample JSON:
 * ```json
 * "snow":
 * {
 *   "1h": 0.35
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class SnowVolume(
    @Json(name = "1h") val snowVolumeInAnHour: Double,
)

/**
 * Rain volume data.
 *
 * Sample JSON:
 * ```json
 * "rain":
 * {
 *   "1h": 2.29
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class RainVolume(
    @Json(name = "1h") val rainVolumeInAnHour: Double,
)

@JsonClass(generateAdapter = true)
data class Temperature(
    val min: Double,
    val max: Double,
)

/**
 * Weather description data.
 *
 * Sample JSON:
 * ```json
 * {
 *   "id": 600,
 *   "main": "Snow",
 *   "description": "light snow",
 *   "icon": "13n"
 * }
 * ```
 *
 * ```json
 * {
 *   "id": 803,
 *   "main": "Clouds",
 *   "description": "broken clouds",
 *   "icon": "04n"
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String,
)

/**
 * Weather overview data, currently used for API key validation only.
 *
 * Sample JSON:
 * ```json
 * {
 *   "lat": 51.509865,
 *   "lon": -11.8092,
 *   "tz": "-01:00",
 *   "date": "2025-01-10",
 *   "units": "standard",
 *   "weather_overview": "The current weather is overcast with a temperature of 282K and a real feel of 276K."
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class WeatherOverview(
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longitude: Double,
    @Json(name = "tz") val timezone: String,
    @Json(name = "date") val date: String,
    @Json(name = "units") val units: String,
    @Json(name = "weather_overview") val weatherOverview: String,
)

/**
 * Possible error response from the API.
 *
 * Sample JSON response:
 * ```json
 * {
 *   "cod": 429,
 *   "message": "Your account is temporary blocked due to exceeding of requests limitation of your subscription type. Please choose the proper subscription https://openweathermap.org/price"
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val cod: Int?,
    val code: Int?,
    val message: String,
)
