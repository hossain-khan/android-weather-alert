package dev.hossain.weatheralert.deeplinking

import com.slack.circuit.runtime.screen.Screen

/**
 * Bundle key for deep link destination [Screen].
 *
 * NOTE: Using whole screen object is not the right way to pass a deep link destination screen.
 * Ideally, we should use a proper deep link mechanism or navigation component.
 * See https://slackhq.github.io/circuit/deep-linking-android/
 */
internal const val BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN = "destination_screen"
