package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.data.utils.CheckResponse
import com.hanadulset.pro_poseapp.data.utils.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloadResourcesDataSource {
    suspend fun checkForDownload(): CheckResponse

    suspend fun startToDownload(): Flow<DownloadState>
}