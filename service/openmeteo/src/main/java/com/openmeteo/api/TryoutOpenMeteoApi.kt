package com.openmeteo.api

import com.openmeteo.api.OpenMeteo.Contexts
import com.openmeteo.api.common.Response
import com.openmeteo.api.common.time.Timezone
import com.openmeteo.api.common.units.PrecipitationUnit
import com.openmeteo.api.common.units.TemperatureUnit

data class Coordinates(
    val latitude: Float,
    val longitude: Float,
)

val testCoordinates: Map<String, Coordinates> =
    mapOf(
        "Grande Prairie" to Coordinates(55.126602f, -118.881424f),
        "Toronto" to Coordinates(43.9319f, -78.851f),
        "Edmonton" to Coordinates(53.526226f, -113.82236f),
        "São Luís" to Coordinates(-2.7408473f, -44.273506f),
        "Santa Bárbara" to Coordinates(4.193481f, -74.74811f),
    )

/**
 * Main function to demonstrate the usage of the OpenMeteo API.
 */
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

/**
 * Function to check the weather forecast for the application - if things are working properly.
 */
@OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
private fun checkForApp() {
    val om =
        OpenMeteo(
            // Uses toronto latitude and longitude
            latitude = testCoordinates["Grande Prairie"]!!.latitude,
            longitude = testCoordinates["Grande Prairie"]!!.longitude,
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
                forecastDays = 7
                temperatureUnit = TemperatureUnit.Celsius
                precipitationUnit = PrecipitationUnit.Millimeters
                timezone = Timezone.auto
            }.getOrThrow()

    Forecast.Daily.run {
        forecast.daily.getValue(snowfallSum).run {
            println("\n\n##### $snowfallSum ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.daily.getValue(rainSum).run {
            println("\n\n##### $rainSum ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
    }
    Forecast.Hourly.run {
        forecast.hourly.getValue(snowfall).run {
            println("\n\n##### $snowfall ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.hourly.getValue(snowDepth).run {
            println("\n\n##### $snowDepth ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
        forecast.hourly.getValue(rain).run {
            println("\n\n##### $rain ($unit)")
            values.forEach { (t, v) -> println("> $t | $v") }
        }
    }
}
