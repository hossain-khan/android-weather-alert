package dev.hossain.weatheralert.simulation

import android.content.Context
import dev.hossain.weatheralert.R
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.io.InputStreamReader

/**
 * An OkHttp Interceptor that simulates responses for specific API requests by providing
 * JSON responses from raw resources based on the request URL.
 */
class SimulatedResponseInterceptor(
    private val context: Context,
) : Interceptor {
    /**
     * Intercepts the HTTP request and provides a simulated response if the request URL matches
     * one of the predefined patterns. Otherwise, proceeds with the actual request.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Determine the appropriate response based on the request URL.
        val responseString =
            when {
                url.contains("api.openweathermap.org") -> readRawResource(R.raw.simulated_forecast_response)
                url.contains("api.tomorrow.io") -> readRawResource(R.raw.simulated_forecast_response)
                url.contains("api.weatherapi.com") -> readRawResource(R.raw.simulated_forecast_response)
                else -> null
            }

        // If a simulated response is found, return it. Otherwise, proceed with the actual request.
        return if (responseString != null) {
            Timber.i("🤖 Sending simulated response for: $url")
            Response
                .Builder()
                .request(request)
                .protocol(chain.connection()?.protocol() ?: okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        } else {
            chain.proceed(request)
        }
    }

    /**
     * Reads the content of a raw resource file and returns it as a string.
     */
    private fun readRawResource(resourceId: Int): String {
        val inputStream = context.resources.openRawResource(resourceId)
        return InputStreamReader(inputStream).use { it.readText() }
    }
}
