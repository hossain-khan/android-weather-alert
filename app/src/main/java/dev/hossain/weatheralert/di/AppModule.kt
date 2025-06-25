package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dev.hossain.weatheralert.BuildConfig

@ContributesTo(AppScope::class)
@Module
class AppModule {
    @Provides
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context,
    ): FirebaseAnalytics {
        if (!BuildConfig.FIREBASE_ENABLED) {
            throw IllegalStateException("Firebase not enabled for this build variant")
        }
        FirebaseApp.initializeApp(context)
        return Firebase.analytics
    }
}
