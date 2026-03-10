package com.hanadulset.pro_poseapp.data.datasource.impls

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.hanadulset.pro_poseapp.data.datasource.interfaces.NetworkDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class NetworkDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context
) : NetworkDataSource {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val networkStatus: Flow<NetworkDataSource.NetworkStatus> = callbackFlow {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                trySend(NetworkDataSource.NetworkStatus.Unavailable).isSuccess
            }

            override fun onAvailable(network: Network) {
                trySend(NetworkDataSource.NetworkStatus.Available).isSuccess
            }

            override fun onLost(network: Network) {
                trySend(NetworkDataSource.NetworkStatus.Unavailable).isSuccess
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }
}
