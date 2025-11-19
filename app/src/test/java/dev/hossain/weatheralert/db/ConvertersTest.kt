package dev.hossain.weatheralert.db

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.db.converter.Converters
import org.junit.Before
import org.junit.Test

class ConvertersTest {
    private lateinit var converters: Converters
    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        converters = Converters()
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testFromHourlyPrecipitationList() {
        val hourlyPrecipitationList =
            listOf(
                HourlyPrecipitation("2025-01-15T21:42:00Z", 5.0, 2.0),
                HourlyPrecipitation("2025-01-15T22:42:00Z", 3.0, 1.0),
            )
        val json = converters.fromHourlyPrecipitationList(hourlyPrecipitationList)
        // language=JSON
        val expectedJson = """[{"isoDateTime":"2025-01-15T21:42:00Z","rain":5.0,"snow":2.0},{"isoDateTime":"2025-01-15T22:42:00Z","rain":3.0,"snow":1.0}]"""
        assertThat(json).isEqualTo(expectedJson)
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testToHourlyPrecipitationList() {
        // language=JSON
        val json = """[{"isoDateTime":"2025-01-15T21:42:00Z","rain":5.0,"snow":2.0},{"isoDateTime":"2025-01-15T22:42:00Z","rain":3.0,"snow":1.0}]"""
        val hourlyPrecipitationList = converters.toHourlyPrecipitationList(json)
        val expectedList =
            listOf(
                HourlyPrecipitation("2025-01-15T21:42:00Z", 5.0, 2.0),
                HourlyPrecipitation("2025-01-15T22:42:00Z", 3.0, 1.0),
            )
        assertThat(hourlyPrecipitationList).isEqualTo(expectedList)
    }

    @Test
    fun testToEmptyHourlyPrecipitationList() {
        val json = "[]"
        val hourlyPrecipitationList = converters.toHourlyPrecipitationList(json)
        val expectedList = emptyList<HourlyPrecipitation>()
        assertThat(hourlyPrecipitationList).isEqualTo(expectedList)
    }
}
