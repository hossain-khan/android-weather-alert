package dev.hossain.weatheralert.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dev.hossain.weatheralert.data.WeatherRepositoryTest
import dev.hossain.weatheralert.work.WeatherCheckWorkerTest
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, TestModule::class])
interface TestAppComponent {
    fun inject(test: WeatherRepositoryTest)

    fun inject(test: WeatherCheckWorkerTest)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance @ApplicationContext context: Context,
        ): TestAppComponent
    }
}
