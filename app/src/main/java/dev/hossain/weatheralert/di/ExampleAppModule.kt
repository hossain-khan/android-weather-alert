package dev.hossain.weatheralert.di

import android.content.Context
import dev.hossain.weatheralert.data.ExampleEmailRepository
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Provides

// Example of a Dagger module that provides dependencies for the app.
// You should delete this file and create your own modules.
@ContributesTo(AppScope::class)
@dagger.Module
class ExampleAppModule {
    @Provides
    fun provideEmailRepository(): ExampleEmailRepository = ExampleEmailRepository()
}
