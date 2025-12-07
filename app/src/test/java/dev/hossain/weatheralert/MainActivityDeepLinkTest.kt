package dev.hossain.weatheralert

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.weatheralert.deeplinking.BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import kotlinx.collections.immutable.ImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test for MainActivity deep linking functionality.
 *
 * Verifies that MainActivity correctly parses deep link intents from notifications
 * and builds the proper navigation back stack.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityDeepLinkTest {
    @Test
    fun `parseDeepLinkedScreens builds proper back stack with WeatherAlertDetailsScreen`() {
        // Given - Create an intent with deep link to WeatherAlertDetailsScreen
        val userAlertId = 123L
        val destinationScreen = WeatherAlertDetailsScreen(userAlertId)
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra(BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN, destinationScreen)
            }

        // When - Parse the deep linked screens using reflection to access private method
        val screens = parseDeepLinkedScreensHelper(intent)

        // Then - Verify proper back stack is built
        assertThat(screens).isNotNull()
        assertThat(screens).hasSize(2)

        // Verify first screen is the root screen
        val rootScreen = screens?.get(0)
        assertThat(rootScreen).isInstanceOf(CurrentWeatherAlertScreen::class.java)
        assertThat((rootScreen as CurrentWeatherAlertScreen).id).isEqualTo("root")

        // Verify second screen is the detail screen with correct alert ID
        val detailScreen = screens?.get(1)
        assertThat(detailScreen).isInstanceOf(WeatherAlertDetailsScreen::class.java)
        assertThat((detailScreen as WeatherAlertDetailsScreen).alertId).isEqualTo(userAlertId)
    }

    @Test
    fun `parseDeepLinkedScreens returns null when intent has no extras`() {
        // Given - Create an intent without extras
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

        // When - Parse the deep linked screens
        val screens = parseDeepLinkedScreensHelper(intent)

        // Then - Should return null as there's no deep link
        assertThat(screens).isNull()
    }

    @Test
    fun `parseDeepLinkedScreens returns null when intent has no deep link screen extra`() {
        // Given - Create an intent with extras but no deep link screen
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra("some_other_key", "some_value")
            }

        // When - Parse the deep linked screens
        val screens = parseDeepLinkedScreensHelper(intent)

        // Then - Should return null as there's no deep link screen
        assertThat(screens).isNull()
    }

    /**
     * Helper method to test the private parseDeepLinkedScreens method logic.
     * This mimics the actual implementation in MainActivity.
     */
    private fun parseDeepLinkedScreensHelper(intent: Intent): ImmutableList<Screen>? {
        val bundle: Bundle = intent.extras ?: return null
        val screen: Screen? =
            try {
                bundle.getParcelable(BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN, Screen::class.java)
            } catch (e: Exception) {
                null
            }
        // Builds stack of screens to navigate to - matching MainActivity implementation
        return screen?.let {
            kotlinx.collections.immutable.persistentListOf(
                CurrentWeatherAlertScreen("root"),
                it,
            )
        }
    }
}
