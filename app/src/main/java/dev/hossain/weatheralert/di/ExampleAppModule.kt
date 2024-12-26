package dev.hossain.weatheralert.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides
import dev.hossain.weatheralert.data.ExampleEmailRepository

// Example of a Dagger module that provides dependencies for the app.
// You should delete this file and create your own modules.
@ContributesTo(AppScope::class)
@dagger.Module
class ExampleAppModule {
    @Provides
    fun provideEmailRepository(): ExampleEmailRepository = ExampleEmailRepository()
}
