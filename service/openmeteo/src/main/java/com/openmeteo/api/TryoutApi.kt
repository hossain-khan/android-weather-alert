package com.openmeteo.api

import com.openmeteo.api.OpenMeteo.Contexts
import com.openmeteo.api.common.Response
import com.openmeteo.api.common.time.Timezone
import com.openmeteo.api.common.units.TemperatureUnit

@OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
fun main() {
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
}
