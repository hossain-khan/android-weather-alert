package dev.hossain.weatheralert.domain.model

import java.util.UUID

data class AlertConfig(
    val id: String = UUID.randomUUID().toString(),
    val type: AlertType,
    val threshold: Float,
    val unit: String
)

enum class AlertType {
    SNOW_FALL,
    RAIN_FALL
}