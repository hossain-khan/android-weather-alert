package com.weatherapi.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.WeatherApiServiceResponse

/**
 * Root response for WeatherAPI forecast request.
 */
@JsonClass(generateAdapter = true)
data class ForecastWeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast,
) : WeatherApiServiceResponse {
    override fun convertToForecastData(): AppForecastData = this.toForecastData()
}

@JsonClass(generateAdapter = true)
data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    @Json(name = "tz_id") val tzId: String,
    @Json(name = "localtime_epoch") val localtimeEpoch: Long,
    @Json(name = "localtime") val localtime: String,
)

@JsonClass(generateAdapter = true)
data class Current(
    @Json(name = "last_updated_epoch") val lastUpdatedEpoch: Long,
    @Json(name = "last_updated") val lastUpdated: String,
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "temp_f") val tempF: Double,
    @Json(name = "is_day") val isDay: Int,
    val condition: Condition,
    @Json(name = "wind_mph") val windMph: Double,
    @Json(name = "wind_kph") val windKph: Double,
    @Json(name = "wind_degree") val windDegree: Int,
    @Json(name = "wind_dir") val windDir: String,
    @Json(name = "pressure_mb") val pressureMb: Double,
    @Json(name = "pressure_in") val pressureIn: Double,
    @Json(name = "precip_mm") val precipMm: Double,
    @Json(name = "precip_in") val precipIn: Double,
    val humidity: Int,
    val cloud: Int,
    @Json(name = "feelslike_c") val feelsLikeC: Double,
    @Json(name = "feelslike_f") val feelsLikeF: Double,
    @Json(name = "windchill_c") val windChillC: Double,
    @Json(name = "windchill_f") val windChillF: Double,
    @Json(name = "heatindex_c") val heatIndexC: Double,
    @Json(name = "heatindex_f") val heatIndexF: Double,
    @Json(name = "dewpoint_c") val dewPointC: Double,
    @Json(name = "dewpoint_f") val dewPointF: Double,
    @Json(name = "vis_km") val visKm: Double,
    @Json(name = "vis_miles") val visMiles: Double,
    val uv: Double,
    @Json(name = "gust_mph") val gustMph: Double,
    @Json(name = "gust_kph") val gustKph: Double,
)

@JsonClass(generateAdapter = true)
data class Condition(
    val text: String,
    val icon: String,
    val code: Int,
)

@JsonClass(generateAdapter = true)
data class Forecast(
    @Json(name = "forecastday")
    val forecastDay: List<ForecastDay>,
)

@JsonClass(generateAdapter = true)
data class ForecastDay(
    val date: String,
    @Json(name = "date_epoch") val dateEpoch: Long,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>,
)

@JsonClass(generateAdapter = true)
data class Day(
    @Json(name = "maxtemp_c") val maxTempC: Double,
    @Json(name = "maxtemp_f") val maxTempF: Double,
    @Json(name = "mintemp_c") val minTempC: Double,
    @Json(name = "mintemp_f") val minTempF: Double,
    @Json(name = "avgtemp_c") val avgTempC: Double,
    @Json(name = "avgtemp_f") val avgTempF: Double,
    @Json(name = "maxwind_mph") val maxWindMph: Double,
    @Json(name = "maxwind_kph") val maxWindKph: Double,
    @Json(name = "totalprecip_mm") val totalPrecipMm: Double,
    @Json(name = "totalprecip_in") val totalPrecipIn: Double,
    @Json(name = "totalsnow_cm") val totalSnowCm: Double,
    @Json(name = "avgvis_km") val avgVisKm: Double,
    @Json(name = "avgvis_miles") val avgVisMiles: Double,
    @Json(name = "avghumidity") val avgHumidity: Int,
    @Json(name = "daily_will_it_rain") val dailyWillItRain: Int,
    @Json(name = "daily_chance_of_rain") val dailyChanceOfRain: Int,
    @Json(name = "daily_will_it_snow") val dailyWillItSnow: Int,
    @Json(name = "daily_chance_of_snow") val dailyChanceOfSnow: Int,
    val condition: Condition,
    val uv: Double,
)

@JsonClass(generateAdapter = true)
data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    @Json(name = "moon_phase") val moonPhase: String,
    @Json(name = "moon_illumination") val moonIllumination: Int,
    @Json(name = "is_moon_up") val isMoonUp: Int,
    @Json(name = "is_sun_up") val isSunUp: Int,
)

@JsonClass(generateAdapter = true)
data class Hour(
    @Json(name = "time_epoch") val timeEpoch: Long,
    val time: String,
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "temp_f") val tempF: Double,
    @Json(name = "is_day") val isDay: Int,
    val condition: Condition,
    @Json(name = "wind_mph") val windMph: Double,
    @Json(name = "wind_kph") val windKph: Double,
    @Json(name = "wind_degree") val windDegree: Int,
    @Json(name = "wind_dir") val windDir: String,
    @Json(name = "pressure_mb") val pressureMb: Double,
    @Json(name = "pressure_in") val pressureIn: Double,
    @Json(name = "precip_mm") val precipMm: Double,
    @Json(name = "precip_in") val precipIn: Double,
    @Json(name = "snow_cm") val snowCm: Double,
    val humidity: Int,
    val cloud: Int,
    @Json(name = "feelslike_c") val feelsLikeC: Double,
    @Json(name = "feelslike_f") val feelsLikeF: Double,
    @Json(name = "windchill_c") val windChillC: Double,
    @Json(name = "windchill_f") val windChillF: Double,
    @Json(name = "heatindex_c") val heatIndexC: Double,
    @Json(name = "heatindex_f") val heatIndexF: Double,
    @Json(name = "dewpoint_c") val dewPointC: Double,
    @Json(name = "dewpoint_f") val dewPointF: Double,
    @Json(name = "will_it_rain") val willItRain: Int,
    @Json(name = "chance_of_rain") val chanceOfRain: Int,
    @Json(name = "will_it_snow") val willItSnow: Int,
    @Json(name = "chance_of_snow") val chanceOfSnow: Int,
    @Json(name = "vis_km") val visKm: Double,
    @Json(name = "vis_miles") val visMiles: Double,
    @Json(name = "gust_mph") val gustMph: Double,
    @Json(name = "gust_kph") val gustKph: Double,
    val uv: Double,
)
