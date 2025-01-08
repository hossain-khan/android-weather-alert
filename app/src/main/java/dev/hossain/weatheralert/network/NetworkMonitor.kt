package dev.hossain.weatheralert.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.squareup.anvil.annotations.optional.SingleIn
import dev.hossain.weatheralert.di.AppScope
import dev.hossain.weatheralert.di.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Monitors network connectivity status. Use [isConnected] to get the current network status.
 */
@SingleIn(AppScope::class)
class NetworkMonitor
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // StateFlow to expose network connectivity status
        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> get() = _isConnected

        // Tracks active networks and their capabilities
        private val activeNetworks = ConcurrentHashMap<Network, NetworkCapabilities>()

        private val networkCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        activeNetworks[network] = capabilities
                        updateNetworkStatus()
                    }
                }

                override fun onLost(network: Network) {
                    activeNetworks.remove(network)
                    updateNetworkStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) {
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        activeNetworks[network] = capabilities
                    } else {
                        activeNetworks.remove(network)
                    }
                    updateNetworkStatus()
                }
            }

        fun startMonitoring() {
            val networkRequest =
                NetworkRequest
                    .Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }

        fun stopMonitoring() {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }

        private fun updateNetworkStatus() {
            Timber.d("Is connected: ${activeNetworks.isNotEmpty()}. Active networks: ${activeNetworks.size}")
            _isConnected.value = activeNetworks.isNotEmpty()
        }
    }
