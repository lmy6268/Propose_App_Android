package com.hanadulset.pro_poseapp.presentation.feature.download.mapper

import com.hanadulset.pro_poseapp.domain.model.CheckResponseEntity
import com.hanadulset.pro_poseapp.domain.model.DownloadStateEntity
import com.hanadulset.pro_poseapp.presentation.feature.download.model.CheckResponseUiItem
import com.hanadulset.pro_poseapp.presentation.feature.download.model.DownloadStateUiItem

object ModelDownloadMapper {
    fun CheckResponseEntity.toUiItem() = CheckResponseUiItem(
        needToDownload = needToDownload,
        downloadType = downloadType,
        totalSize = totalSize,
        hasRemainStorage = hasRemainStorage
    )

    fun DownloadStateEntity.toUiItem() = DownloadStateUiItem(
        currentFileName = currentFileName,
        currentFileIndex = currentFileIndex,
        totalFileCnt = totalFileCnt,
        currentBytes = currentBytes,
        totalBytes = totalBytes
    )
}
