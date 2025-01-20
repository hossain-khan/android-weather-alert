package dev.hossain.weatheralert

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import com.squareup.anvil.annotations.ContributesMultibinding
import dev.hossain.weatheralert.deeplinking.DEEP_LINK_HOST_VIEW_ALERT
import dev.hossain.weatheralert.deeplinking.getIdFromPath
import dev.hossain.weatheralert.di.ActivityKey
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.network.NetworkMonitor
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.details.WeatherAlertDetailsScreen
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import dev.hossain.weatheralert.ui.theme.dimensions
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
            val data: Uri? = intent?.data
            Timber.d("onCreate action: $action, data: $data, savedInstanceState: $savedInstanceState")

            enableEdgeToEdge()

            setContent {
                val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
                WeatherAlertAppTheme(dimensions = windowSizeClass.windowWidthSizeClass.dimensions()) {
                    // See https://slackhq.github.io/circuit/navigation/
                    val backStack = rememberSaveableBackStack(root = CurrentWeatherAlertScreen("root"))
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
            val dataUri: Uri? = intent.data
            if (dataUri != null) {
                when (dataUri.host) {
                    DEEP_LINK_HOST_VIEW_ALERT -> {
                        val alertId = getIdFromPath(dataUri)
                        if (alertId != null) {
                            navigator.goTo(WeatherAlertDetailsScreen(alertId))
                        }
                    }
                }
            }
        }
    }
