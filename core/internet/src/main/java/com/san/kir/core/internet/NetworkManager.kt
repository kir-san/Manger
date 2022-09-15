package com.san.kir.core.internet

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.san.kir.core.utils.connectivityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkState {
    NOT_WIFI, NOT_CELLURAR, OK
}

@Singleton
class CellularNetwork @Inject constructor(context: Application) : TemplateNetwork(
    context, NetworkCapabilities.TRANSPORT_CELLULAR
)

@Singleton
class WifiNetwork @Inject constructor(context: Application) : TemplateNetwork(
    context, NetworkCapabilities.TRANSPORT_WIFI,
)

abstract class TemplateNetwork(
    private val context: Context,
    networkTransport: Int,
) : ConnectivityManager.NetworkCallback() {

    private val request =
        NetworkRequest.Builder().addTransportType(networkTransport).build()

    private val _state = MutableStateFlow(false)
    val state = _state.asStateFlow()

    init {
        context.connectivityManager.registerNetworkCallback(request, this)
    }

    fun stop() {
        context.connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        _state.update { true }
    }

    override fun onLost(network: Network) {
        _state.update { false }
    }
}
