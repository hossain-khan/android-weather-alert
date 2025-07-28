package dev.hossain.weatheralert.di

import android.content.Context
import dev.hossain.weatheralert.data.WeatherRepositoryTest
import dev.hossain.weatheralert.work.WeatherCheckWorkerTest

interface TestAppComponent {
    fun inject(test: WeatherRepositoryTest)

    fun inject(test: WeatherCheckWorkerTest)

    interface Factory {
        fun create(
            context: Context,
        ): TestAppComponent
    }
}
