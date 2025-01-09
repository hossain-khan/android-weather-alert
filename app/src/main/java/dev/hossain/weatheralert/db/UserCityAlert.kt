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
) {
    fun toNotificationTag(): String = "${city.id}_${alert.id}_${alert.alertCategory.name}"
}
