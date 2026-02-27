package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.utils.pose.PoseDataResult

interface PoseDataSource {
    suspend fun recommendPose(backgroundBitmap: Bitmap): PoseDataResult
    suspend fun preparePoseData() // 비동기 로딩을 위해 suspend 추가
}
