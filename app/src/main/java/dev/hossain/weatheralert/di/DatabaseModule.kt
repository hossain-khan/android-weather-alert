package dev.hossain.weatheralert.di

import android.content.Context
import androidx.room.Room
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.db.AppDatabase
import dev.hossain.weatheralert.db.CityDao

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
            .build()

    @Provides
    fun provideCityDao(database: AppDatabase): CityDao = database.cityDao()
}
