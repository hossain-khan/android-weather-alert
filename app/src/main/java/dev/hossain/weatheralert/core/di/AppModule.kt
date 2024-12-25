package dev.hossain.weatheralert.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.hossain.weatheralert.core.data.AlertConfigDataStore
import dev.hossain.weatheralert.core.network.OpenWeatherMapApi
import dev.hossain.weatheralert.core.network.WeatherRepository
import dev.hossain.weatheralert.feature.alerts.AlertListPresenter
import dev.hossain.weatheralert.feature.alerts.AlertListUiFactory
import dev.hossain.weatheralert.feature.settings.SettingsPresenter
import dev.hossain.weatheralert.feature.settings.SettingsUiFactory
import com.google.gson.Gson
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val USER_PREFERENCES = "user_preferences"

@Module
@ContributesTo(AppScope::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenWeatherMapApi(retrofit: Retrofit): OpenWeatherMapApi {
        return retrofit.create(OpenWeatherMapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(api: OpenWeatherMapApi): WeatherRepository {
        return WeatherRepository(api)
    }

    @Singleton
    @Provides
    fun providesDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Provides
    @Singleton
    fun provideAlertConfigDataStore(dataStore: DataStore<Preferences>): AlertConfigDataStore {
        return AlertConfigDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideCircuit(
        alertListPresenterFactory: AlertListPresenter.Factory,
        alertListUiFactory: AlertListUiFactory,
        settingsPresenterFactory: SettingsPresenter.Factory,
        settingsUiFactory: SettingsUiFactory
    ): Circuit {
        return Circuit.Builder()
            .addPresenterFactory(alertListPresenterFactory)
            .addUiFactory(alertListUiFactory)
            .addPresenterFactory(settingsPresenterFactory)
            .addUiFactory(settingsUiFactory)
            .build()
    }
}