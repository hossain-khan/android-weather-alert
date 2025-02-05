package com.weatherapi.api.model

import dev.hossain.weatheralert.datamodel.AppForecastData
import dev.hossain.weatheralert.datamodel.Rain
import dev.hossain.weatheralert.datamodel.Snow

internal fun ForecastWeatherResponse.toForecastData(): AppForecastData =
    AppForecastData(
        latitude = location.lat,
        longitude = location.lon,
        snow = Snow(),
        rain = Rain(),
        hourlyPrecipitation = emptyList(),
    )
