package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object AppBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
        FirebaseApp.initializeApp(context)
        return Firebase.analytics
    }
}
