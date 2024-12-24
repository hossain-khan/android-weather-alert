package app.example.di

import app.example.data.ExampleEmailRepository
import com.squareup.anvil.annotations.ContributesTo
import dagger.Provides

// Example of a Dagger module that provides dependencies for the app.
// You should delete this file and create your own modules.
@ContributesTo(AppScope::class)
@dagger.Module
class ExampleAppModule {
    @Provides
    fun provideEmailRepository(): ExampleEmailRepository = ExampleEmailRepository()
}
