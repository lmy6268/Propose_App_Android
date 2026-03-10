package com.hanadulset.pro_poseapp.data.datasource.interfaces

import android.graphics.Bitmap
import android.util.SizeF
import org.opencv.core.Size

interface VisionProcessDataSource {
    fun getFixedImage(bitmap: Bitmap): Bitmap
    fun resizeBitmapWithOpenCV(bitmap: Bitmap, size: Size): Bitmap
    suspend fun useOpticalFlow(bitmap: Bitmap, targetOffset: SizeF): SizeF?
    fun stopToUseOpticalFlow()
}
