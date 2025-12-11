package dev.hossain.weatheralert.ui.about

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import dev.hossain.weatheralert.ui.devtools.DeveloperPortalScreen

/**
 * Helper function to navigate to Developer Portal.
 * This function only exists in internal builds.
 */
fun Navigator.navigateToDeveloperPortal() {
    goTo(DeveloperPortalScreen)
}

/**
 * Returns the Developer Portal screen.
 * This function only exists in internal builds.
 */
fun getDeveloperPortalScreen(): Screen = DeveloperPortalScreen
