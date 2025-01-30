package dev.hossain.weatheralert.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.weatheralert.datamodel.HourlyPrecipitation

class Converters {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(List::class.java, HourlyPrecipitation::class.java)
    private val adapter = moshi.adapter<List<HourlyPrecipitation>>(type)

    @TypeConverter
    fun fromHourlyPrecipitationList(value: List<HourlyPrecipitation>): String = adapter.toJson(value)

    @TypeConverter
    fun toHourlyPrecipitationList(value: String): List<HourlyPrecipitation> = adapter.fromJson(value) ?: emptyList()
}
