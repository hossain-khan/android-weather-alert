package dev.hossain.weatheralert.di

import android.content.Context
import androidx.room.Room
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.CityForecastDao

object TestDatabaseModule {
    fun provideInMemoryDatabase(context: Context): AppDatabase =
        Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()

    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    fun provideCityForecastDao(database: AppDatabase): CityForecastDao = database.forecastDao()
}
