package dev.hossain.weatheralert

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import com.squareup.anvil.annotations.ContributesMultibinding
import dev.hossain.weatheralert.di.ActivityKey
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.alertslist.CurrentWeatherAlertScreen
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import javax.inject.Inject

@ContributesMultibinding(AppScope::class, boundType = Activity::class)
@ActivityKey(MainActivity::class)
class MainActivity
    @Inject
    constructor(
        private val circuit: Circuit,
    ) : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            setContent {
                WeatherAlertAppTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        // See https://slackhq.github.io/circuit/navigation/
                        val backStack = rememberSaveableBackStack(root = CurrentWeatherAlertScreen("root"))
                        val navigator = rememberCircuitNavigator(backStack)

                        // See https://slackhq.github.io/circuit/circuit-content/
                        CircuitCompositionLocals(circuit) {
                            NavigableCircuitContent(
                                navigator = navigator,
                                backStack = backStack,
                                Modifier.padding(innerPadding),
                                decoration =
                                    GestureNavigationDecoration {
                                        navigator.pop()
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
