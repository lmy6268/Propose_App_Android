package com.hanadulset.pro_poseapp.domain.model

data class CheckResponseEntity(
    val needToDownload: Boolean = false,
    val downloadType: Int = TYPE_MUST_DOWNLOAD,
    val totalSize: Long = 0,
    val hasRemainStorage: Boolean = false,
) {
    companion object {
        const val TYPE_NEED_CONNECTION = 10
        const val TYPE_MUST_DOWNLOAD = 0
        const val TYPE_ADDITIONAL_DOWNLOAD = 1
        const val TYPE_ERROR = -1
    }
}
