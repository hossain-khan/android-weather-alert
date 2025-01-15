package com.openmeteo.api

import com.openmeteo.api.OpenMeteo.Contexts
import com.openmeteo.api.common.Response
import com.openmeteo.api.common.time.Timezone
import com.openmeteo.api.common.units.PrecipitationUnit
import com.openmeteo.api.common.units.TemperatureUnit

@OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
internal fun main() {
    println("Hello, Open Meteo!")
    val om = OpenMeteo(43.9319f, -78.851f, null, contexts = Contexts())
    val forecast =
        om
            .forecast {
                daily =
                    Forecast.Daily {
                        listOf(temperature2mMin, temperature2mMax)
                    }
                temperatureUnit = TemperatureUnit.Fahrenheit
                timezone = Timezone.auto
            }.getOrThrow()

    println("Got Forecast: $forecast")

    Forecast.Daily.run {
        forecast.daily.getValue(temperature2mMax).run {
            println("# $temperature2mMax ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
    }

    checkForApp()
}

@OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
private fun checkForApp() {
    val om =
        OpenMeteo(
            latitude = 43.9319f,
            longitude = -78.851f,
            apikey = null,
            contexts = Contexts(),
        )

    val forecast: Forecast.Response =
        om
            .forecast {
                hourly =
                    Forecast.Hourly {
                        listOf(snowfall, snowDepth, rain)
                    }
                daily =
                    Forecast.Daily {
                        listOf(snowfallSum, rainSum)
                    }
                forecastDays = 3
                temperatureUnit = TemperatureUnit.Celsius
                precipitationUnit = PrecipitationUnit.Millimeters
                timezone = Timezone.auto
            }.getOrThrow()

    Forecast.Daily.run {
        forecast.daily.getValue(snowfallSum).run {
            println("# $snowfallSum ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.daily.getValue(rainSum).run {
            println("# $rainSum ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
    }
    Forecast.Hourly.run {
        forecast.hourly.getValue(snowfall).run {
            println("# $snowfall ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.hourly.getValue(snowDepth).run {
            println("# $snowDepth ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.hourly.getValue(rain).run {
            println("# $rain ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
    }
}
