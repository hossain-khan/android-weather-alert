package dev.hossain.weatheralert.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dev.hossain.weatheralert.data.WeatherRepositoryTest
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface TestAppComponent {
    fun inject(test: WeatherRepositoryTest)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @ApplicationContext context: Context,
        ): TestAppComponent
    }
}
