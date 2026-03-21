package com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.pose.mapper

import android.net.Uri
import android.util.SizeF
import com.hanadulset.pro_poseapp.domain.model.PoseEntity
import com.hanadulset.pro_poseapp.presentation.feature.camera.components.common.pose.model.PoseUIItem

fun PoseEntity.toUI() = PoseUIItem(
    poseId = poseId,
    poseCat = poseCat,
    bottomCenterRate = SizeF(bottomCenterRate.width, bottomCenterRate.height),
    sizeRate = SizeF(sizeRate.width, sizeRate.height),
    imageUri = imageUri?.let { Uri.parse(it) },
    imageScale = imageScale
)

fun PoseUIItem.toDomain() = PoseEntity(
    poseId = poseId,
    poseCat = poseCat,
    bottomCenterRate = com.hanadulset.pro_poseapp.domain.model.SizeFloat(bottomCenterRate.width, bottomCenterRate.height),
    sizeRate = com.hanadulset.pro_poseapp.domain.model.SizeFloat(sizeRate.width, sizeRate.height),
    imageUri = imageUri?.toString(),
    imageScale = imageScale
)
