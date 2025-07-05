package dev.hossain.weatheralert.di

import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.data.HistoricalWeatherRepository
import dev.hossain.weatheralert.data.HistoricalWeatherRepositoryImpl
import dev.hossain.weatheralert.db.HistoricalWeatherDao

@Module
@ContributesTo(AppScope::class)
object HistoricalWeatherRepositoryModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideHistoricalWeatherRepository(dao: HistoricalWeatherDao): HistoricalWeatherRepository = HistoricalWeatherRepositoryImpl(dao)
}
