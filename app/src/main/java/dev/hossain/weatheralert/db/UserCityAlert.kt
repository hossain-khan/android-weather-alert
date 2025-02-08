package dev.hossain.weatheralert.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a user's city with alert.
 *
 * - https://developer.android.com/training/data-storage/room/relationships/one-to-many
 *
 * @see City
 * @see Alert
 */
data class UserCityAlert(
    @Embedded val alert: Alert,
    @Relation(
        parentColumn = "city_id",
        entityColumn = "id",
    )
    val city: City,
    /**
     * NOTE: Currently it's a list, because everytime there is a refresh
     * a new city forecast is added, old one is not deleted at the moment.
     * Only the latest city forecast data should be used.
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "alert_id",
    )
    val cityForecasts: List<CityForecast>,
) {
    fun latestCityForecast(): CityForecast? {
        // NOTE: Currently it's a list, because everytime there is a refresh
        // a new city forecast is added, old one is not deleted at the moment.
        // Only the latest city forecast data is used.
        return cityForecasts.maxByOrNull { it.createdAt }
    }

    fun toNotificationTag(): String = "${city.id}_${alert.id}_${alert.alertCategory.name}"
}
