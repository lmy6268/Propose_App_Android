package com.hanadulset.pro_poseapp.data.model


data class DownloadStateDto(
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFileCnt: Int = 0,
    val currentBytes: Long = 0L,
    val totalBytes: Long = 0,
)