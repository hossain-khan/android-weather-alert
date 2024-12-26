package dev.hossain.weatheralert.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dev.hossain.weatheralert.data.api.OpenWeatherMapService
import dev.hossain.weatheralert.data.model.WeatherForecast
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class WeatherRepository @Inject constructor(
    private val api: OpenWeatherMapService,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) {
    suspend fun getTomorrowsForecast(): WeatherForecast {
        val location = getCurrentLocation()

        return withContext(Dispatchers.IO) {
            val response = api.getForecast(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY
            )

            // Parse and convert the response to our domain model
            response.list
                .filter { it.dt.isTomorrow() }
                .maxByOrNull { it.dt }
                ?.let { forecast ->
                    WeatherForecast(
                        temperature = forecast.main.temp,
                        snowfall = forecast.snow?.amount,
                        rainfall = forecast.rain?.amount,
                        timestamp = forecast.dt
                    )
                } ?: throw WeatherDataNotFoundException()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location = suspendCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resumeWithException(LocationPermissionException())
            return@suspendCoroutine
        }

        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        locationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    continuation.resume(it)
                } ?: continuation.resumeWithException(LocationNotFoundException())
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}