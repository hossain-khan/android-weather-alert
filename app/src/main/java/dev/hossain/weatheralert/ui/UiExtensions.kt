package dev.hossain.weatheralert.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hossain.weatheralert.R
import dev.hossain.weatheralert.data.WeatherService

/**
 * Internal config to show logo with right sizing and description for each weather service.
 */
internal data class WeatherServiceLogoConfig(
    @DrawableRes val logoResId: Int,
    val logoWidth: Dp,
    val logoHeight: Dp,
    val description: String,
)

internal fun WeatherService.serviceConfig(): WeatherServiceLogoConfig =
    when (this) {
        WeatherService.OPEN_WEATHER_MAP ->
            WeatherServiceLogoConfig(
                logoResId = R.drawable.openweather_logo,
                logoWidth = 100.dp,
                logoHeight = 50.dp,
                description = "Free API service with larger usage limits. However, requires credit card to activate free API subscription.",
            )
        WeatherService.TOMORROW_IO ->
            WeatherServiceLogoConfig(
                logoResId = R.drawable.tomorrow_io_logo,
                logoWidth = 120.dp,
                logoHeight = 30.dp,
                description = "Free API service with accurate data but limited usage limits. No credit card required.",
            )
    }
