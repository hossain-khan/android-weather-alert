package dev.hossain.weatheralert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.remember
import androidx.core.os.BundleCompat
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import com.squareup.anvil.annotations.ContributesMultibinding
import dev.hossain.weatheralert.deeplinking.BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN
import dev.hossain.weatheralert.di.ActivityKey
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.network.NetworkMonitor
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import timber.log.Timber
import javax.inject.Inject

@ContributesMultibinding(AppScope::class, boundType = Activity::class)
@ActivityKey(MainActivity::class)
class MainActivity
    @Inject
    constructor(
        private val circuit: Circuit,
        private val networkMonitor: NetworkMonitor,
    ) : ComponentActivity() {
        private lateinit var navigator: Navigator

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val action: String? = intent?.action
            val bundle: Bundle? = intent?.extras
            Timber.d("onCreate action: $action, bundle: $bundle, savedInstanceState: $savedInstanceState")

            enableEdgeToEdge()

            setContent {
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                WeatherAlertAppTheme(dimensions = windowSizeClass.windowWidthSizeClass.dimensions()) {
                    // See https://slackhq.github.io/circuit/navigation/
                    val stack: ImmutableList<Screen> =
                        remember {
                            parseDeepLinkedScreens(intent) ?: persistentListOf(CurrentWeatherAlertScreen("root"))
                        }
                    val backStack = rememberSaveableBackStack(stack)
                    navigator = rememberCircuitNavigator(backStack)

                    // See https://slackhq.github.io/circuit/circuit-content/
                    CircuitCompositionLocals(circuit) {
                        NavigableCircuitContent(
                            navigator = navigator,
                            backStack = backStack,
                            decoration =
                                GestureNavigationDecoration {
                                    navigator.pop()
                                },
                        )
                    }
                }
            }
        }

        override fun onStart() {
            super.onStart()
            networkMonitor.startMonitoring()
        }

        override fun onStop() {
            super.onStop()
            networkMonitor.stopMonitoring()
        }

        override fun onNewIntent(intent: Intent) {
            super.onNewIntent(intent)
            Timber.d("onNewIntent received: $intent")
            handleDeepLink(intent)
        }

        private fun handleDeepLink(intent: Intent) {
            val destinationScreen =
                intent.extras?.let {
                    BundleCompat.getParcelable(
                        it,
                        BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN,
                        Screen::class.java,
                    )
                }
            if (destinationScreen != null) {
                navigator.goTo(destinationScreen)
            }
        }

        private fun parseDeepLinkedScreens(intent: Intent): ImmutableList<Screen>? {
            val bundle: Bundle = intent.extras ?: return null
            val screen: Screen? =
                BundleCompat.getParcelable(
                    bundle,
                    BUNDLE_KEY_DEEP_LINK_DESTINATION_SCREEN,
                    Screen::class.java,
                )
            // Builds stack of screens to navigate to.
            return screen?.let { persistentListOf(CurrentWeatherAlertScreen("root"), it) }
        }
    }
