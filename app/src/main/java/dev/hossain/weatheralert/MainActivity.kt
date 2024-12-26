package dev.hossain.weatheralert

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import dev.hossain.weatheralert.api.RetrofitClient
import dev.hossain.weatheralert.circuit.SettingsScreen
import dev.hossain.weatheralert.data.PreferencesManager
import dev.hossain.weatheralert.data.WeatherRepository
import dev.hossain.weatheralert.ui.AlertScreen
import dev.hossain.weatheralert.ui.AlertViewModel
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
                    val navController = rememberNavController()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBar(navController) }
                    ) { innerPadding ->
                        /*// See https://slackhq.github.io/circuit/navigation/
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
                        }*/

                        Box(modifier = Modifier.padding(innerPadding)) {
                            //AppNavigation(PreferencesManager(application))
                            AnimatedNavGraph(navController)
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
            composable("alerts") {
                AlertScreen(
                    AlertViewModel(
                        preferencesManager = PreferencesManager(application),
                        weatherRepository = WeatherRepository(RetrofitClient.weatherApi)
                    )
                )
            }
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
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            composable("home") {
                AlertScreen(
                    AlertViewModel(
                        preferencesManager = PreferencesManager(application),
                        weatherRepository = WeatherRepository(RetrofitClient.weatherApi)
                    )
                )
            }
            composable("settings") { SettingsScreen(PreferencesManager(application)) }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf("home", "settings")
        BottomAppBar {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            items.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        when (screen) {
                            "home" -> Icon(Icons.Filled.Home, contentDescription = "Home")
                            "settings" -> Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )

                            else -> Icon(Icons.Filled.Home, contentDescription = "Home")
                        }
                    },
                    label = { Text(screen) },
                    selected = currentRoute == screen,
                    onClick = {
                        navController.navigate(screen) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
