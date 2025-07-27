package dev.hossain.weatheralert.di

import android.content.Context
import androidx.room.Room
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.CityForecastDao

@Module
@ContributesTo(AppScope::class)
object TestDatabaseModule {
    @Provides
    fun provideInMemoryDatabase(
        context: Context,
    ): AppDatabase =
        Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()

    @Provides
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideCityForecastDao(database: AppDatabase): CityForecastDao = database.forecastDao()
}
