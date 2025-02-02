package dev.hossain.weatheralert.datamodel

import androidx.annotation.Keep

@Keep
enum class WeatherAlertCategory(
    val label: String,
    /**
     * 🛑 Got a great way to keep unit label. Ideally should allow user to configure it. ⚠️
     * - https://github.com/hossain-khan/android-weather-alert/issues/60
     */
    val unit: String,
) {
    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     * - https://docs.tomorrow.io/reference/data-layers-core
     */
    SNOW_FALL("Snow", "mm"),

    /**
     * Precipitation volume	standard=mm, imperial=mm and metric=mm
     * - https://openweathermap.org/weather-data
     * - https://docs.tomorrow.io/reference/data-layers-core
     */
    RAIN_FALL("Rain", "mm"),
}
