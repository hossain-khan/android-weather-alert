package dev.hossain.weatheralert.datamodel

import androidx.annotation.Keep

@Keep
enum class WeatherAlertCategory(
    val label: String,
    /**
     * 🛑 THIS IS A BIG MESS. Fix it in the app. ⚠️
     * - https://github.com/hossain-khan/android-weather-alert/issues/60
     */
    val unit: String,
) {
    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     */
    SNOW_FALL("Snow", "mm"),

    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     */
    RAIN_FALL("Rain", "mm"),
}
