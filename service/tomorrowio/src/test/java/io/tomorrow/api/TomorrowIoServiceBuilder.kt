package io.tomorrow.api

import com.slack.eithernet.integration.retrofit.ApiResultCallAdapterFactory
import com.slack.eithernet.integration.retrofit.ApiResultConverterFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object TomorrowIoServiceBuilder {
    private val okHttpClient = OkHttpClient.Builder().build()

    fun provideTomorrowIoService(baseUrl: HttpUrl): TomorrowIoService {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ApiResultConverterFactory)
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()

        return retrofit.create(TomorrowIoService::class.java)
    }
}
