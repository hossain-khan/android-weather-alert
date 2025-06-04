package io.tomorrow.api.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

class TomorrowIoApiErrorResponseTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun loadJsonFromResources(fileName: String): String {
        return TomorrowIoApiErrorResponseTest::class.java.classLoader!!
            .getResourceAsStream(fileName)!!
            .bufferedReader()
            .use { it.readText() }
    }

    @Test
    fun `parses TomorrowIoApiErrorResponse from JSON correctly`() {
        val jsonString = loadJsonFromResources("sample_api_error.json")
        val adapter = moshi.adapter(TomorrowIoApiErrorResponse::class.java)
        val errorResponse = adapter.fromJson(jsonString)

        assertThat(errorResponse).isNotNull()
        errorResponse!! // Ensure response is not null

        assertThat(errorResponse.code).isEqualTo(429001)
        assertThat(errorResponse.type).isEqualTo("Too Many Calls")
        assertThat(errorResponse.message).isEqualTo("The request limit for this resource has been reached for the current rate limit window. Wait and retry the operation, or examine your API request volume.")
    }

    @Test
    fun `parses another TomorrowIoApiErrorResponse sample`() {
        val jsonString = """
        {
          "code": 401001,
          "type": "Invalid Auth",
          "message": "The method requires authentication but it was not presented or is invalid."
        }
        """.trimIndent()
        val adapter = moshi.adapter(TomorrowIoApiErrorResponse::class.java)
        val errorResponse = adapter.fromJson(jsonString)

        assertThat(errorResponse).isNotNull()
        errorResponse!! // Ensure response is not null

        assertThat(errorResponse.code).isEqualTo(401001)
        assertThat(errorResponse.type).isEqualTo("Invalid Auth")
        assertThat(errorResponse.message).isEqualTo("The method requires authentication but it was not presented or is invalid.")
    }
}
