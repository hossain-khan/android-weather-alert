package dev.hossain.weatheralert.di

import android.content.Context
import androidx.room.Room
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.db.AlertDao
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.CityDao
import dev.hossain.weatheralert.db.CityForecastDao

@Module
@ContributesTo(AppScope::class)
object DatabaseModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "app.db")
            .createFromAsset("alertapp.db")
            // ⚠️ UPDATE: Disabled destructive migration for now.
            // To ensure user data is not lost, we need to handle migration properly.
            // .fallbackToDestructiveMigrationFrom(3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()

    @Provides
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideCityForecastDao(database: AppDatabase): CityForecastDao = database.forecastDao()
}
