package dev.hossain.weatheralert.di

import android.content.Context
import androidx.room.Room
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.CityForecastDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object DatabaseBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "app.db")
            .createFromAsset("alertapp.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()

    @Provides
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideCityForecastDao(database: AppDatabase): CityForecastDao = database.forecastDao()
}
