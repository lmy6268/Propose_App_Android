package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import com.hanadulset.pro_poseapp.domain.model.pose.PoseDataResultModel
import org.opencv.core.Mat

interface PoseDataSource {
    /**포즈를 추천해준다.*/
    suspend fun recommendPose(backgroundBitmap: Bitmap): PoseDataResultModel

    fun preparePoseData()
}
