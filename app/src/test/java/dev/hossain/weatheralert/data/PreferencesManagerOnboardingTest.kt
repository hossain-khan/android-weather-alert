package dev.hossain.weatheralert.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for [PreferencesManager] onboarding-related methods.
 */
@RunWith(RobolectricTestRunner::class)
class PreferencesManagerOnboardingTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val preferencesManager = PreferencesManager(context)

    @Before
    fun setUp() =
        runTest {
            // Clear onboarding preference before each test
            context.dataStore.edit { preferences ->
                preferences.remove(UserPreferences.onboardingCompletedKey)
            }
        }

    @Test
    fun isOnboardingCompleted_returnsFalseByDefault() =
        runTest {
            val isCompleted = preferencesManager.isOnboardingCompleted.first()
            assertThat(isCompleted).isFalse()
        }

    @Test
    fun setOnboardingCompleted_savesTrue() =
        runTest {
            preferencesManager.setOnboardingCompleted(true)

            val isCompleted = preferencesManager.isOnboardingCompleted.first()
            assertThat(isCompleted).isTrue()
        }

    @Test
    fun setOnboardingCompleted_savesFalse() =
        runTest {
            // First set to true
            preferencesManager.setOnboardingCompleted(true)
            // Then set back to false
            preferencesManager.setOnboardingCompleted(false)

            val isCompleted = preferencesManager.isOnboardingCompleted.first()
            assertThat(isCompleted).isFalse()
        }
}
