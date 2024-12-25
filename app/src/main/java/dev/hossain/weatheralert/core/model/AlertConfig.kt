package dev.hossain.weatheralert.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AlertCategory : Parcelable {
    @Parcelize
    object Snow : AlertCategory()
    @Parcelize
    object Rain : AlertCategory()
}

@Parcelize
data class AlertConfig(
    val category: AlertCategory,
    val threshold: Double // e.g., 5.0 for snow (cm), 10.0 for rain (mm)
) : Parcelable