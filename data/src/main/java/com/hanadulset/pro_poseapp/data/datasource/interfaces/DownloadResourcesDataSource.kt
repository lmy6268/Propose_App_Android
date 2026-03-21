package com.hanadulset.pro_poseapp.data.datasource.interfaces

import com.hanadulset.pro_poseapp.data.model.CheckResponseDto
import com.hanadulset.pro_poseapp.data.model.DownloadStateDto
import kotlinx.coroutines.flow.Flow

interface DownloadResourcesDataSource {
    suspend fun checkForDownload(): CheckResponseDto

    suspend fun startToDownload(): Flow<DownloadStateDto>
}