package dev.hossain.weatheralert.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@ContributesTo(AppScope::class)
@Module
class AppModule {
    @Provides
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context,
    ): FirebaseAnalytics {
        FirebaseApp.initializeApp(context)
        return Firebase.analytics
    }
}
