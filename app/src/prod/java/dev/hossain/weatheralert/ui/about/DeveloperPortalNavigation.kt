package dev.hossain.weatheralert.ui.about

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen

/**
 * Stub implementation for prod builds.
 * Developer Portal is not available in production.
 */
fun Navigator.navigateToDeveloperPortal() {
    // No-op in production builds
}

/**
 * Stub implementation for prod builds.
 * Returns null as Developer Portal doesn't exist in production.
 */
fun getDeveloperPortalScreen(): Screen? = null
