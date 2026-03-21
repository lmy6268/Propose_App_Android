package com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.pose.model

import android.net.Uri
import android.util.SizeF

data class PoseUIItem(
    val poseId: Int = 0,
    val poseCat: Int = 0,
    val bottomCenterRate: SizeF = SizeF(0F, 0F),
    val sizeRate: SizeF = SizeF(0F, 0F),
    val imageUri: Uri? = null,
    val imageScale: Float = 1F,
)
