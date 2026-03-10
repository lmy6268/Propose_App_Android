package com.hanadulset.pro_poseapp.data.datasource.interfaces

import kotlinx.coroutines.flow.Flow

interface NetworkDataSource {
    val networkStatus: Flow<NetworkStatus>

    sealed class NetworkStatus {
        data object Available : NetworkStatus()
        data object Unavailable : NetworkStatus()
    }
}
