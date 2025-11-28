package dev.hossain.weatheralert.db

import com.google.common.truth.Truth.assertThat
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation
import dev.hossain.weatheralert.db.converter.Converters
import org.junit.Before
import org.junit.Test

class ConvertersTest {
    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun fromHourlyPrecipitationList_convertsToJson() {
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
    fun toHourlyPrecipitationList_parsesJsonCorrectly() {
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
    fun toHourlyPrecipitationList_handlesEmptyArray() {
        val json = "[]"
        val hourlyPrecipitationList = converters.toHourlyPrecipitationList(json)
        assertThat(hourlyPrecipitationList).isEmpty()
    }

    @Test
    fun fromHourlyPrecipitationList_handlesEmptyList() {
        val emptyList = emptyList<HourlyPrecipitation>()
        val json = converters.fromHourlyPrecipitationList(emptyList)
        assertThat(json).isEqualTo("[]")
    }

    @Test
    fun roundTrip_preservesData() {
        val originalList =
            listOf(
                HourlyPrecipitation("2025-01-15T21:42:00Z", 5.0, 2.0),
                HourlyPrecipitation("2025-01-15T22:42:00Z", 0.0, 0.0),
            )
        val json = converters.fromHourlyPrecipitationList(originalList)
        val convertedList = converters.toHourlyPrecipitationList(json)
        assertThat(convertedList).isEqualTo(originalList)
    }

    @Test
    fun fromHourlyPrecipitationList_handlesSingleItem() {
        val singleItemList = listOf(HourlyPrecipitation("2025-01-15T21:42:00Z", 10.5, 3.2))
        val json = converters.fromHourlyPrecipitationList(singleItemList)
        // language=JSON
        val expectedJson = """[{"isoDateTime":"2025-01-15T21:42:00Z","rain":10.5,"snow":3.2}]"""
        assertThat(json).isEqualTo(expectedJson)
    }
}
