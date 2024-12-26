package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.weatherAlertDataStore by preferencesDataStore(name = "weather_alerts")

object WeatherAlertKeys {
    val SNOW_ALERT = stringPreferencesKey("snow_alert")
    val RAIN_ALERT = stringPreferencesKey("rain_alert")
}