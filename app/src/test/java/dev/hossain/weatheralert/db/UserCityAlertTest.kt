package dev.hossain.weatheralert.db

import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.WeatherAlertCategory
import dev.hossain.weatheralert.datamodel.WeatherForecastService
import org.junit.Test
import java.time.Instant

class UserCityAlertTest {
    @Test
    fun testLatestCityForecast_manyForecasts() {
        val cityForecast1 =
            CityForecast(
                forecastId = 1,
                cityId = 1,
                alertId = 1,
                latitude = 23.8103,
                longitude = 90.4125,
                dailyCumulativeSnow = 5.0,
                nextDaySnow = 2.0,
                dailyCumulativeRain = 10.0,
                nextDayRain = 3.0,
                forecastSourceService = WeatherForecastService.OPEN_WEATHER_MAP,
                createdAt = Instant.parse("2025-01-15T21:42:00Z").toEpochMilli(),
                hourlyPrecipitation = emptyList(),
            )
        val cityForecast2 =
            CityForecast(
                forecastId = 2,
                cityId = 1,
                alertId = 1,
                latitude = 23.8103,
                longitude = 90.4125,
                dailyCumulativeSnow = 6.0,
                nextDaySnow = 3.0,
                dailyCumulativeRain = 12.0,
                nextDayRain = 4.0,
                forecastSourceService = WeatherForecastService.OPEN_WEATHER_MAP,
                createdAt = Instant.parse("2025-01-16T21:42:00Z").toEpochMilli(),
                hourlyPrecipitation = emptyList(),
            )
        val cityForecast3 =
            CityForecast(
                forecastId = 3,
                cityId = 1,
                alertId = 1,
                latitude = 23.8103,
                longitude = 90.4125,
                dailyCumulativeSnow = 4.0,
                nextDaySnow = 1.0,
                dailyCumulativeRain = 8.0,
                nextDayRain = 2.0,
                forecastSourceService = WeatherForecastService.OPEN_WEATHER_MAP,
                createdAt = Instant.parse("2025-01-14T21:42:00Z").toEpochMilli(),
                hourlyPrecipitation = emptyList(),
            )

        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 1, cityId = 1, alertCategory = WeatherAlertCategory.SNOW_FALL, threshold = 5.0f),
                city =
                    City(
                        city = "Test City",
                        cityName = "Test City",
                        lat = 23.8103,
                        lng = 90.4125,
                        country = "Bangladesh",
                        iso2 = "BD",
                        iso3 = "BGD",
                        provStateName = "Dhaka",
                        capital = "Dhaka",
                        population = 8906039,
                        id = 1,
                    ),
                cityForecasts = listOf(cityForecast1, cityForecast2, cityForecast3),
            )

        val latestForecast = userCityAlert.latestCityForecast()
        assertThat(latestForecast).isEqualTo(cityForecast2)
    }

    @Test
    fun testLatestCityForecast_singleForecast() {
        val cityForecast =
            CityForecast(
                forecastId = 1,
                cityId = 1,
                alertId = 1,
                latitude = 23.8103,
                longitude = 90.4125,
                dailyCumulativeSnow = 5.0,
                nextDaySnow = 2.0,
                dailyCumulativeRain = 10.0,
                nextDayRain = 3.0,
                forecastSourceService = WeatherForecastService.OPEN_WEATHER_MAP,
                createdAt = Instant.parse("2025-01-15T21:42:00Z").toEpochMilli(),
                hourlyPrecipitation = emptyList(),
            )

        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 1, cityId = 1, alertCategory = WeatherAlertCategory.RAIN_FALL, threshold = 5.0f),
                city =
                    City(
                        city = "Test City",
                        cityName = "Test City",
                        lat = 23.8103,
                        lng = 90.4125,
                        country = "Bangladesh",
                        iso2 = "BD",
                        iso3 = "BGD",
                        provStateName = "Dhaka",
                        capital = "Dhaka",
                        population = 8906039,
                        id = 1,
                    ),
                cityForecasts = listOf(cityForecast),
            )

        val latestForecast = userCityAlert.latestCityForecast()
        assertThat(latestForecast).isEqualTo(cityForecast)
    }

    @Test
    fun testLatestCityForecast_emptyList() {
        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 1, cityId = 1, alertCategory = WeatherAlertCategory.SNOW_FALL, threshold = 5.0f),
                city =
                    City(
                        city = "Test City",
                        cityName = "Test City",
                        lat = 23.8103,
                        lng = 90.4125,
                        country = "Bangladesh",
                        iso2 = "BD",
                        iso3 = "BGD",
                        provStateName = "Dhaka",
                        capital = "Dhaka",
                        population = 8906039,
                        id = 1,
                    ),
                cityForecasts = emptyList(),
            )

        val latestForecast = userCityAlert.latestCityForecast()
        assertThat(latestForecast).isNull()
    }

    @Test
    fun toNotificationTag_correctFormat() {
        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 1, cityId = 1, alertCategory = WeatherAlertCategory.SNOW_FALL, threshold = 5.0f),
                city =
                    City(
                        city = "Test City",
                        cityName = "Test City",
                        lat = 23.8103,
                        lng = 90.4125,
                        country = "Bangladesh",
                        iso2 = "BD",
                        iso3 = "BGD",
                        provStateName = "Dhaka",
                        capital = "Dhaka",
                        population = 8906039,
                        id = 1,
                    ),
                cityForecasts = emptyList(),
            )

        val notificationTag = userCityAlert.toNotificationTag()
        assertThat(notificationTag).isEqualTo("1_1_SNOW_FALL")
    }

    @Test
    fun toNotificationTag_differentAlertCategory() {
        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 2, cityId = 2, alertCategory = WeatherAlertCategory.RAIN_FALL, threshold = 10.0f),
                city =
                    City(
                        city = "Another City",
                        cityName = "Another City",
                        lat = 40.7128,
                        lng = -74.0060,
                        country = "USA",
                        iso2 = "US",
                        iso3 = "USA",
                        provStateName = "New York",
                        capital = "Albany",
                        population = 8419600,
                        id = 2,
                    ),
                cityForecasts = emptyList(),
            )

        val notificationTag = userCityAlert.toNotificationTag()
        assertThat(notificationTag).isEqualTo("2_2_RAIN_FALL")
    }

    @Test
    fun toNotificationTag_handlesSpecialCharacters() {
        val userCityAlert =
            UserCityAlert(
                alert = Alert(id = 3, cityId = 3, alertCategory = WeatherAlertCategory.SNOW_FALL, threshold = 15.0f),
                city =
                    City(
                        city = "City@123",
                        cityName = "City@123",
                        lat = 34.0522,
                        lng = -118.2437,
                        country = "USA",
                        iso2 = "US",
                        iso3 = "USA",
                        provStateName = "California",
                        capital = "Sacramento",
                        population = 3970000,
                        id = 3,
                    ),
                cityForecasts = emptyList(),
            )

        val notificationTag = userCityAlert.toNotificationTag()
        assertThat(notificationTag).isEqualTo("3_3_SNOW_FALL")
    }
}
