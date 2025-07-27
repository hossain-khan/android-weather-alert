package dev.hossain.weatheralert.di

import android.content.Context
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.BindsInstance
import dagger.Component
import dev.hossain.weatheralert.data.WeatherRepositoryTest
import dev.hossain.weatheralert.work.WeatherCheckWorkerTest

@SingleIn(AppScope::class)
@Component(modules = [NetworkModule::class, TestModule::class, TestDatabaseModule::class])
interface TestAppComponent {
    fun inject(test: WeatherRepositoryTest)

    fun inject(test: WeatherCheckWorkerTest)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
        ): TestAppComponent
    }
}
