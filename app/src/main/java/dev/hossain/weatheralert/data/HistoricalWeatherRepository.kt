package dev.hossain.weatheralert.data

import dev.hossain.weatheralert.datamodel.HistoricalWeather
import dev.hossain.weatheralert.db.HistoricalWeatherDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import dev.hossain.weatheralert.db.HistoricalWeatherEntity as DbEntity

interface HistoricalWeatherRepository {
    suspend fun insertHistoricalWeather(entity: HistoricalWeather)

    fun getHistoryForCity(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<List<HistoricalWeather>>

    fun getAverageSnow(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<Double?>

    fun getAverageRain(
        cityId: Long,
        start: Long,
        end: Long,
    ): Flow<Double?>
}

class HistoricalWeatherRepositoryImpl
    @Inject
    constructor(
        private val dao: HistoricalWeatherDao,
    ) : HistoricalWeatherRepository {
        override suspend fun insertHistoricalWeather(entity: HistoricalWeather) {
            dao.insert(entity.toDbEntity())
        }

        override fun getHistoryForCity(
            cityId: Long,
            start: Long,
            end: Long,
        ): Flow<List<HistoricalWeather>> = dao.getHistoryForCity(cityId, start, end).map { list -> list.map { it.toModel() } }

        override fun getAverageSnow(
            cityId: Long,
            start: Long,
            end: Long,
        ): Flow<Double?> = dao.getAverageSnow(cityId, start, end)

        override fun getAverageRain(
            cityId: Long,
            start: Long,
            end: Long,
        ): Flow<Double?> = dao.getAverageRain(cityId, start, end)
    }

private fun HistoricalWeather.toDbEntity() =
    DbEntity(
        id = id,
        cityId = cityId,
        date = date,
        latitude = latitude,
        longitude = longitude,
        snow = snow,
        rain = rain,
        forecastSourceService = forecastSourceService,
        hourlyPrecipitation = hourlyPrecipitation,
    )

private fun DbEntity.toModel() =
    HistoricalWeather(
        id = id,
        cityId = cityId,
        date = date,
        latitude = latitude,
        longitude = longitude,
        snow = snow,
        rain = rain,
        forecastSourceService = forecastSourceService,
        hourlyPrecipitation = hourlyPrecipitation,
    )
