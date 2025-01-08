package dev.hossain.weatheralert.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a user's city with alert.
 *
 * @see City
 * @see Alert
 */
data class UserCityAlert(
    @Embedded val city: City,
    @Relation(
        parentColumn = "id",
        entityColumn = "cityId",
    )
    val alert: Alert,
) {
    fun toNotificationTag(): String = "${city.id}_${alert.id}_${alert.alertCategory.name}"
}
