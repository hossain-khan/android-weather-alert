package dev.hossain.weatheralert.util

import java.util.Locale

/**
 * Formats 2.440923834343 to 2.44 cm
 */
internal fun Float.formatUnit(unit: String): String = "%.2f %s".format(Locale.getDefault(), this, unit)

/**
 * Formats 8.98237320 to 8.98 mm
 */
internal fun Double.formatUnit(unit: String): String = "%.2f %s".format(Locale.getDefault(), this, unit)
