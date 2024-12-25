package dev.hossain.weatheralert

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hossain.weatheralert.circuit.InboxScreen
import dev.hossain.weatheralert.di.ActivityKey
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.ui.theme.WeatherAlertAppTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import com.squareup.anvil.annotations.ContributesMultibinding
import dev.hossain.weatheralert.circuit.SettingsScreen
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.ui.AlertScreen
import javax.inject.Inject
import dev.hossain.weatheralert.work.scheduleWeatherAlerts

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

            // Schedule weather alert worker
            scheduleWeatherAlerts(this)

            setContent {
                WeatherAlertAppTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        // See https://slackhq.github.io/circuit/navigation/
                        val backStack = rememberSaveableBackStack(root = InboxScreen)
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

    // TODO - use this later
    @Composable
    fun AppNavigation(preferencesManager: PreferencesManager) {
        val navController = rememberNavController()

        NavHost(navController, startDestination = "settings") {
            composable("settings") {
                SettingsScreen(preferencesManager)
            }
            // Add other screens here
        }
    }


    // TODO - use this later
    @Composable
    fun WeatherAlertApp(navController: NavHostController) {
        NavHost(navController, startDestination = "alerts") {
            composable("alerts") { AlertScreen() }
        }
    }

    /**
     * Transition Effects Between Screens
     *
     *     Purpose: Make navigation visually engaging.
     *     Implementation: Use AnimatedNavHost for screen transitions.
     *         Add fade-in, slide-in, or zoom-in effects for smoother screen changes.
     */
    @Composable
    fun AnimatedNavGraph(navController: NavHostController) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            composable("home") { HomeScreen() }
            composable("settings") { SettingsScreen() }
        }
    }


}
